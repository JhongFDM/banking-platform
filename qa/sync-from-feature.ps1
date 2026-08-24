param(
    [string]$SourceBranch = "origin/feature/springai"
)

$ErrorActionPreference = "Stop"

$current = git rev-parse --abbrev-ref HEAD
if ($current -ne "QA") {
    Write-Error "You must be on branch 'QA' to sync. Current branch: $current"
}

git fetch origin
git merge --ff-only $SourceBranch
Write-Output "QA is now synced from $SourceBranch"
