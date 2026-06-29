# .loop/README.md

This directory contains the guarded loop-engineering scaffold for `java-mastery`.

What is already implemented:
- PostgreSQL schema for events, work items, run log, decisions, budgets, and locks
- Human-facing markdown state files at project root
- Prompt templates for triage/action/verify/summary/maintenance loops
- Python runtime skeletons for state hydration, triage application, action/verify application, summary application, reconciliation, and stale lock cleanup

Current limitation:
- The local workspace now has Git initialized and a GitHub remote connected.
- Remote `master` contains the current bootstrap baseline.
- Branch/worktree/PR automation can be enabled later, but remains intentionally off by default at L1.

Environment variables:
- `LOOP_DATABASE_URL`: PostgreSQL connection string
- `LOOP_REPO_NAME`: optional logical repo name, defaults to project folder name
- `LOOP_PHASE`: optional, defaults to `L1`
- `LOOP_RUNNER`: optional logical runner name, defaults to `hermes-local`

Suggested bootstrap:

1. Create a PostgreSQL database for the loop state.
2. Apply `.loop/sql/schema.sql`.
3. Export `LOOP_DATABASE_URL`.
4. Run the first context build:
   - `python .loop/scripts/triage_context.py > triage-context.json`
5. Feed `triage-context.json` to Hermes with `.loop/prompts/triage.prompt.md`.
6. Save the model JSON result and apply it:
   - `python .loop/scripts/apply_triage.py --input triage-result.json`
7. Rebuild `STATE.md` automatically through the script lifecycle.

Script flow:
- `triage_context.py`: pull DB + markdown context for the triage loop
- `apply_triage.py`: upsert work items and write STATE.md / run log
- `select_next_item.py`: claim one low-risk work item for execution
- `apply_action_result.py`: persist action-loop output
- `apply_verify_result.py`: persist verifier verdicts
- `build_daily_summary.py`: build 24h operational context
- `apply_summary.py`: persist summary-loop output
- `reconcile_github.py`: close/update items from incoming events
- `unlock_stale_locks.py`: recover stale locks and abandoned work

Notes:
- Runtime scripts support `psycopg` first, then `psycopg2`.
- If neither package is installed, the scripts will stop with a clear dependency message.
- Markdown files at project root are regenerated from PostgreSQL state; do not hand-edit them for durable machine state.
