# AGENTS.md

Project role:
This repository is managed by a guarded sequential loop-engineering system.
All automated runs must prefer safety, minimal scope, and full auditability.

Primary goal:
- Keep repository health visible
- Resolve only low-risk, well-bounded issues automatically
- Escalate ambiguity to humans

Non-goals:
- No autonomous refactors
- No schema migrations
- No secrets handling
- No infrastructure changes
- No auto-merge

Current repository note:
- This project now has a local Git repository initialized in the workspace.
- A GitHub remote is now connected at `origin -> git@github.com:Ash-ahy/java-mastery.git`.
- PR and merge automation remain policy-disabled by default in L1 until explicitly enabled for this repo.

Low-risk changes allowed:
- formatting
- deterministic test fixes with clear root cause
- patch/minor dependency bumps
- documentation corrections
- CI/config comment updates only when explicitly allowlisted

Forbidden paths:
- src/main/resources/application-*.yml
- docker/
- .github/workflows/production*
- sql/
- deploy/
- infra/
- secrets/
- payment/
- billing/

Definition of done:
- change scope is minimal
- relevant tests pass
- verifier confirms no forbidden path touched
- work item state is persisted
- human-facing summary is written

Required commands:
- build: mvn -DskipTests package
- test: mvn test
- lint: not configured; use mvn -q -DskipTests compile as structural sanity check

Pull request policy:
- PR automation remains disabled by default in L1; enabling it requires an explicit repo policy change
- Once enabled, open PR only for low-risk approved changes
- PR title format: [loop] <short summary>
- PR body must include:
  - why this was selected
  - commands run
  - files touched
  - verifier result

Escalate immediately if:
- more than 5 files touched
- root cause unclear
- tests fail outside target scope
- requirement ambiguity exists
- forbidden path would be modified
- GitHub authentication or remote operations are required but unavailable
