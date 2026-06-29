import json

from common import REPO_NAME, STATE_PATH, ensure_daily_budgets, fetch_all, get_budget_snapshot, read_text

ensure_daily_budgets()

payload = {
    "repo": REPO_NAME,
    "state_md": read_text(STATE_PATH),
    "budget_snapshot": get_budget_snapshot(),
    "runs_last_24h": fetch_all(
        """
        select run_type, status, summary, started_at, ended_at, error_message
        from run_log
        where repo = %s and started_at >= now() - interval '24 hour'
        order by started_at desc
        """,
        (REPO_NAME,),
    ),
    "open_items": fetch_all(
        """
        select id, title, priority, risk_level, status, attempts, triage_reason
        from work_items
        where repo = %s and status not in ('done', 'dead_letter')
        order by priority asc, created_at asc
        """,
        (REPO_NAME,),
    ),
    "resolved_last_24h": fetch_all(
        """
        select id, title, completed_at
        from work_items
        where repo = %s and completed_at >= now() - interval '24 hour'
        order by completed_at desc
        """,
        (REPO_NAME,),
    ),
}

print(json.dumps(payload, ensure_ascii=False, default=str, indent=2))
