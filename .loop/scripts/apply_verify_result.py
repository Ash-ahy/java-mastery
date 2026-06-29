import argparse
import json

from common import REPO_NAME, ensure_daily_budgets, execute, load_input_json, log_run, write_state_md


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", help="Path to verify result JSON; defaults to stdin")
    args = parser.parse_args()

    ensure_daily_budgets()
    payload = load_input_json(args.input)
    work_item_id = payload["work_item_id"]
    next_status = payload.get("next_status", "waiting_human")
    summary = "; ".join(payload.get("reasons", [])) or payload.get("verdict", "verify complete")
    execute(
        """
        update work_items
        set status = %s,
            last_action = %s,
            last_error = case when %s = 'approve' then null else %s end,
            completed_at = case when %s = 'done' then now() else completed_at end,
            locked_by = null,
            locked_at = null,
            payload = coalesce(payload, '{}'::jsonb) || %s::jsonb
        where id = %s and repo = %s
        """,
        (
            next_status,
            summary,
            payload.get("verdict"),
            summary,
            next_status,
            json.dumps(payload, ensure_ascii=False),
            work_item_id,
            REPO_NAME,
        ),
    )
    write_state_md()
    log_run(
        "verify",
        "success" if payload.get("verdict") == "approve" else "partial",
        summary,
        work_item_id=work_item_id,
        actions=[
            {"verdict": payload.get("verdict")},
            {"next_status": next_status},
            {"tests_ok": payload.get("tests_ok")},
        ],
    )
    print(json.dumps({"ok": True, "work_item_id": work_item_id, "status": next_status}, ensure_ascii=False))


if __name__ == "__main__":
    main()
