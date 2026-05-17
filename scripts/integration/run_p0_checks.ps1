param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$OutFile = "",
    [int]$TimeoutSec = 20,
    [int]$HarvestWaitSec = 180
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if ([string]::IsNullOrWhiteSpace($OutFile)) {
    $OutFile = Join-Path $repoRoot "tmp.integration.validation.json"
}

$runId = [Guid]::NewGuid().ToString("N")
$results = New-Object System.Collections.Generic.List[object]
$context = [ordered]@{
    runId = $runId
    userId = $null
    seedTypeId = $null
    plotId = $null
    cropId = $null
    unlockPlotId = $null
}

function New-RequestId {
    param([string]$Name)
    return "$runId-$Name"
}

function Convert-JsonText {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $null
    }
    if ($PSVersionTable.PSVersion.Major -ge 6) {
        return $Text | ConvertFrom-Json -Depth 20
    }
    return $Text | ConvertFrom-Json
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null
    )
    $uri = "$BaseUrl$Path"
    $payload = $null
    if ($null -ne $Body) {
        $payload = $Body | ConvertTo-Json -Depth 20
    }
    try {
        $commonArgs = @{
            Uri = $uri
            ContentType = "application/json; charset=utf-8"
            TimeoutSec = $TimeoutSec
        }
        if ($PSVersionTable.PSVersion.Major -lt 6) {
            $commonArgs.UseBasicParsing = $true
        }
        if ($Method -eq "GET") {
            $resp = Invoke-WebRequest -Method GET @commonArgs
        } else {
            if ($null -ne $payload) {
                $commonArgs.Body = $payload
            }
            $resp = Invoke-WebRequest -Method $Method @commonArgs
        }
        $json = $null
        if ($resp.Content) {
            $json = Convert-JsonText -Text $resp.Content
        }
        return [ordered]@{
            ok = $true
            uri = $uri
            method = $Method
            request = $Body
            statusCode = [int]$resp.StatusCode
            raw = $resp.Content
            json = $json
            error = $null
        }
    } catch {
        $statusCode = 0
        $raw = ""
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode.value__
        }
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $raw = $_.ErrorDetails.Message
        } elseif ($_.Exception.Response) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $raw = $reader.ReadToEnd()
                    $reader.Close()
                }
            } catch {
            }
        }
        $json = $null
        if ($raw) {
            try {
                $json = Convert-JsonText -Text $raw
            } catch {
            }
        }
        return [ordered]@{
            ok = $false
            uri = $uri
            method = $Method
            request = $Body
            statusCode = $statusCode
            raw = $raw
            json = $json
            error = $_.Exception.Message
        }
    }
}

function Is-ApiSuccess {
    param([object]$Resp)
    return ($Resp.statusCode -eq 200 -and $Resp.json -and $Resp.json.code -eq 200)
}

function Get-BizCode {
    param([object]$Resp)
    if ($Resp.json -and $Resp.json.details -and $Resp.json.details.bizCode) {
        return [string]$Resp.json.details.bizCode
    }
    return ""
}

function Is-BizError {
    param(
        [object]$Resp,
        [string]$BizCode,
        [int[]]$ApiCodes = @(400, 500)
    )
    if (-not $Resp.json) {
        return $false
    }
    $actualBizCode = Get-BizCode $Resp
    if ($actualBizCode -ne $BizCode) {
        return $false
    }
    if ($null -eq $Resp.json.code) {
        return $false
    }
    return ($ApiCodes -contains [int]$Resp.json.code)
}

function Add-Case {
    param(
        [string]$Name,
        [bool]$Passed,
        [object]$Resp,
        [string]$Note = ""
    )
    $results.Add([pscustomobject]@{
        name = $Name
        passed = $Passed
        statusCode = $Resp.statusCode
        apiCode = if ($Resp.json) { $Resp.json.code } else { $null }
        msg = if ($Resp.json) { $Resp.json.msg } else { $Resp.error }
        bizCode = Get-BizCode $Resp
        note = $Note
        method = $Resp.method
        uri = $Resp.uri
    })
}

Write-Host "P0 regression run id: $runId"
Write-Host "Base URL: $BaseUrl"

# 0) Health check
$resp = Invoke-Api -Method "GET" -Path "/user/list"
$ok = Is-ApiSuccess $resp
Add-Case -Name "health.user.list" -Passed $ok -Resp $resp -Note "Backend health check"
if (-not $ok) {
    Write-Host "Backend is not ready, stop run."
    $report = [ordered]@{
        generatedAt = (Get-Date).ToString("o")
        baseUrl = $BaseUrl
        summary = [ordered]@{
            total = 1
            passed = 0
            failed = 1
        }
        context = $context
        results = $results
    }
    $report | ConvertTo-Json -Depth 20 | Set-Content -Path $OutFile -Encoding UTF8
    exit 1
}

# 1) Create test user
$username = "p0_user_$([DateTimeOffset]::Now.ToUnixTimeSeconds())"
$addUserBody = @{
    username = $username
    nickname = "P0回归用户"
    coin = "200000"
    experience = "0"
    score = "0"
}
$resp = Invoke-Api -Method "POST" -Path "/user/addOrUpdate" -Body $addUserBody
$ok = Is-ApiSuccess $resp -and $resp.json.data -and $resp.json.data.id
Add-Case -Name "user.create" -Passed $ok -Resp $resp -Note "Create user for P0 test"
if ($ok) {
    $context.userId = [int64]$resp.json.data.id
}

# 2) Query seed shop
$resp = Invoke-Api -Method "POST" -Path "/seed/shop/page" -Body @{ page = 1; rows = 10 }
$records = @()
if ($resp.json -and $resp.json.data -and $resp.json.data.records) {
    $records = @($resp.json.data.records)
}
$ok = Is-ApiSuccess $resp -and $records.Count -gt 0
Add-Case -Name "seed.shop.page" -Passed $ok -Resp $resp -Note "Need at least one seed type"
if ($ok) {
    $context.seedTypeId = [int64]$records[0].id
}

# 3) Buy seed + idempotent repeat
$buyRequestId = New-RequestId "buy-seed"
$buyBody = @{
    requestId = $buyRequestId
    userId = $context.userId
    seedTypeId = $context.seedTypeId
    quantity = 3
}
$respBuy1 = Invoke-Api -Method "POST" -Path "/seed/shop/buy" -Body $buyBody
$okBuy1 = Is-ApiSuccess $respBuy1
Add-Case -Name "seed.shop.buy.first" -Passed $okBuy1 -Resp $respBuy1

$respBuy2 = Invoke-Api -Method "POST" -Path "/seed/shop/buy" -Body $buyBody
$sameBuyAfterQty = $false
if ($respBuy1.json -and $respBuy2.json) {
    $sameBuyAfterQty = ([string]$respBuy1.json.data.afterSeedQuantity -eq [string]$respBuy2.json.data.afterSeedQuantity)
}
$okBuy2 = Is-ApiSuccess $respBuy2 -and $sameBuyAfterQty
Add-Case -Name "seed.shop.buy.idempotent" -Passed $okBuy2 -Resp $respBuy2 -Note "Repeat same requestId should return cached result"

# 4) Plantable plots
$resp = Invoke-Api -Method "POST" -Path "/gameplay/seedPlantablePlots" -Body @{
    userId = $context.userId
    seedTypeId = $context.seedTypeId
}
$plots = @()
if ($resp.json -and $resp.json.data -and $resp.json.data.plots) {
    $plots = @($resp.json.data.plots)
}
$ok = Is-ApiSuccess $resp -and $plots.Count -gt 0
Add-Case -Name "gameplay.seedPlantablePlots" -Passed $ok -Resp $resp
if ($ok) {
    $context.plotId = [int64]$plots[0].plotId
}

# 5) Plant + idempotent repeat
$plantRequestId = New-RequestId "plant"
$plantBody = @{
    requestId = $plantRequestId
    userId = $context.userId
    plotId = $context.plotId
    seedTypeId = $context.seedTypeId
}
$respPlant1 = Invoke-Api -Method "POST" -Path "/gameplay/plant" -Body $plantBody
$okPlant1 = Is-ApiSuccess $respPlant1
Add-Case -Name "gameplay.plant.first" -Passed $okPlant1 -Resp $respPlant1
if ($okPlant1) {
    $context.cropId = [int64]$respPlant1.json.data.cropId
}
$respPlant2 = Invoke-Api -Method "POST" -Path "/gameplay/plant" -Body $plantBody
$sameCropId = $false
if ($respPlant1.json -and $respPlant2.json) {
    $sameCropId = ([string]$respPlant1.json.data.cropId -eq [string]$respPlant2.json.data.cropId)
}
$okPlant2 = Is-ApiSuccess $respPlant2 -and $sameCropId
Add-Case -Name "gameplay.plant.idempotent" -Passed $okPlant2 -Resp $respPlant2

# 6) Crop action page
$resp = Invoke-Api -Method "POST" -Path "/gameplay/crop/action/page" -Body @{
    userId = $context.userId
    actionType = "PLANT"
    page = 1
    rows = 10
}
$logCount = 0
if ($resp.json -and $resp.json.data) {
    $logCount = [int64]$resp.json.data.total
}
$ok = Is-ApiSuccess $resp -and $logCount -ge 1
Add-Case -Name "gameplay.crop.action.page" -Passed $ok -Resp $resp

# 7) Harvest before ripe should fail
$preHarvestRequestId = New-RequestId "harvest-not-ripe"
$resp = Invoke-Api -Method "POST" -Path "/gameplay/harvest" -Body @{
    requestId = $preHarvestRequestId
    userId = $context.userId
    plotId = $context.plotId
}
$ok = Is-BizError -Resp $resp -BizCode "CROP_NOT_RIPE" -ApiCodes @(500)
Add-Case -Name "gameplay.harvest.not.ripe" -Passed $ok -Resp $resp -Note "Expected blocked before mature"

# 8) Wait ripe and harvest success (+idempotent)
$ripe = $false
$deadline = (Get-Date).AddSeconds($HarvestWaitSec)
while ((Get-Date) -lt $deadline) {
    $respOverview = Invoke-Api -Method "POST" -Path "/gameplay/myFarmOverview" -Body @{ userId = $context.userId }
    if (Is-ApiSuccess $respOverview) {
        $targetPlot = $null
        if ($respOverview.json.data -and $respOverview.json.data.plots) {
            foreach ($plot in @($respOverview.json.data.plots)) {
                if ([string]$plot.plotId -eq [string]$context.plotId) {
                    $targetPlot = $plot
                    break
                }
            }
        }
        if ($targetPlot -and $targetPlot.crop -and $targetPlot.crop.harvestable -eq $true) {
            $ripe = $true
            break
        }
    }
    Start-Sleep -Seconds 3
}

if ($ripe) {
    $harvestRequestId = New-RequestId "harvest-success"
    $harvestBody = @{
        requestId = $harvestRequestId
        userId = $context.userId
        plotId = $context.plotId
    }
    $respHarvest1 = Invoke-Api -Method "POST" -Path "/gameplay/harvest" -Body $harvestBody
    $okHarvest1 = Is-ApiSuccess $respHarvest1
    Add-Case -Name "gameplay.harvest.first" -Passed $okHarvest1 -Resp $respHarvest1

    $respHarvest2 = Invoke-Api -Method "POST" -Path "/gameplay/harvest" -Body $harvestBody
    $sameHarvestCropId = $false
    if ($respHarvest1.json -and $respHarvest2.json) {
        $sameHarvestCropId = ([string]$respHarvest1.json.data.cropId -eq [string]$respHarvest2.json.data.cropId)
    }
    $okHarvest2 = Is-ApiSuccess $respHarvest2 -and $sameHarvestCropId
    Add-Case -Name "gameplay.harvest.idempotent" -Passed $okHarvest2 -Resp $respHarvest2
} else {
    $respMock = [ordered]@{
        statusCode = 0
        method = "WAIT"
        uri = "$BaseUrl/gameplay/myFarmOverview"
        json = $null
        error = "harvest wait timeout"
    }
    Add-Case -Name "gameplay.harvest.wait.ripe" -Passed $false -Resp $respMock -Note "Not ripe within timeout"
}

# 9) Shop sell fruit (+idempotent)
$sellRequestId = New-RequestId "sell-fruit"
$sellBody = @{
    requestId = $sellRequestId
    userId = $context.userId
    seedTypeId = $context.seedTypeId
    quantity = 1
}
$respSell1 = Invoke-Api -Method "POST" -Path "/seed/shop/sell-fruit" -Body $sellBody
$okSell1 = Is-ApiSuccess $respSell1
Add-Case -Name "seed.shop.sell.first" -Passed $okSell1 -Resp $respSell1

$respSell2 = Invoke-Api -Method "POST" -Path "/seed/shop/sell-fruit" -Body $sellBody
$sameSellAfterCoin = $false
if ($respSell1.json -and $respSell2.json) {
    $sameSellAfterCoin = ([string]$respSell1.json.data.afterCoin -eq [string]$respSell2.json.data.afterCoin)
}
$okSell2 = Is-ApiSuccess $respSell2 -and $sameSellAfterCoin
Add-Case -Name "seed.shop.sell.idempotent" -Passed $okSell2 -Resp $respSell2

# 10) Plot status + unlock order checks
$respStatus = Invoke-Api -Method "POST" -Path "/gameplay/plot/status" -Body @{ userId = $context.userId }
$okStatus = Is-ApiSuccess $respStatus
Add-Case -Name "gameplay.plot.status" -Passed $okStatus -Resp $respStatus
if ($okStatus -and $respStatus.json.data) {
    $context.unlockPlotId = $respStatus.json.data.nextUnlockPlotId
}

$outOfOrderPlotId = $null
if ($okStatus -and $respStatus.json.data -and $respStatus.json.data.plots) {
    $locked = @($respStatus.json.data.plots | Where-Object { $_.locked -eq $true })
    if ($locked.Count -gt 1) {
        $sorted = @($locked | Sort-Object plotIndex -Descending)
        $outOfOrderPlotId = $sorted[0].plotId
        if ([string]$outOfOrderPlotId -eq [string]$context.unlockPlotId -and $sorted.Count -gt 1) {
            $outOfOrderPlotId = $sorted[1].plotId
        }
    }
}
if ($outOfOrderPlotId) {
    $resp = Invoke-Api -Method "POST" -Path "/gameplay/plot/unlock" -Body @{
        userId = $context.userId
        plotId = $outOfOrderPlotId
    }
    $ok = Is-BizError -Resp $resp -BizCode "PLOT_UNLOCK_ORDER_INVALID" -ApiCodes @(500)
    Add-Case -Name "gameplay.plot.unlock.out.of.order" -Passed $ok -Resp $resp
}

if ($context.unlockPlotId) {
    $resp = Invoke-Api -Method "POST" -Path "/gameplay/plot/unlock" -Body @{
        userId = $context.userId
        plotId = $context.unlockPlotId
    }
    $ok = Is-ApiSuccess $resp
    Add-Case -Name "gameplay.plot.unlock.next" -Passed $ok -Resp $resp
}

$resp = Invoke-Api -Method "POST" -Path "/gameplay/plot/expand" -Body @{ userId = $context.userId; soilTypeId = 1 }
Add-Case -Name "gameplay.plot.expand" -Passed (Is-ApiSuccess $resp) -Resp $resp

# 11) Shop/plot pages and options
$resp = Invoke-Api -Method "POST" -Path "/seed/shop/home" -Body @{ userId = $context.userId; page = 1; rows = 10 }
Add-Case -Name "seed.shop.home" -Passed (Is-ApiSuccess $resp) -Resp $resp

$resp = Invoke-Api -Method "POST" -Path "/seed/shop/trade/page" -Body @{ userId = $context.userId; page = 1; rows = 10 }
Add-Case -Name "seed.shop.trade.page" -Passed (Is-ApiSuccess $resp) -Resp $resp

$resp = Invoke-Api -Method "POST" -Path "/seed/shop/fruit/page" -Body @{ userId = $context.userId; page = 1; rows = 10 }
Add-Case -Name "seed.shop.fruit.page" -Passed (Is-ApiSuccess $resp) -Resp $resp

$resp = Invoke-Api -Method "POST" -Path "/gameplay/plot/trade/page" -Body @{ userId = $context.userId; bizType = "UNLOCK"; page = 1; rows = 10 }
Add-Case -Name "gameplay.plot.trade.page.alias" -Passed (Is-ApiSuccess $resp) -Resp $resp

$resp = Invoke-Api -Method "GET" -Path "/gameplay/plot/trade/bizType/options"
$optOk = Is-ApiSuccess $resp
if ($optOk -and $resp.json.data) {
    $types = @($resp.json.data | ForEach-Object { $_.bizType })
    $optOk = ($types -contains "UNLOCK_PLOT" -and $types -contains "EXPAND_PLOT")
}
Add-Case -Name "gameplay.plot.trade.bizType.options" -Passed $optOk -Resp $resp

# 12) Parameter validation regressions
$resp = Invoke-Api -Method "POST" -Path "/seed/shop/page" -Body @{ page = 1; rows = 101 }
$ok = Is-BizError -Resp $resp -BizCode "PARAM_INVALID" -ApiCodes @(400)
Add-Case -Name "validation.seed.shop.page.rows.max" -Passed $ok -Resp $resp

$resp = Invoke-Api -Method "POST" -Path "/seed/shop/home" -Body @{ userId = $context.userId; page = 0; rows = 10 }
$ok = Is-BizError -Resp $resp -BizCode "PARAM_INVALID" -ApiCodes @(400)
Add-Case -Name "validation.seed.shop.home.page.min" -Passed $ok -Resp $resp

$resp = Invoke-Api -Method "POST" -Path "/gameplay/plant" -Body @{
    requestId = New-RequestId "invalid-plant"
    userId = $context.userId
    plotId = 0
    seedTypeId = $context.seedTypeId
}
$ok = Is-BizError -Resp $resp -BizCode "PARAM_INVALID" -ApiCodes @(400)
Add-Case -Name "validation.gameplay.plant.plotId.positive" -Passed $ok -Resp $resp

$passed = @($results | Where-Object { $_.passed -eq $true }).Count
$total = $results.Count
$failed = $total - $passed

$report = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    baseUrl = $BaseUrl
    summary = [ordered]@{
        total = $total
        passed = $passed
        failed = $failed
    }
    context = $context
    results = $results
}

$outDir = Split-Path -Parent $OutFile
if (-not [string]::IsNullOrWhiteSpace($outDir) -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$report | ConvertTo-Json -Depth 20 | Set-Content -Path $OutFile -Encoding UTF8

Write-Host ""
Write-Host "==== P0 regression summary ===="
Write-Host "Total: $total, Passed: $passed, Failed: $failed"
Write-Host "Report: $OutFile"

if ($failed -gt 0) {
    Write-Host ""
    Write-Host "Failed cases:"
    $results | Where-Object { $_.passed -eq $false } | ForEach-Object {
        Write-Host "- $($_.name) | status=$($_.statusCode) | bizCode=$($_.bizCode) | msg=$($_.msg)"
    }
    exit 1
}

exit 0
