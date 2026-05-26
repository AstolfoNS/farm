$ErrorActionPreference = "Stop"

$resourceRoots = @(
    "src/main/resources/static/resources/imgs",
    "src/main/resources/static/resources/sounds"
)

$namePattern = '^[a-z0-9]+(?:-[a-z0-9]+)*\.[a-z0-9]+$'
$invalidFiles = @()

foreach ($root in $resourceRoots) {
    if (-not (Test-Path -LiteralPath $root)) {
        continue
    }
    $files = Get-ChildItem -LiteralPath $root -File -Recurse
    foreach ($file in $files) {
        if ($file.Name -cnotmatch $namePattern) {
            $invalidFiles += $file.FullName
        }
    }
}

if ($invalidFiles.Count -gt 0) {
    Write-Output "Resource naming check failed. Non-kebab-case files:"
    $invalidFiles | Sort-Object | ForEach-Object { Write-Output " - $_" }
    exit 1
}

Write-Output "Resource naming check passed."
exit 0
