import json
import os
import subprocess
import sys
from contextlib import contextmanager
from datetime import date, datetime, timezone
from pathlib import Path
from typing import Any, Iterable

ROOT = Path(__file__).resolve().parents[2]
LOOP_DIR = ROOT / ".loop"
PROMPTS_DIR = LOOP_DIR / "prompts"
SQL_DIR = LOOP_DIR / "sql"
STATE_PATH = ROOT / "STATE.md"
RUN_LOG_PATH = ROOT / "loop-run-log.md"
BUDGET_PATH = ROOT / "loop-budget.md"
AGENTS_PATH = ROOT / "AGENTS.md"
LOOP_MD_PATH = ROOT / "LOOP.md"
ENV_LOCAL_PATH = LOOP_DIR / ".env.local"


def _load_local_env() -> None:
    if not ENV_LOCAL_PATH.exists():
        return
    for raw_line in ENV_LOCAL_PATH.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip())


_load_local_env()

REPO_NAME = os.getenv("LOOP_REPO_NAME", ROOT.name)
PHASE = os.getenv("LOOP_PHASE", "L1")
RUNNER = os.getenv("LOOP_RUNNER", "hermes-local")
DATABASE_URL = os.getenv("LOOP_DATABASE_URL")


def _git_initialized() -> bool:
    try:
        result = subprocess.run(
            ["git", "-C", str(ROOT), "rev-parse", "--is-inside-work-tree"],
            capture_output=True,
            text=True,
            check=False,
        )
        return result.returncode == 0 and result.stdout.strip() == "true"
    except Exception:
        return False


def _git_remote_configured() -> bool:
    if not _git_initialized():
        return False
    try:
        result = subprocess.run(
            ["git", "-C", str(ROOT), "remote"],
            capture_output=True,
            text=True,
            check=False,
        )
        return result.returncode == 0 and bool(result.stdout.strip())
    except Exception:
        return False


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


def read_text(path: Path, default: str = "") -> str:
    return path.read_text(encoding="utf-8") if path.exists() else default


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def load_input_json(arg_path: str | None) -> Any:
    if arg_path:
        return json.loads(Path(arg_path).read_text(encoding="utf-8"))
    return json.load(sys.stdin)


def _normalize_json_value(value: Any) -> Any:
    if isinstance(value, str):
        try:
            return json.loads(value)
        except Exception:
            return value
    return value


def _import_driver():
    try:
        import psycopg  # type: ignore

        return "psycopg", psycopg
    except Exception:
        try:
            import psycopg2  # type: ignore
            import psycopg2.extras  # type: ignore

            return "psycopg2", psycopg2
        except Exception as exc:
            raise RuntimeError(
                "Missing PostgreSQL driver. Install one of: pip install psycopg[binary] OR pip install psycopg2-binary"
            ) from exc


@contextmanager
def get_conn():
    if not DATABASE_URL:
        raise RuntimeError("LOOP_DATABASE_URL is not set")
    _driver_name, driver = _import_driver()
    conn = driver.connect(DATABASE_URL)
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def fetch_all(query: str, params: Iterable[Any] | None = None) -> list[dict[str, Any]]:
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(query, params or ())
            rows = cur.fetchall()
            cols = [desc[0] for desc in cur.description]
    result: list[dict[str, Any]] = []
    for row in rows:
        item = dict(zip(cols, row))
        for key, value in list(item.items()):
            item[key] = _normalize_json_value(value)
        result.append(item)
    return result


def fetch_one(query: str, params: Iterable[Any] | None = None) -> dict[str, Any] | None:
    rows = fetch_all(query, params)
    return rows[0] if rows else None


def execute(query: str, params: Iterable[Any] | None = None) -> None:
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(query, params or ())


def execute_returning(query: str, params: Iterable[Any] | None = None) -> dict[str, Any] | None:
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(query, params or ())
            if cur.description is None:
                return None
            row = cur.fetchone()
            if row is None:
                return None
            cols = [desc[0] for desc in cur.description]
            item = dict(zip(cols, row))
            for key, value in list(item.items()):
                item[key] = _normalize_json_value(value)
            return item


def ensure_daily_budgets() -> None:
    defaults = [
        ("tokens", 500000, "tokens"),
        ("action_runs", 5, "count"),
        ("auto_prs", 2, "count"),
        ("alerts", 10, "count"),
    ]
    with get_conn() as conn:
        with conn.cursor() as cur:
            for metric, limit_value, unit in defaults:
                cur.execute(
                    """
                    insert into budgets (budget_date, repo, metric, limit_value, used_value, unit)
                    values (current_date, %s, %s, %s, 0, %s)
                    on conflict (budget_date, repo, metric) do nothing
                    """,
                    (REPO_NAME, metric, limit_value, unit),
                )


def get_budget_snapshot() -> list[dict[str, Any]]:
    ensure_daily_budgets()
    return fetch_all(
        """
        select metric, limit_value, used_value, unit
        from budgets
        where budget_date = current_date and repo = %s
        order by metric
        """,
        (REPO_NAME,),
    )


def increment_budget(metric: str, amount: float = 1) -> None:
    ensure_daily_budgets()
    execute(
        """
        update budgets
        set used_value = used_value + %s
        where budget_date = current_date and repo = %s and metric = %s
        """,
        (amount, REPO_NAME, metric),
    )


def append_run_log_markdown(
    run_type: str,
    status: str,
    summary: str,
    work_item_id: str | None,
    actions: list[Any],
    error_message: str | None,
) -> None:
    stamp = utc_now().strftime("%Y-%m-%d %H:%M UTC")
    lines = [f"[{stamp}] {run_type.upper()} {status}"]
    if work_item_id:
        lines.append(f"- work item: {work_item_id}")
    if summary:
        lines.append(f"- summary: {summary}")
    if actions:
        lines.append(f"- actions: {json.dumps(actions, ensure_ascii=False)}")
    if error_message:
        lines.append(f"- error: {error_message}")
    existing = read_text(RUN_LOG_PATH, "# loop-run-log.md\n")
    if not existing.endswith("\n"):
        existing += "\n"
    write_text(RUN_LOG_PATH, existing + "\n" + "\n".join(lines) + "\n")


def log_run(
    run_type: str,
    status: str,
    summary: str,
    work_item_id: str | None = None,
    actions: list[Any] | None = None,
    error_message: str | None = None,
    tokens_in: int = 0,
    tokens_out: int = 0,
    estimated_cost: float = 0.0,
) -> None:
    actions_json = json.dumps(actions or [], ensure_ascii=False)
    execute(
        """
        insert into run_log (
          run_type, repo, work_item_id, started_at, ended_at, status, actor,
          summary, actions, tokens_in, tokens_out, estimated_cost, error_message
        ) values (%s, %s, %s, now(), now(), %s, %s, %s, %s::jsonb, %s, %s, %s, %s)
        """,
        (
            run_type,
            REPO_NAME,
            work_item_id,
            status,
            RUNNER,
            summary,
            actions_json,
            tokens_in,
            tokens_out,
            estimated_cost,
            error_message,
        ),
    )
    append_run_log_markdown(run_type, status, summary, work_item_id, actions or [], error_message)


def current_mode() -> dict[str, Any]:
    pause_row = fetch_one("select control_value from loop_control where control_key = %s", ("pause_all",))
    phase_row = fetch_one("select control_value from loop_control where control_key = %s", ("current_phase",))
    pause_value = _normalize_json_value((pause_row or {}).get("control_value", {}))
    phase_value = _normalize_json_value((phase_row or {}).get("control_value", {}))
    pause_all = bool(pause_value.get("value", False)) if isinstance(pause_value, dict) else False
    phase = str(phase_value.get("value", PHASE)) if isinstance(phase_value, dict) else PHASE
    git_initialized = _git_initialized()
    git_remote_configured = _git_remote_configured()
    return {
        "pause_all": pause_all,
        "phase": phase,
        "git_initialized": git_initialized,
        "git_remote_configured": git_remote_configured,
        "git_integration": git_initialized,
        "executor": "sequential",
        "auto_pr": False,
        "auto_merge": False,
    }


def write_state_md(extra: dict[str, Any] | None = None) -> None:
    extra = extra or {}
    mode = current_mode()
    budgets = {row["metric"]: row for row in get_budget_snapshot()}
    active = fetch_all(
        """
        select id, title, priority, status, triage_reason, last_action, next_run_at
        from work_items
        where repo = %s and status in ('triaged', 'in_progress', 'verifying', 'waiting_human', 'blocked', 'retry_later')
        order by priority asc, created_at asc
        limit 20
        """,
        (REPO_NAME,),
    )
    high_priority = [row for row in active if row["priority"] <= 1 and row["status"] != "waiting_human"]
    in_progress = [row for row in active if row["status"] in ("in_progress", "verifying")]
    waiting_human = [row for row in active if row["status"] == "waiting_human"]

    if extra.get("watch_list") is None:
        watch_list = [
            row["title"]
            for row in active
            if row["status"] in ("triaged", "retry_later", "blocked") and row["priority"] >= 2
        ]
    else:
        watch_list = extra.get("watch_list") or []

    if extra.get("noise") is None:
        noise = []
    else:
        noise = extra.get("noise") or []

    last_runs = fetch_all(
        """
        select run_type, status, ended_at
        from run_log
        where repo = %s
        order by ended_at desc nulls last
        limit 20
        """,
        (REPO_NAME,),
    )

    def latest(run_type: str) -> str:
        for row in last_runs:
            if row["run_type"] == run_type and row["ended_at"] is not None:
                value = row["ended_at"]
                try:
                    return value.strftime("%Y-%m-%d %H:%M UTC")
                except Exception:
                    return str(value)
        return "not yet executed"

    loop_status = "PAUSED" if mode["pause_all"] else "ACTIVE"
    auto_pr = "enabled" if mode["auto_pr"] else "disabled"
    auto_merge = "enabled" if mode["auto_merge"] else "disabled"
    pause_all_text = str(mode["pause_all"]).lower()
    git_line = (
        "- Git integration: enabled locally (git initialized, no remote configured)"
        if mode.get("git_initialized") and not mode.get("git_remote_configured")
        else "- Git integration: enabled"
        if mode.get("git_initialized") and mode.get("git_remote_configured")
        else "- Git integration: disabled (local workspace not initialized as git repo)"
    )
    token_used = int(float(budgets.get("tokens", {}).get("used_value", 0)))
    token_limit = int(float(budgets.get("tokens", {}).get("limit_value", 500000)))
    action_used = int(float(budgets.get("action_runs", {}).get("used_value", 0)))
    action_limit = int(float(budgets.get("action_runs", {}).get("limit_value", 5)))
    pr_used = int(float(budgets.get("auto_prs", {}).get("used_value", 0)))
    pr_limit = int(float(budgets.get("auto_prs", {}).get("limit_value", 2)))

    lines = [
        "# STATE.md",
        "",
        f"Loop status: {loop_status}",
        f"Last triage run: {latest('triage')}",
        f"Last action run: {latest('action')}",
        f"Last verifier run: {latest('verify')}",
        f"Last summary run: {latest('summary')}",
        "",
        "Current mode:",
        f"- Phase: {mode['phase']}",
        f"- Executor: {mode['executor']}",
        f"- Auto PR: {auto_pr}",
        f"- Auto merge: {auto_merge}",
        git_line,
        "",
        "## High Priority",
    ]

    if high_priority:
        for row in high_priority:
            lines.append(f"- [P{row['priority']}] {row['title']}")
            lines.append(f"  Work item: {row['id']}")
            lines.append(f"  Status: {row['status']}")
            if row.get("triage_reason"):
                lines.append(f"  Reason: {row['triage_reason']}")
    else:
        lines.append("- No active high-priority work items.")

    lines.extend(["", "## In Progress"])
    if in_progress:
        for row in in_progress:
            lines.append(f"- Work item: {row['id']}")
            lines.append(f"  Title: {row['title']}")
            lines.append(f"  Status: {row['status']}")
            if row.get("last_action"):
                lines.append(f"  Last action: {row['last_action']}")
    else:
        lines.append("- None.")

    lines.extend(["", "## Waiting Human"])
    if waiting_human:
        for row in waiting_human:
            lines.append(f"- Work item: {row['id']}")
            lines.append(f"  Title: {row['title']}")
            if row.get("triage_reason"):
                lines.append(f"  Why escalated: {row['triage_reason']}")
    else:
        lines.append("- None.")

    lines.extend(["", "## Watch List"])
    if watch_list:
        lines.extend([f"- {item}" for item in watch_list])
    else:
        lines.append("- No additional watch items.")

    lines.extend(["", "## Recent Noise"])
    if noise:
        lines.extend([f"- {item}" for item in noise])
    else:
        lines.append("- None recorded.")

    lines.extend(
        [
            "",
            "## Budget Snapshot",
            f"- tokens today: {token_used} / {token_limit}",
            f"- action runs today: {action_used} / {action_limit}",
            f"- auto PRs today: {pr_used} / {pr_limit}",
            "- consecutive failed runs: tracked via run_log query / maintenance loop",
            "",
            "## Kill Switches",
            f"- pause_all: {pause_all_text}",
            "- repo_pause_label: false",
            "",
            "## Notes",
            "- This dashboard is regenerated from PostgreSQL state by .loop/scripts/common.py.",
        ]
    )

    write_text(STATE_PATH, "\n".join(lines).rstrip() + "\n")


def seed_incoming_event(source: str, event_type: str, external_event_id: str, dedupe_key: str, payload: dict[str, Any]) -> None:
    execute(
        """
        insert into incoming_events(source, event_type, repo, external_event_id, dedupe_key, payload)
        values (%s, %s, %s, %s, %s, %s::jsonb)
        on conflict (dedupe_key) do nothing
        """,
        (source, event_type, REPO_NAME, external_event_id, dedupe_key, json.dumps(payload, ensure_ascii=False)),
    )
