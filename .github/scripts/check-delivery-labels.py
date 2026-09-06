#!/usr/bin/env python3
"""Enforce Arcogine's canonical delivery-label namespace.

Arcogine planning coordinates use the reserved `PLAN-<TRACK>-<LOCAL-ID>` namespace (e.g.
`PLAN-ENG-4`, `PLAN-GOV-1-3`, `PLAN-ENG-4-B`). PR-local review/finding identifiers use `REV-NNN`.
Both are temporary delivery coordinates: legitimate in `docs/planning/` and in active delivery
history (issues, PRs, reviews, branches, commits, handoffs), but never as durable semantic naming.
See AGENTS.md's "Temporary delivery coordinates and durable documentation" section for the full
policy.

Because both namespaces are unmistakable (`PLAN-` and `REV-` are not ordinary English or code
vocabulary), this checker needs no per-language extension lists, Markdown-directory tiers, or
per-file exemptions the way a collection of ambiguous compact labels (`G1`, `D3`, `C2`, `O1`,
`Gate 4`, `W1`, `DH-E`, ...) would. It enumerates tracked files via `git ls-files` and applies
exactly two rules:

1. Outside `docs/planning/`: a `PLAN-<TRACK>-...` or `REV-<digits>` token is a durable-naming
   leak and fails the check.
2. Inside `docs/planning/`: `PLAN-*` and `REV-<digits>` are expected and allowed, but the old
   ambiguous label families they replaced (`Gate 4`, `G1`, `G1.3`, `G4-B`, `D1`, `C1`, `O1`, `W1`,
   `DH-E`, ...) must not be reintroduced at their planning source. These legacy forms are not
   banned outside `docs/planning/` -- `C1`, `O1`, `D3`, etc. are ordinary identifiers elsewhere in
   the codebase (see the existing package/dependency corpus), and rejecting them there would be
   exactly the ambiguous, collision-prone enforcement this namespace exists to avoid.

Human review remains responsible for semantic leakage no syntax checker can identify (e.g. prose
like "the next stage" with no literal coordinate).
"""

from __future__ import annotations

import os
from pathlib import Path
import re
import subprocess
import sys

# DELIVERY_LABELS_ROOT lets the test suite point this checker at a disposable temporary git
# repository instead of the real one; production invocations never set it.
ROOT = Path(os.environ.get("DELIVERY_LABELS_ROOT", Path(__file__).resolve().parents[2]))

# This checker's own script/test files document and exercise these patterns as literal fixtures --
# the one unavoidable exemption, per AGENTS.md's allowance for "process/policy material where the
# temporary-coordinate syntax itself is the subject."
SELF_FILES = {
    ".github/scripts/check-delivery-labels.py",
    ".github/scripts/check-delivery-labels.test.py",
}

PLANNING_DIR = "docs/planning/"

PLAN_PATTERN = re.compile(r"\bPLAN-[A-Z]+(?:-[A-Za-z0-9]+)+\b")
REV_PATTERN = re.compile(r"\bREV-\d+\b")

# Legacy ambiguous planning-coordinate forms this namespace replaces. Rejected only at their
# planning source (docs/planning/**), never globally -- see module docstring.
LEGACY_PATTERNS = (
    re.compile(r"\bGate\s?\d+\b"),
    re.compile(r"\bG\d+(?:\.\d+)?(?:-[A-Za-z0-9]+)?\b"),
    re.compile(r"\bD\d+\b"),
    re.compile(r"\bC\d+\b"),
    re.compile(r"\bO\d+\b"),
    re.compile(r"\bW1\b"),
    re.compile(r"\bDH-[A-Z]\b"),
)

# A planning-derived filename embeds the same legacy shapes without word boundaries working the
# same way (hyphens instead of spaces), so it gets its own, slightly looser check.
LEGACY_FILENAME_PATTERN = re.compile(
    r"(?:^|[-_])(?:gate-?\d+|g\d+(?:[.-]\d+)?|d\d+|c\d+|o\d+|w1|dh-[a-z])(?:[-_]|$)",
    re.IGNORECASE,
)


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
        if rel in SELF_FILES:
            continue

        in_planning = rel.startswith(PLANNING_DIR)

        if in_planning and LEGACY_FILENAME_PATTERN.search(path.name):
            errors.append(
                f"{rel}: planning filename still embeds a legacy delivery-coordinate form; "
                "planning filenames should be semantic (the label belongs in the content, not the path)"
            )

        text = read_text(path)
        if text is None:
            continue

        for line_number, line in enumerate(text.splitlines(), start=1):
            if not in_planning:
                for pattern, name in ((PLAN_PATTERN, "PLAN-*"), (REV_PATTERN, "REV-NNN")):
                    for match in pattern.finditer(line):
                        errors.append(
                            f"{rel}:{line_number}: durable artifact contains a {name} delivery "
                            f"coordinate (`{match.group(0)}`) -- name the capability/contract/"
                            "invariant instead"
                        )
            else:
                # Mask already-canonical PLAN-*/REV-NNN tokens first, so a legacy pattern never
                # matches a substring embedded inside one (e.g. "W1" inside "PLAN-ENG-W1", or
                # "C1" inside "PLAN-ENG-5-C1").
                masked = PLAN_PATTERN.sub(lambda m: " " * len(m.group(0)), line)
                masked = REV_PATTERN.sub(lambda m: " " * len(m.group(0)), masked)
                for pattern in LEGACY_PATTERNS:
                    for match in pattern.finditer(masked):
                        errors.append(
                            f"{rel}:{line_number}: legacy delivery-coordinate form "
                            f"`{match.group(0)}` -- use the canonical PLAN-<TRACK>-<LOCAL-ID> "
                            "namespace instead"
                        )

    if errors:
        print("Delivery-label check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        print(
            "Planning coordinates use PLAN-<TRACK>-<LOCAL-ID>; PR-local review/finding "
            "identifiers use REV-NNN. Both are allowed in docs/planning/ and active delivery "
            "history (issues, PRs, reviews, branches, commits, handoffs); durable artifacts "
            "(documentation, code/Javadoc comments, workflow definitions, test/class/file names) "
            "must name the underlying capability, contract, identity, invariant, or behavior "
            "instead. Legacy ambiguous forms (Gate 4, G1.3, D1, C1, O1, W1, DH-E, ...) must not "
            "be reintroduced into planning material.",
            file=sys.stderr,
        )
        return 1

    print("Delivery-label check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
