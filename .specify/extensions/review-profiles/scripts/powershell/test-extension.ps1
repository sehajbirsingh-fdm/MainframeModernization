param(
    [string]$SpecKitProject = "",
    [string]$SpecifyExecutable = "specify",
    [string]$TestRoot = "",
    [switch]$Cleanup
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message"
}

function Invoke-Specify {
    param([string[]]$Arguments)

    if ($SpecKitProject) {
        & uv run --project $SpecKitProject specify @Arguments
    }
    else {
        & $SpecifyExecutable @Arguments
    }

    if ($LASTEXITCODE -ne 0) {
        throw "specify command failed: $($Arguments -join ' ')"
    }
}

function Assert-File {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Expected file not found: $Path"
    }
}

$extensionRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

if (-not $TestRoot) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $TestRoot = Join-Path ([System.IO.Path]::GetTempPath()) "speckit-review-profiles-test-$timestamp"
}

$projectDir = Join-Path $TestRoot "project"

Write-Step "Validating extension source"
Assert-File (Join-Path $extensionRoot "extension.yml")
Assert-File (Join-Path $extensionRoot "commands\speckit.review-profiles.review.md")
Assert-File (Join-Path $extensionRoot "review-profiles-config.yml")
Assert-File (Join-Path $extensionRoot "rules\api-code-review-rules.md")
Assert-File (Join-Path $extensionRoot "rules\frontend-code-review-rules.md")
Assert-File (Join-Path $extensionRoot "rules\security-code-quality-rules.md")
Assert-File (Join-Path $extensionRoot "scripts\powershell\new-review-report.ps1")

New-Item -ItemType Directory -Force -Path $TestRoot | Out-Null

Write-Step "Creating temporary Spec Kit project"
Invoke-Specify @(
    "init",
    $projectDir,
    "--integration",
    "copilot",
    "--script",
    "ps",
    "--ignore-agent-tools"
)

Write-Step "Installing extension in dev mode"
Push-Location $projectDir
try {
    Invoke-Specify @(
        "extension",
        "add",
        $extensionRoot,
        "--dev",
        "--force"
    )

    Write-Step "Checking generated Copilot files"
    Assert-File ".github\agents\speckit.review-profiles.review.agent.md"
    Assert-File ".github\agents\speckit.review-profiles.gate.agent.md"
    Assert-File ".github\prompts\speckit.review-profiles.review.prompt.md"
    Assert-File ".github\prompts\speckit.review-profiles.gate.prompt.md"

    Write-Step "Checking installed extension files"
    Assert-File ".specify\extensions\review-profiles\extension.yml"
    Assert-File ".specify\extensions\review-profiles\review-profiles-config.yml"
    Assert-File ".specify\extensions\review-profiles\rules\api-code-review-rules.md"
    Assert-File ".specify\extensions\review-profiles\rules\frontend-code-review-rules.md"
    Assert-File ".specify\extensions\review-profiles\rules\security-code-quality-rules.md"
    Assert-File ".specify\extensions\review-profiles\scripts\powershell\new-review-report.ps1"

    Write-Step "Checking extension registry"
    Invoke-Specify @("extension", "list")
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "PASS: Spec Kit Review Profiles extension installed and registered successfully."
Write-Host "Test project: $projectDir"

if ($Cleanup) {
    $resolvedTestRoot = Resolve-Path -LiteralPath $TestRoot
    if ($resolvedTestRoot.Path -like (Join-Path ([System.IO.Path]::GetTempPath()) "*")) {
        Remove-Item -LiteralPath $resolvedTestRoot.Path -Recurse -Force
        Write-Host "Cleaned up: $($resolvedTestRoot.Path)"
    }
    else {
        Write-Warning "Refusing to clean up non-temp path: $($resolvedTestRoot.Path)"
    }
}
