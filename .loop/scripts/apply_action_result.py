import argparse
import json

from common import REPO_NAME, ensure_daily_budgets, increment_budget, execute, load_input_json, log_run, write_state_md


def map_status(decision: str) -> str:
    return {
        "done": "verifying",
        "blocked": "blocked",
        "need_human": "waiting_human",
        "retry_later": "retry_later",
    }.get(decision, "blocked")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", help="Path to action result JSON; defaults to stdin")
    args = parser.parse_args()

    ensure_daily_budgets()
    payload = load_input_json(args.input)
    status = map_status(payload["decision"])
    work_item_id = payload["work_item_id"]
    notes = payload.get("notes")
    execute(
        """
        update work_items
        set status = %s,
            last_action = %s,
            payload = coalesce(payload, '{}'::jsonb) || %s::jsonb,
            next_run_at = case when %s in ('retry_later', 'blocked') then now() + interval '2 hour' else now() end,
            locked_by = null,
            locked_at = null
        where id = %s and repo = %s
        """,
        (
            status,
            notes,
            json.dumps(payload, ensure_ascii=False),
            status,
            work_item_id,
            REPO_NAME,
        ),
    )
    increment_budget("action_runs", 1)
    write_state_md()
    log_run(
        "action",
        "success" if status == "verifying" else "partial",
        notes or "action result applied",
        work_item_id=work_item_id,
        actions=[
            {"decision": payload["decision"]},
            {"files_touched": payload.get("files_touched", [])},
            {"commands_run": payload.get("commands_run", [])},
        ],
    )
    print(json.dumps({"ok": True, "work_item_id": work_item_id, "status": status}, ensure_ascii=False))


if __name__ == "__main__":
    main()
