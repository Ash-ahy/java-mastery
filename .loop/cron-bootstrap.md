# cron bootstrap notes

This file documents the recommended Hermes cron jobs for the local loop runtime.

Workdir:
- /mnt/c/Users/aihey/Desktop/java_study/java-mastery

Environment bootstrap for each cron shell:
- export $(grep -v '^#' .loop/.env.local | xargs)

Recommended jobs:

1. triage-context-builder
- schedule: every 2h
- prompt: read `.loop/runtime/triage-context.json`, classify signals with `.loop/prompts/triage.prompt.md`, write JSON to `.loop/runtime/triage-result.json`, then run `python3 .loop/scripts/apply_triage.py --input .loop/runtime/triage-result.json` and `python3 .loop/scripts/select_next_item.py > .loop/runtime/selected-item.json`

2. maintenance-unlock
- schedule: every 6h
- command:
  python3 .loop/scripts/unlock_stale_locks.py

3. daily-summary-context
- schedule: 0 9 * * 1-5
- prompt: read `.loop/runtime/daily-summary-context.json`, summarize with `.loop/prompts/summary.prompt.md`, write JSON to `.loop/runtime/daily-summary-result.json`, then run `python3 .loop/scripts/apply_summary.py --input .loop/runtime/daily-summary-result.json`

Current limitation:
- Full autonomous cron execution still needs a self-contained Hermes prompt that reads these JSON files and writes back result JSON before apply_* scripts run.
- GitHub webhook ingestion is not connected yet.
- GitHub remote is connected (`origin`), so remote-aware automation is now possible, but PR/merge automation remains policy-disabled until explicitly enabled.
