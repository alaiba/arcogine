#!/usr/bin/env python3
"""Fail when an Accepted/Superseded ADR body is rewritten.

Accepted ADRs are historical records. After acceptance, only the metadata
needed to mark an ADR Superseded may change in place. Decision/body changes
must be recorded in a new Accepted ADR that declares `Supersedes: ADR-NNNN`.
"""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import sys
from typing import Iterable

ADR_DIR = "docs/architecture/decisions"
ADR_NAME = re.compile(r"^(\d{4})-[^/]+\.md$")
STATUS = re.compile(r"^Status:[ \t]*(\S+)[ \t]*$", re.MULTILINE)
SUPERSEDED_BY = re.compile(r"^Superseded by:[ \t]*(ADR-\d{4})[ \t]*$", re.MULTILINE)
SUPERSEDES = re.compile(r"^Supersedes:[ \t]*(ADR-\d{4})[ \t]*$", re.MULTILINE)
ALLOWED_MUTABLE_METADATA = re.compile(
    r"^(?:Status:[ \t]*\S+[ \t]*|Superseded by:[ \t]*ADR-\d{4}[ \t]*)(?:\r?\n|$)",
    re.MULTILINE,
)
SECTION_HEADING = re.compile(r"^##\s", re.MULTILINE)
HEADER_METADATA = (
    ("Status", STATUS),
    ("Superseded by", SUPERSEDED_BY),
    ("Supersedes", SUPERSEDES),
)


def run_git(*args: str, check: bool = True) -> str:
    result = subprocess.run(
        ["git", *args],
        check=check,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return result.stdout


def metadata_header(text: str) -> str:
    """Return only the ADR header area before the first level-two section."""
    match = SECTION_HEADING.search(text)
    return text if match is None else text[: match.start()]


def duplicate_header_metadata(text: str) -> list[str]:
    """Return singular metadata field names that occur more than once in the ADR header."""
    header = metadata_header(text)
    return [name for name, pattern in HEADER_METADATA if len(pattern.findall(header)) > 1]


def status(text: str) -> str | None:
    match = STATUS.search(metadata_header(text))
    return match.group(1) if match else None


def superseded_by(text: str) -> str | None:
    match = SUPERSEDED_BY.search(metadata_header(text))
    return match.group(1) if match else None


def supersedes(text: str) -> str | None:
    match = SUPERSEDES.search(metadata_header(text))
    return match.group(1) if match else None


def immutable_body(text: str) -> str:
    match = SECTION_HEADING.search(text)
    if match is None:
        return ALLOWED_MUTABLE_METADATA.sub("", text)
    header = ALLOWED_MUTABLE_METADATA.sub("", text[: match.start()])
    return header + text[match.start() :]


def adr_id(path: str) -> str:
    name = Path(path).name
    match = ADR_NAME.match(name)
    if not match:
        raise ValueError(path)
    return f"ADR-{match.group(1)}"


def list_base_adrs(base_ref: str) -> list[str]:
    output = run_git("ls-tree", "-r", "--name-only", base_ref, ADR_DIR)
    paths: list[str] = []
    for path in output.splitlines():
        name = Path(path).name
        match = ADR_NAME.match(name)
        if match and match.group(1) != "0000":
            paths.append(path)
    return sorted(paths)


def read_base(base_ref: str, path: str) -> str:
    return run_git("show", f"{base_ref}:{path}")


def read_current(path: str) -> str | None:
    file_path = Path(path)
    if not file_path.exists():
        return None
    return file_path.read_text(encoding="utf-8")


def current_new_adrs(base_paths: set[str]) -> dict[str, str]:
    found: dict[str, str] = {}
    root = Path(ADR_DIR)
    if not root.exists():
        return found
    for path in sorted(root.glob("[0-9][0-9][0-9][0-9]-*.md")):
        relative = path.as_posix()
        if relative in base_paths or path.name.startswith("0000-"):
            continue
        found[adr_id(relative)] = path.read_text(encoding="utf-8")
    return found


def validate(base_ref: str) -> list[str]:
    errors: list[str] = []
    base_paths = list_base_adrs(base_ref)
    base_path_set = set(base_paths)
    new_adrs = current_new_adrs(base_path_set)

    for path in base_paths:
        before = read_base(base_ref, path)
        before_status = status(before)
        if before_status not in {"Accepted", "Superseded"}:
            continue

        after = read_current(path)
        if after is None:
            errors.append(f"{path}: accepted ADRs may not be deleted or renamed")
            continue

        duplicates = duplicate_header_metadata(after)
        if duplicates:
            errors.append(
                f"{path}: duplicate ADR header metadata is not allowed: {', '.join(duplicates)}"
            )
            continue

        if immutable_body(before) != immutable_body(after):
            errors.append(
                f"{path}: {before_status} ADR body changed; create a superseding ADR instead"
            )
            continue

        after_status = status(after)
        if before_status == "Superseded":
            if after_status != "Superseded":
                errors.append(f"{path}: Superseded status is immutable")
            if superseded_by(before) != superseded_by(after):
                errors.append(f"{path}: existing Superseded-by metadata is immutable")
            continue

        # Accepted ADR: unchanged Accepted metadata is fine. The only allowed
        # transition is Accepted -> Superseded with a matching new Accepted ADR.
        if after_status == "Accepted":
            if superseded_by(after) is not None:
                errors.append(
                    f"{path}: Accepted ADR cannot declare Superseded by without Status: Superseded"
                )
            continue

        if after_status != "Superseded":
            errors.append(
                f"{path}: Accepted ADR status may only remain Accepted or become Superseded"
            )
            continue

        target = superseded_by(after)
        if target is None:
            errors.append(f"{path}: Superseded ADR must declare `Superseded by: ADR-NNNN`")
            continue

        new_text = new_adrs.get(target)
        if new_text is None:
            errors.append(
                f"{path}: {target} must be a new ADR in the same change when superseding {adr_id(path)}"
            )
            continue

        new_duplicates = duplicate_header_metadata(new_text)
        if new_duplicates:
            errors.append(
                f"{target}: duplicate ADR header metadata is not allowed: {', '.join(new_duplicates)}"
            )
            continue

        if status(new_text) != "Accepted":
            errors.append(
                f"{target}: superseding ADR must be `Status: Accepted` before {adr_id(path)} can become Superseded"
            )
            continue

        expected = adr_id(path)
        if supersedes(new_text) != expected:
            errors.append(
                f"{target}: must declare `Supersedes: {expected}` when replacing {path}"
            )

    return errors


def base_from_ci_event() -> str | None:
    event_path = os.environ.get("GITHUB_EVENT_PATH")
    event_name = os.environ.get("GITHUB_EVENT_NAME")
    if not event_path or not event_name:
        return None

    event = json.loads(Path(event_path).read_text(encoding="utf-8"))
    if event_name == "pull_request":
        return event.get("pull_request", {}).get("base", {}).get("sha")
    if event_name == "push":
        before = event.get("before")
        if before and set(before) != {"0"}:
            return before
    return None


def main(argv: Iterable[str] | None = None) -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-ref", help="Git ref/commit containing the historical ADRs")
    parser.add_argument(
        "--ci",
        action="store_true",
        help="derive the base commit from GITHUB_EVENT_PATH; skip non-diff CI events",
    )
    args = parser.parse_args(argv)

    base_ref = args.base_ref
    if args.ci:
        base_ref = base_from_ci_event()
        if base_ref is None:
            print("ADR immutability: no comparable CI base for this event; skipped")
            return 0

    if not base_ref:
        parser.error("provide --base-ref or --ci")

    errors = validate(base_ref)
    if errors:
        print("ADR immutability check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(f"ADR immutability check passed against {base_ref}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
