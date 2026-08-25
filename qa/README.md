# QA Automation Workspace

This folder contains QA-only assets and test automation frameworks.

## Branch policy
- Work on branch `QA` only.
- Never push to any branch other than `QA`.
- Pull dev updates from `origin/feature/springai` into `QA`.

## Layout
- `selenium/` - Existing Selenium + Cucumber + Maven suite.
- `playwright/` - New Playwright suite (in progress).
- `.githooks/` - Local git safeguards for QA-only commits/pushes.
- `.github/` - QA-specific Copilot guidance.
- `sync-from-feature.ps1` - Sync helper from dev branch into QA.

## Common commands
Sync QA with latest dev branch:

```powershell
./sync-from-feature.ps1
```

Run Selenium suite:

```powershell
cd ./selenium
mvn test
```
