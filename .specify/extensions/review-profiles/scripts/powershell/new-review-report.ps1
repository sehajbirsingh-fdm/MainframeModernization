param(
    [string]$FeatureDirectory = "",
    [string]$ReviewsRoot = ".specify\reviews",
    [string]$Profile = "api",
    [string]$Result = "PENDING",
    [string]$BaseBranch = "",
    [string]$ReportPrefix = "review",
    [string]$ReportDirectoryName = "reviews",
    [string]$LatestAlias = "",
    [string]$Timestamp = "",
    [switch]$NoIndex
)

$ErrorActionPreference = "Stop"

function Get-NextReviewVersion {
    param([string]$Directory, [string]$Prefix)

    if (-not (Test-Path -LiteralPath $Directory -PathType Container)) {
        return 1
    }

    $pattern = "^$([regex]::Escape($Prefix))-v(\d+)-\d{8}-\d{6}\.md$"
    $versions = @(Get-ChildItem -LiteralPath $Directory -Filter "$Prefix-v*.md" -File |
        ForEach-Object {
            if ($_.Name -match $pattern) {
                [int]$Matches[1]
            }
        })

    if ($versions.Count -eq 0) {
        return 1
    }

    return [int](($versions | Measure-Object -Maximum).Maximum + 1)
}

function Convert-ToRelativePath {
    param([string]$Path)

    try {
        return [System.IO.Path]::GetRelativePath((Get-Location).Path, (Resolve-Path -LiteralPath $Path).Path)
    }
    catch {
        return $Path
    }
}

if (-not $Timestamp) {
    $Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
}

$generatedAt = Get-Date -Format "yyyy-MM-ddTHH:mm:sszzz"

if ($FeatureDirectory) {
    $reportDirectory = Join-Path $FeatureDirectory $ReportDirectoryName
    $latestAliasName = if ($LatestAlias) { $LatestAlias } else { "review.md" }
    $latestAliasPath = Join-Path $FeatureDirectory $latestAliasName
    $featureLabel = $FeatureDirectory
    $latestReportLinkPrefix = "$ReportDirectoryName/"
}
else {
    $reportDirectory = $ReviewsRoot
    $latestAliasName = if ($LatestAlias) { $LatestAlias } else { "latest-review.md" }
    $latestAliasPath = Join-Path $ReviewsRoot $latestAliasName
    $featureLabel = "No Spec Kit feature directory found"
    $latestReportLinkPrefix = ""
}

New-Item -ItemType Directory -Force -Path $reportDirectory | Out-Null

$nextVersionNumber = [int](Get-NextReviewVersion -Directory $reportDirectory -Prefix $ReportPrefix)
$version = "v{0:D3}" -f $nextVersionNumber
$reportFileName = "$ReportPrefix-$version-$Timestamp.md"
$reportPath = Join-Path $reportDirectory $reportFileName
$indexPath = Join-Path $reportDirectory "review-index.md"

$template = @"
# Review Report

## Report Metadata

- Report version: $version
- Generated at: $generatedAt
- Timestamp: $Timestamp
- Profile: $Profile
- Result: $Result
- Feature directory: $featureLabel
- Base branch: $BaseBranch
- Report file: $(Convert-ToRelativePath $reportPath)

## Review Result

- Profile: $Profile
- Result: $Result
- Blockers: 0
- Major issues: 0
- Minor issues: 0
- Info notes: 0

## Executive Summary

Replace this placeholder with the review summary.

## Changed Files Reviewed

Replace this placeholder with changed files reviewed.

## Rules Loaded

Replace this placeholder with rule files used.

## Findings

Replace this placeholder with findings, or state that no findings were found.

## Spec Traceability

| Spec/task item | Evidence found | Status | Notes |
| --- | --- | --- | --- |

## Test Coverage Review

| Required test area | Evidence found | Status | Notes |
| --- | --- | --- | --- |

## QA Notes

Replace this placeholder with QA checks.

## PR Comment Draft

Replace this placeholder with a concise PR comment.
"@

Set-Content -LiteralPath $reportPath -Value $template -Encoding UTF8

$latest = @"
# Latest Review Report

- Latest report: [$(Split-Path -Leaf $reportPath)]($latestReportLinkPrefix$(Split-Path -Leaf $reportPath))
- Report version: $version
- Generated at: $generatedAt
- Profile: $Profile
- Result: $Result

See the versioned report file for full findings.
"@

Set-Content -LiteralPath $latestAliasPath -Value $latest -Encoding UTF8

if (-not $NoIndex) {
    if (-not (Test-Path -LiteralPath $indexPath -PathType Leaf)) {
        $indexHeader = @"
# Review Report Index

| Version | Timestamp | Profile | Result | Report |
| --- | --- | --- | --- | --- |
"@
        Set-Content -LiteralPath $indexPath -Value $indexHeader -Encoding UTF8
    }

    $relativeReport = Split-Path -Leaf $reportPath
    Add-Content -LiteralPath $indexPath -Value "| $version | $generatedAt | $Profile | $Result | [$relativeReport]($relativeReport) |"
}

[PSCustomObject]@{
    ReportPath = (Resolve-Path -LiteralPath $reportPath).Path
    LatestAliasPath = (Resolve-Path -LiteralPath $latestAliasPath).Path
    IndexPath = if (Test-Path -LiteralPath $indexPath -PathType Leaf) { (Resolve-Path -LiteralPath $indexPath).Path } else { "" }
    Version = $version
    Timestamp = $Timestamp
    GeneratedAt = $generatedAt
} | ConvertTo-Json
