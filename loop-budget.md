# loop-budget.md

Daily limits:
- tokens: 500000
- action_runs: 5
- auto_prs: 2
- alerts: 10

Per-item limits:
- max_attempts: 3
- max_runtime_minutes: 15
- max_files_touched: 5

Safety limits:
- no action on medium/high/critical risk
- no action on forbidden paths
- pause after 3 consecutive failed runs
- pause if verifier rejects same item twice

Escalation thresholds:
- budget > 80%: slow down to report-only
- budget > 100%: pause action loop
- repeated same blocker for 3 days: force human review

Recovery:
- stale locks > 20m can be cleared
- dead_letter reviewed daily in summary
