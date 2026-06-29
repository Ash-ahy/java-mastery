import json

from common import REPO_NAME, execute, fetch_all, log_run, write_state_md


def main() -> None:
    events = fetch_all(
        """
        select id, event_type, payload
        from incoming_events
        where repo = %s and status = 'normalized'
        order by received_at asc
        limit 100
        """,
        (REPO_NAME,),
    )
    closed = 0
    for event in events:
        payload = event.get("payload") or {}
        if not isinstance(payload, dict):
            continue
        dedupe_key = payload.get("dedupe_key")
        state = payload.get("state") or payload.get("status")
        if dedupe_key and state in ("closed", "merged", "success", "resolved"):
            execute(
                """
                update work_items
                set status = 'done', completed_at = now(), last_action = %s
                where repo = %s and dedupe_key = %s and status not in ('done', 'dead_letter')
                """,
                (f"reconciled from normalized event {event['id']}", REPO_NAME, dedupe_key),
            )
            closed += 1
    write_state_md()
    log_run("maintenance", "success", f"reconciled normalized events; closed={closed}", actions=[{"closed": closed}])
    print(json.dumps({"ok": True, "closed": closed}, ensure_ascii=False))


if __name__ == "__main__":
    main()
