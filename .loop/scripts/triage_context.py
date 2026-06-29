import json

from common import (
    AGENTS_PATH,
    LOOP_MD_PATH,
    REPO_NAME,
    STATE_PATH,
    current_mode,
    ensure_daily_budgets,
    fetch_all,
    get_budget_snapshot,
    read_text,
)

ensure_daily_budgets()

context = {
    "repo": REPO_NAME,
    "mode": current_mode(),
    "state_md": read_text(STATE_PATH),
    "loop_md": read_text(LOOP_MD_PATH),
    "agents_md": read_text(AGENTS_PATH),
    "budget_snapshot": get_budget_snapshot(),
    "incoming_events": fetch_all(
        """
        select id, source, event_type, external_event_id, dedupe_key, payload, received_at, status
        from incoming_events
        where repo = %s and status = 'new'
        order by received_at asc
        limit 100
        """,
        (REPO_NAME,),
    ),
    "open_work_items": fetch_all(
        """
        select id, source, source_ref, dedupe_key, title, priority, risk_level, status,
               recommended_action, attempts, max_attempts, next_run_at, triage_reason, payload
        from work_items
        where repo = %s and status in ('new', 'triaged', 'in_progress', 'verifying', 'blocked', 'retry_later', 'waiting_human')
        order by priority asc, created_at asc
        limit 100
        """,
        (REPO_NAME,),
    ),
    "recent_runs": fetch_all(
        """
        select run_type, status, summary, started_at, ended_at, error_message
        from run_log
        where repo = %s
        order by started_at desc
        limit 20
        """,
        (REPO_NAME,),
    ),
}

print(json.dumps(context, ensure_ascii=False, default=str, indent=2))
