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
exactly two content rules, plus one path rule:

1. Outside `docs/planning/`: any `PLAN-...` or `REV-...` token is a durable-naming leak and fails
   the check -- whether or not the token is well-formed. A malformed variant (wrong case, a
   dotted/underscore-joined segment, `REV-abc`, ...) is not a loophole: the whole point of a
   reserved namespace is that nothing shaped like it is allowed to leak into durable material.
2. Inside `docs/planning/`: a `PLAN-...`/`REV-...` token must fully conform to the canonical
   grammar (`PLAN-<TRACK>-<LOCAL-ID>` with an uppercase-ASCII track and hyphen-separated segments;
   `REV-<digits>`) or the check fails -- the namespace stays unambiguous only if every instance of
   it actually is the canonical shape. Once a line's well-formed tokens are accounted for, the old
   ambiguous label families this namespace replaced (`Gate 4`, `G1`, `G1.3`, `G4-B`, `D1`, `C1`,
   `O1`, `W1`, `DH-E`, ...) must not be reintroduced at their planning source. These legacy forms
   are not banned outside `docs/planning/` -- `C1`, `O1`, `D3`, etc. are ordinary identifiers
   elsewhere in the codebase (see the existing package/dependency corpus), and rejecting them there
   would be exactly the ambiguous, collision-prone enforcement this namespace exists to avoid.
3. A tracked file's path must never embed a `PLAN-*`/`REV-NNN` token, anywhere in the repository.
   A filename or directory name is always durable naming (it outlives whatever sequenced the work
   that produced it), so a reserved delivery label belongs in a planning document's content, never
   its path. Inside `docs/planning/`, the older per-track coordinate-derived filenames this
   convention replaced are rejected the same way.

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

# Broad tokenizers: catch every literal that *starts* as a delivery-coordinate attempt, whether or
# not it is actually well-formed, so a malformed variant (wrong case, a dotted or underscore-joined
# segment, ...) cannot silently evade detection merely by not matching the strict grammar below.
PLAN_TOKEN_PATTERN = re.compile(r"\bPLAN-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*")
REV_TOKEN_PATTERN = re.compile(r"\bREV-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*")

# The canonical grammar every token above must fully conform to (AGENTS.md: PLAN-<TRACK>-<LOCAL-ID>
# with an uppercase-ASCII track and hyphen-separated LOCAL-ID segments; REV-NNN, digits only).
CANONICAL_PLAN = re.compile(r"PLAN-[A-Z]+(?:-[A-Za-z0-9]+)+")
CANONICAL_REV = re.compile(r"REV-\d+")

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

        if PLAN_TOKEN_PATTERN.search(rel) or REV_TOKEN_PATTERN.search(rel):
            errors.append(
                f"{rel}: file path embeds a reserved PLAN-*/REV-NNN delivery-coordinate token; "
                "filenames must remain semantic, never coordinate-derived"
            )
        elif in_planning and LEGACY_FILENAME_PATTERN.search(path.name):
            errors.append(
                f"{rel}: planning filename still embeds a legacy delivery-coordinate form; "
                "planning filenames should be semantic (the label belongs in the content, not the path)"
            )

        text = read_text(path)
        if text is None:
            continue

        for line_number, line in enumerate(text.splitlines(), start=1):
            if not in_planning:
                for match in PLAN_TOKEN_PATTERN.finditer(line):
                    errors.append(
                        f"{rel}:{line_number}: durable artifact contains a PLAN-* delivery "
                        f"coordinate (`{match.group(0)}`) -- name the capability/contract/"
                        "invariant instead"
                    )
                for match in REV_TOKEN_PATTERN.finditer(line):
                    token = match.group(0)
                    if CANONICAL_REV.fullmatch(token):
                        errors.append(
                            f"{rel}:{line_number}: durable artifact contains a REV-NNN delivery "
                            f"coordinate (`{token}`) -- name the capability/contract/"
                            "invariant instead"
                        )
                    else:
                        errors.append(
                            f"{rel}:{line_number}: malformed delivery-coordinate token "
                            f"`{token}` -- review/finding identifiers must conform exactly to "
                            "REV-<digits>"
                        )
            else:
                # Every PLAN-* token must fully conform to the canonical grammar -- a malformed
                # variant (wrong case, a dotted/underscore-joined segment, ...) is flagged here
                # rather than silently passing through unmatched. Well-formed tokens are masked out
                # before the legacy sweep below, so a legacy pattern never matches a substring
                # embedded inside one (e.g. "W1" inside "PLAN-ENG-W1", or "C1" inside
                # "PLAN-ENG-5-C1").
                masked = list(line)
                for match in PLAN_TOKEN_PATTERN.finditer(line):
                    token = match.group(0)
                    if CANONICAL_PLAN.fullmatch(token):
                        masked[match.start() : match.end()] = " " * len(token)
                    else:
                        errors.append(
                            f"{rel}:{line_number}: malformed delivery-coordinate token "
                            f"`{token}` -- planning coordinates must conform exactly to "
                            "PLAN-<TRACK>-<LOCAL-ID> (uppercase-ASCII track, hyphen-separated "
                            "segments)"
                        )
                for match in REV_TOKEN_PATTERN.finditer(line):
                    token = match.group(0)
                    if CANONICAL_REV.fullmatch(token):
                        masked[match.start() : match.end()] = " " * len(token)
                    else:
                        errors.append(
                            f"{rel}:{line_number}: malformed delivery-coordinate token "
                            f"`{token}` -- review/finding identifiers must conform exactly to "
                            "REV-<digits>"
                        )
                masked_line = "".join(masked)
                for pattern in LEGACY_PATTERNS:
                    for match in pattern.finditer(masked_line):
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
