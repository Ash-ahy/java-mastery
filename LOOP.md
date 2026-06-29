# LOOP.md

System name:
Guarded Sequential Agent Loop

Mission:
Operate this repository as a safe, persistent, auditable agent loop.
Prefer visibility first, then low-risk action, then guarded automation.

Current local reality:
- The local workspace now has Git initialized and a GitHub remote connected.
- Remote `master` now contains the current local bootstrap baseline commit.
- PostgreSQL is the source of truth for loop state.
- STATE.md is the human dashboard.
- One active work item at a time.
- Maker/checker separation is mandatory.
- Remote Git operations are now available, but PR/merge automation remains off by default in L1.

Operating model:
- fresh-run sessions, never immortal chats
- PostgreSQL is the source of truth
- STATE.md is the human dashboard
- one active work item at a time
- sequential execution only, no parallel swarm
- maker/checker separation required

Active loops:

1. Triage Loop
- cadence: every 2 hours
- goal: normalize external signals into prioritized work items
- output: updated work_items + STATE.md
- phase: always enabled

2. Action Loop
- trigger: only when a low-risk triaged item is selected
- goal: execute the smallest safe action
- output: patch/comment/notes
- phase: enabled only after L1 proves stable
- current local restriction: PR/merge automation remains policy-disabled by default

3. Verifier Loop
- trigger: after every action attempt
- goal: independently validate scope, tests, risk, and completion
- output: approve / reject / escalate
- phase: mandatory for any code change

4. Daily Summary Loop
- cadence: every weekday at 09:00
- goal: compress the last 24h into a human-readable operations summary
- output: STATE.md summary + loop-run-log.md append

5. Maintenance Loop
- cadence: every 6 hours
- goal: clear stale locks, move exhausted items to dead_letter, reconcile external state

Priority rules:
- P0: default branch CI failure
- P1: PR blockers
- P2: high-priority issues
- P3: dependency/doc/cleanup

Risk policy:
- low: may act if within allowlist
- medium: report and require human approval
- high: human only
- critical: pause loop and escalate

Human gates:
- any forbidden path
- any medium/high/critical risk
- more than 3 attempts on same item
- more than 5 files changed
- unclear root cause
- test failures outside target scope
- any action that needs GitHub auth, branching/worktrees, or PR automation beyond current repo policy

Denylist:
- auth
- secrets
- prod infra
- billing/payment
- db migrations
- production deploy workflows
- application-docker.yml
- application.yml

Budgets:
- max 5 action runs/day
- max 2 auto PRs/day
- max 1 active work item at a time
- max run time 15 minutes/run
- max 3 attempts/item
- pause on 3 consecutive failed runs

Kill switches:
- loop_control.pause_all = true
- repo label: loop-pause-all
- STATE.md header pause flag

Notification policy:
- notify humans only on:
  - need_human
  - budget breach
  - pause triggered
  - high-priority new blocker
- batch all non-critical items into daily summary

Recovery policy:
- stale locks older than 20m are recoverable
- interrupted runs must not leave item in undefined state
- all changes must be persisted before exit
