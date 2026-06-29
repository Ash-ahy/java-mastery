import json

from common import REPO_NAME, RUNNER, current_mode, ensure_daily_budgets, execute_returning, log_run, write_state_md


def main() -> None:
    ensure_daily_budgets()
    mode = current_mode()
    if mode["pause_all"]:
        log_run("maintenance", "paused", "pause_all is true; no item selected")
        print(json.dumps({"selected": None, "reason": "pause_all"}, ensure_ascii=False))
        return

    row = execute_returning(
        """
        update work_items
        set status = 'in_progress',
            locked_by = %s,
            locked_at = now(),
            attempts = attempts + 1,
            last_action = 'claimed for action loop'
        where id = (
          select id
          from work_items
          where repo = %s
            and status in ('triaged', 'retry_later')
            and risk_level = 'low'
            and next_run_at <= now()
          order by priority asc, created_at asc
          limit 1
          for update skip locked
        )
        returning id, title, priority, risk_level, status, source, source_ref, dedupe_key, triage_reason, payload
        """,
        (RUNNER, REPO_NAME),
    )
    write_state_md()
    if not row:
        print(json.dumps({"selected": None, "reason": "no_eligible_item"}, ensure_ascii=False))
        return
    log_run("action", "running", f"Selected work item {row['id']}", work_item_id=row["id"])
    print(json.dumps({"selected": row}, ensure_ascii=False, default=str, indent=2))


if __name__ == "__main__":
    main()
