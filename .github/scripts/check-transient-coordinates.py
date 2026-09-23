#!/usr/bin/env python3
"""Reject concrete transient workspace artifact coordinates in durable tracked files.

Arcogine uses exact commit-SHA plus path pairs to identify artifacts while they are in active
workspace custody. Those coordinates must not become durable dependencies. This checker catches
the deterministic form: a full 40-character SHA immediately paired with a concrete path under
the reserved ``workspace/`` root. It deliberately does not validate ancestry, ban historical
SHAs, or infer transientness for paths outside ``workspace/``. Semantic dependencies without
this recognizable syntax remain a human-review responsibility; see AGENTS.md.
"""

from __future__ import annotations

import os
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(os.environ.get("TRANSIENT_COORDINATES_ROOT", Path(__file__).resolve().parents[2]))

# These files discuss or exercise the syntax itself. The narrowly scoped exemption follows the
# precedent in check-delivery-labels.py; ordinary durable files are never allowlisted.
SELF_FILES = {
    ".github/scripts/check-transient-coordinates.py",
    ".github/scripts/check-transient-coordinates.test.py",
}

# Active planning/delivery context may retain coordinates, while workspace/ is transient custody
# and is independently rejected from merge candidates by check-transient-workspace.py.
ALLOWED_PREFIXES = ("docs/planning/", "workspace/")

SHA = r"(?<![0-9A-Fa-f])[0-9A-Fa-f]{40}(?![0-9A-Fa-f])"
PATH = r"workspace/(?:[A-Za-z0-9._-]+/)*[A-Za-z0-9._-]+"
# Accept ordinary presentation around the pair (Markdown code ticks, quotes, table separators,
# plus signs, commas, and whitespace), but no intervening prose that could create broad matches.
COORDINATE = re.compile(rf"{SHA}[`'\"|+,:;(){{}}\[\]<>\s–—-]*{PATH}(?![A-Za-z0-9._/-])")


def tracked_files() -> list[Path]:
    result = subprocess.run(
        ["git", "ls-files"], cwd=ROOT, capture_output=True, text=True, check=True
    )
    return [ROOT / line for line in result.stdout.splitlines() if line]


def read_text(path: Path) -> str | None:
    try:
        return path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        return None


def relative(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def main() -> int:
    errors: list[str] = []
    for path in tracked_files():
        rel = relative(path)
        if rel in SELF_FILES or rel.startswith(ALLOWED_PREFIXES):
            continue

        text = read_text(path)
        if text is None:
            continue
        for line_number, line in enumerate(text.splitlines(), start=1):
            for match in COORDINATE.finditer(line):
                errors.append(
                    f"{rel}:{line_number}: durable artifact contains a transient workspace "
                    f"coordinate (`{match.group(0)}`); transfer retained meaning to durable "
                    "state or cite durable delivery-history provenance"
                )

    if errors:
        print("Transient-coordinate check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        print(
            "Durable tracked files must not depend on exact SHA + workspace artifact coordinates. "
            "Historical SHAs and semantic cases without recognizable coordinate syntax remain "
            "allowed and subject to human review.",
            file=sys.stderr,
        )
        return 1

    print("Transient-coordinate check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
