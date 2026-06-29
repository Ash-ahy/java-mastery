import argparse
import json

from common import REPO_NAME, ensure_daily_budgets, execute, load_input_json, log_run, write_state_md


def upsert_new_item(item: dict) -> None:
    execute(
        """
        insert into work_items (
          repo, source, source_ref, dedupe_key, title, body, priority, risk_level,
          status, recommended_action, triage_reason, payload
        ) values (%s, %s, %s, %s, %s, %s, %s, %s, 'triaged', %s, %s, %s::jsonb)
        on conflict (dedupe_key) do update set
          title = excluded.title,
          body = excluded.body,
          priority = excluded.priority,
          risk_level = excluded.risk_level,
          recommended_action = excluded.recommended_action,
          triage_reason = excluded.triage_reason,
          payload = excluded.payload,
          status = case when work_items.status in ('done', 'dead_letter') then work_items.status else 'triaged' end,
          next_run_at = now()
        """,
        (
            REPO_NAME,
            item.get("source", "triage"),
            item.get("source_ref"),
            item["dedupe_key"],
            item["title"],
            item.get("body"),
            int(item.get("priority", 3)),
            item.get("risk_level", "medium"),
            item.get("recommended_action", "report"),
            item.get("triage_reason"),
            json.dumps(item.get("payload", {}), ensure_ascii=False),
        ),
    )


def apply_update(item: dict) -> None:
    payload = json.dumps(item.get("payload", {}), ensure_ascii=False)
    execute(
        """
        update work_items
        set status = %s,
            priority = %s,
            risk_level = %s,
            recommended_action = %s,
            triage_reason = %s,
            payload = coalesce(payload, '{}'::jsonb) || %s::jsonb,
            next_run_at = now()
        where repo = %s and dedupe_key = %s
        """,
        (
            item.get("status", "triaged"),
            int(item.get("priority", 3)),
            item.get("risk_level", "medium"),
            item.get("recommended_action", "report"),
            item.get("triage_reason"),
            payload,
            REPO_NAME,
            item["dedupe_key"],
        ),
    )


def mark_events_processed() -> None:
    execute(
        """
        update incoming_events
        set status = 'normalized', processed_at = now(), error_message = null
        where repo = %s and status = 'new'
        """,
        (REPO_NAME,),
    )


def maybe_flag_pause(pause_recommended: bool, summary: str) -> None:
    if not pause_recommended:
        return
    execute(
        """
        insert into loop_control(control_key, control_value)
        values ('pause_all', %s::jsonb)
        on conflict (control_key) do update set control_value = excluded.control_value, updated_at = now()
        """,
        (json.dumps({"value": True, "reason": summary}, ensure_ascii=False),),
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", help="Path to triage result JSON; defaults to stdin")
    args = parser.parse_args()

    ensure_daily_budgets()
    payload = load_input_json(args.input)
    for item in payload.get("new_items", []):
        upsert_new_item(item)
    for item in payload.get("updates", []):
        apply_update(item)
    mark_events_processed()
    maybe_flag_pause(bool(payload.get("pause_recommended", False)), payload.get("summary", "pause requested"))
    write_state_md(
        {
            "watch_list": payload.get("watch_list", []),
            "noise": payload.get("noise", []),
        }
    )
    log_run(
        "triage",
        "paused" if payload.get("pause_recommended") else "success",
        payload.get("summary", "triage applied"),
        actions=[
            {"new_items": len(payload.get("new_items", []))},
            {"updates": len(payload.get("updates", []))},
            {"selected_next_item_dedupe_key": payload.get("selected_next_item_dedupe_key")},
        ],
    )
    print(json.dumps({"ok": True, "repo": REPO_NAME}, ensure_ascii=False))


if __name__ == "__main__":
    main()
