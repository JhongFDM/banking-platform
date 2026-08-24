# QA Branch Workflow

## Purpose
Use `QA` as the only branch for QA commits and pushes.

## Sync latest dev changes into QA
Run from branch `QA`:

```powershell
git fetch origin
git merge --ff-only origin/feature/springai
```

If fast-forward is not possible:

```powershell
git fetch origin
git merge origin/feature/springai
```

## Push QA results

```powershell
git push origin QA
```

## Guardrails
Local git hooks in `qa/.githooks` block commits and pushes unless you are on `QA`, and block pushes to non-`QA` branch targets.
