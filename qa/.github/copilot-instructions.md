# QA Workspace Policy

This folder holds QA-only Copilot/agent guidance and automation artifacts.

Rules for QA operations:
- Work on branch `QA` only.
- Never push to any branch other than `QA`.
- Pull developer changes from `origin/feature/springai` into `QA` for validation.
- Keep QA scripts and agent definitions under `qa/`.
