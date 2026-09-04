#!/usr/bin/env python3
"""Protect accepted ADR decisions while permitting audited editorial amendments.

Accepted and Superseded ADRs remain historical semantic decision records. A semantic decision change
still requires a new Accepted ADR that declares `Supersedes: ADR-NNNN`. An in-place prose/title or
filename clarification is permitted only when the PR adds explicit `Amendment:` metadata declaring
that the edit makes no semantic change. CI can enforce that process evidence; independent review
remains responsible for verifying the semantic-equivalence claim.
"""

from __future__ import annotations

import argparse
from collections import Counter
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
AMENDMENT = re.compile(r"^Amendment:[ \t]*(.+?)[ \t]*$", re.MULTILINE)
ALLOWED_MUTABLE_METADATA = re.compile(
    r"^(?:Status:[ \t]*\S+[ \t]*|Superseded by:[ \t]*ADR-\d{4}[ \t]*|Amendment:[ \t]*.+?[ \t]*)(?:\r?\n|$)",
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


def amendments(text: str) -> list[str]:
    return AMENDMENT.findall(metadata_header(text))


def protected_body(text: str) -> str:
    """Return ADR content excluding mutable status/supersession/amendment metadata."""
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


def current_adrs_by_id() -> tuple[dict[str, tuple[str, str]], list[str]]:
    found: dict[str, tuple[str, str]] = {}
    duplicates: list[str] = []
    root = Path(ADR_DIR)
    if not root.exists():
        return found, duplicates
    for path in sorted(root.glob("[0-9][0-9][0-9][0-9]-*.md")):
        if path.name.startswith("0000-"):
            continue
        relative = path.as_posix()
        identifier = adr_id(relative)
        if identifier in found:
            duplicates.append(identifier)
            continue
        found[identifier] = (relative, path.read_text(encoding="utf-8"))
    return found, duplicates


def new_amendments(before: str, after: str) -> tuple[list[str], list[str]]:
    before_counter = Counter(amendments(before))
    after_counter = Counter(amendments(after))
    removed = list((before_counter - after_counter).elements())
    added = list((after_counter - before_counter).elements())
    return removed, added


def valid_editorial_amendment(entry: str) -> bool:
    return "no semantic change" in entry.casefold()


def validate(base_ref: str) -> list[str]:
    errors: list[str] = []
    base_paths = list_base_adrs(base_ref)
    base_ids = {adr_id(path) for path in base_paths}
    current_adrs, duplicate_ids = current_adrs_by_id()
    for identifier in duplicate_ids:
        errors.append(f"{identifier}: ADR number appears in more than one current filename")

    new_adrs = {
        identifier: text
        for identifier, (_path, text) in current_adrs.items()
        if identifier not in base_ids
    }

    for base_path in base_paths:
        identifier = adr_id(base_path)
        before = read_base(base_ref, base_path)
        before_status = status(before)
        if before_status not in {"Accepted", "Superseded"}:
            continue

        current = current_adrs.get(identifier)
        if current is None:
            errors.append(f"{base_path}: accepted ADR may not be deleted")
            continue
        after_path, after = current

        duplicates = duplicate_header_metadata(after)
        if duplicates:
            errors.append(
                f"{after_path}: duplicate ADR header metadata is not allowed: {', '.join(duplicates)}"
            )
            continue

        removed_amendments, added_amendments = new_amendments(before, after)
        if removed_amendments:
            errors.append(
                f"{after_path}: existing Amendment metadata is historical and may not be removed"
            )
            continue
        invalid_amendments = [
            entry for entry in added_amendments if not valid_editorial_amendment(entry)
        ]
        if invalid_amendments:
            errors.append(
                f"{after_path}: new Amendment metadata must explicitly declare `no semantic change`"
            )
            continue

        body_changed = protected_body(before) != protected_body(after)
        path_changed = base_path != after_path
        if (body_changed or path_changed) and not added_amendments:
            changed_what = "body/title or filename" if body_changed and path_changed else (
                "body/title" if body_changed else "filename"
            )
            errors.append(
                f"{after_path}: {before_status} ADR {changed_what} changed without an audited "
                "editorial Amendment; add `Amendment: <date> — ...; no semantic change` only for "
                "a genuinely semantics-preserving clarification, otherwise create a superseding ADR"
            )
            continue

        after_status = status(after)
        if before_status == "Superseded":
            if after_status != "Superseded":
                errors.append(f"{after_path}: Superseded status is immutable")
            if superseded_by(before) != superseded_by(after):
                errors.append(f"{after_path}: existing Superseded-by metadata is immutable")
            continue

        # Accepted ADR: unchanged Accepted status is fine, including a reviewed editorial amendment.
        # The only semantic-status transition is Accepted -> Superseded with a matching new Accepted ADR.
        if after_status == "Accepted":
            if superseded_by(after) is not None:
                errors.append(
                    f"{after_path}: Accepted ADR cannot declare Superseded by without Status: Superseded"
                )
            continue

        if after_status != "Superseded":
            errors.append(
                f"{after_path}: Accepted ADR status may only remain Accepted or become Superseded"
            )
            continue

        target = superseded_by(after)
        if target is None:
            errors.append(f"{after_path}: Superseded ADR must declare `Superseded by: ADR-NNNN`")
            continue

        new_text = new_adrs.get(target)
        if new_text is None:
            errors.append(
                f"{after_path}: {target} must be a new ADR in the same change when superseding {identifier}"
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
                f"{target}: superseding ADR must be `Status: Accepted` before {identifier} can become Superseded"
            )
            continue

        if supersedes(new_text) != identifier:
            errors.append(
                f"{target}: must declare `Supersedes: {identifier}` when replacing {after_path}"
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
            print("ADR history: no comparable CI base for this event; skipped")
            return 0

    if not base_ref:
        parser.error("provide --base-ref or --ci")

    errors = validate(base_ref)
    if errors:
        print("ADR history check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(f"ADR history check passed against {base_ref}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
