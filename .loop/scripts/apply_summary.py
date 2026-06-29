import argparse
import json

from common import load_input_json, log_run, write_state_md


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", help="Path to summary result JSON; defaults to stdin")
    args = parser.parse_args()

    payload = load_input_json(args.input)
    notes = payload.get("top_priorities", []) + payload.get("human_actions_needed", [])
    write_state_md({"watch_list": notes, "noise": payload.get("blocked_today", [])})
    log_run(
        "summary",
        "success",
        payload.get("summary", "daily summary applied"),
        actions=[
            {"health": payload.get("health")},
            {"top_priorities": payload.get("top_priorities", [])},
            {"human_actions_needed": payload.get("human_actions_needed", [])},
        ],
    )
    print(json.dumps({"ok": True}, ensure_ascii=False))


if __name__ == "__main__":
    main()
