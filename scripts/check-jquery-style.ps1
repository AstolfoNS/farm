$ErrorActionPreference = "Stop"

$targets = @(
    "src/main/resources/static/js/app",
    "src/main/resources/static/html/app.html"
)

$forbiddenPatterns = @(
    "document.getElementById",
    "querySelector(",
    "querySelectorAll(",
    "addEventListener(",
    "fetch("
)

$violations = @()

foreach ($target in $targets) {
    if (-not (Test-Path -LiteralPath $target)) {
        continue
    }
    foreach ($pattern in $forbiddenPatterns) {
        $result = rg -n --fixed-strings $pattern $target
        if ($LASTEXITCODE -eq 0 -and $result) {
            $violations += $result
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Output "jQuery style check failed. Forbidden API usage found:"
    $violations | ForEach-Object { Write-Output $_ }
    exit 1
}

Write-Output "jQuery style check passed."
exit 0
