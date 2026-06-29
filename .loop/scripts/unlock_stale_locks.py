import json

from common import REPO_NAME, execute, fetch_all, log_run, write_state_md


def main() -> None:
    stale_items = fetch_all(
        """
        select id, status, locked_by, locked_at
        from work_items
        where repo = %s and locked_at is not null and locked_at < now() - interval '20 minutes'
        """,
        (REPO_NAME,),
    )
    stale_locks = fetch_all(
        """
        select lock_name, owner, expires_at
        from locks
        where expires_at < now()
        """
    )

    for item in stale_items:
        execute(
            """
            update work_items
            set status = case when status = 'verifying' then 'retry_later' else 'triaged' end,
                locked_by = null,
                locked_at = null,
                last_error = 'stale lock recovered by maintenance loop'
            where id = %s
            """,
            (item["id"],),
        )

    for lock in stale_locks:
        execute("delete from locks where lock_name = %s", (lock["lock_name"],))

    write_state_md()
    log_run(
        "maintenance",
        "success",
        "stale lock cleanup complete",
        actions=[
            {"recovered_work_items": len(stale_items)},
            {"deleted_locks": len(stale_locks)},
        ],
    )
    print(
        json.dumps(
            {
                "ok": True,
                "recovered_work_items": len(stale_items),
                "deleted_locks": len(stale_locks),
            },
            ensure_ascii=False,
        )
    )


if __name__ == "__main__":
    main()
