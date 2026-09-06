#!/usr/bin/env python3
"""Reject temporary delivery-coordinate identifiers used as durable semantic naming.

Planning documents and active delivery/history context (issues, PRs, reviews, branches, commits,
handoffs) may use initiative-local stage/gate/slice coordinates and PR-local review/finding
identifiers (e.g. `REV-123`) while that work is active. Durable semantic naming -- current-state
documentation, code/Javadoc comments, workflow definitions, and test/class/file names -- must
instead name the capability, contract, identity, invariant, or behavior directly, so it remains
legible after a plan, PR, or review is completed, renamed, condensed, or removed. See AGENTS.md's
"Temporary delivery coordinates and durable documentation" section for the full policy.

This mechanical check is a syntactic baseline, not proof of semantic self-containment. It covers
two tiers of pattern, deliberately scoped by collision risk:

- BROAD patterns (`REV-123`, `Gate 4`/`Gate4...`, `DH-E`) are unambiguous enough to flag across
  durable Markdown, Java/Javadoc comments, JS/TS test comments, and GitHub Actions workflow
  definitions.
- NARROW compact patterns (bare `G1`, `O2`, `C1`, `D1`, `W1`) are collision-prone against ordinary
  prose and identifiers, so they stay scoped to the repository's durable reader-facing Markdown
  surfaces only, matching this checker's original behavior.

Human review remains responsible for semantic leakage that cannot be recognized safely by syntax
alone (e.g. prose like "the next stage" with no literal coordinate).
"""

from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]

# Durable reader-facing Markdown surfaces (original scope; also carries the narrow compact-form
# patterns, which are too collision-prone for the broader source/workflow scan below).
DURABLE_MARKDOWN_FILES = (ROOT / "README.md", ROOT / "docs" / "README.md")
DURABLE_MARKDOWN_DIRS = (
    ROOT / "docs" / "architecture",
    ROOT / "docs" / "product",
    ROOT / "docs" / "reference",
    ROOT / "docs" / "examples",
)

# Durable non-Markdown artifacts: source/test comments, test/class/file names, and workflow
# definitions. Only the BROAD patterns apply here -- compact forms like "G1" are far too likely to
# collide with ordinary code identifiers to scan blindly across all of product/ and infra/.
BROAD_SCAN_DIRS = (
    ROOT / "product",
    ROOT / "infra",
    ROOT / ".github" / "workflows",
)
BROAD_SCAN_SUFFIXES = {".java", ".mjs", ".yml", ".yaml"}

# Planning documents are the legitimate home for these coordinates while work is active; never
# scanned, regardless of file type.
EXEMPT_DIRS = (ROOT / "docs" / "planning",)

SKIP_DIR_NAMES = {
    ".git",
    "build",
    "dist",
    "node_modules",
    "coverage",
    "logs",
    ".gradle",
    "test-results",
    "playwright-report",
}

# BROAD: unambiguous enough to flag as durable semantic naming anywhere in scope.
BROAD_CONTENT_PATTERNS = (
    re.compile(r"\bREV-\d+\b"),
    # No trailing \b: a planning-derived PascalCase identifier like `Gate4SomethingTest` has no
    # word boundary between the digit and the letter that immediately follows it, but the `Gate4`
    # prefix is still the temporary coordinate this check exists to catch.
    re.compile(r"\bGate\s?\d+(?:[-_][A-Za-z0-9]+)?"),
    re.compile(r"\bDH-[A-Z0-9]+\b"),
)

# NARROW: collision-prone compact forms, kept to the original durable-Markdown-only scope.
NARROW_CONTENT_PATTERNS = (re.compile(r"\b(?:G|O|C|D|W)\d+(?:\.\d+|-[A-Z0-9]+)?\+?\b"),)

# Path/filename variants of the same families (case-insensitive; filenames are lowercase-hyphenated
# for docs, PascalCase for Java classes -- so, unlike NARROW_PATH_PATTERN below, this has no
# trailing separator requirement: `Gate4SomethingTest.java` has no separator between `Gate4` and
# `Something`, but is exactly the case this pattern must catch.
BROAD_PATH_PATTERN = re.compile(
    r"(?:^|[-_/])(?:gate[-_]?\d+(?:[-_][a-z0-9]+)?|rev[-_]?\d+|dh[-_][a-z0-9]+)",
    re.IGNORECASE,
)
NARROW_PATH_PATTERN = re.compile(
    r"(?:^|[-_/])(?:[gocdw]\d+(?:[._-][a-z0-9]+)?)(?=[-_/\.]|$)",
    re.IGNORECASE,
)


def skipped(path: Path) -> bool:
    if any(part in SKIP_DIR_NAMES for part in path.parts):
        return True
    return any(exempt in path.parents or path == exempt for exempt in EXEMPT_DIRS)


def durable_markdown_files() -> list[Path]:
    files = {path for path in DURABLE_MARKDOWN_FILES if path.is_file() and not skipped(path)}
    for directory in DURABLE_MARKDOWN_DIRS:
        if directory.is_dir():
            files.update(path for path in directory.rglob("*.md") if not skipped(path))
    return sorted(files)


def broad_scan_files() -> list[Path]:
    files: set[Path] = set()
    for directory in BROAD_SCAN_DIRS:
        if not directory.is_dir():
            continue
        for path in directory.rglob("*"):
            if path.is_file() and path.suffix in BROAD_SCAN_SUFFIXES and not skipped(path):
                files.add(path)
    return sorted(files)


def content_violations(path: Path, patterns: tuple[re.Pattern[str], ...]) -> list[tuple[int, str]]:
    found: list[tuple[int, str]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        for pattern in patterns:
            for match in pattern.finditer(line):
                found.append((line_number, match.group(0)))
    return found


def path_violations(path: Path, pattern: re.Pattern[str]) -> list[str]:
    relative = path.relative_to(ROOT).as_posix()
    return [match.group(0).lstrip("-_/") for match in pattern.finditer(relative)]


def main() -> int:
    errors: list[str] = []

    for path in durable_markdown_files():
        relative = path.relative_to(ROOT)
        for identifier in path_violations(path, BROAD_PATH_PATTERN):
            errors.append(f"{relative}: temporary delivery identifier `{identifier}` in durable path")
        for identifier in path_violations(path, NARROW_PATH_PATTERN):
            errors.append(f"{relative}: temporary delivery identifier `{identifier}` in durable path")
        for line_number, identifier in content_violations(
            path, BROAD_CONTENT_PATTERNS + NARROW_CONTENT_PATTERNS
        ):
            errors.append(f"{relative}:{line_number}: temporary delivery identifier `{identifier}`")

    for path in broad_scan_files():
        relative = path.relative_to(ROOT)
        for identifier in path_violations(path, BROAD_PATH_PATTERN):
            errors.append(f"{relative}: temporary delivery identifier `{identifier}` in durable path")
        for line_number, identifier in content_violations(path, BROAD_CONTENT_PATTERNS):
            errors.append(f"{relative}:{line_number}: temporary delivery identifier `{identifier}`")

    if errors:
        print("Durable vocabulary check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        print(
            "Use semantic capability/contract/invariant names on durable artifacts (documentation, "
            "code/Javadoc comments, workflow definitions, test/class/file names); keep temporary "
            "stage/gate/slice or PR-local review/finding identifiers in docs/planning/ or active "
            "delivery history (issues, PRs, reviews, branches, commits, handoffs).",
            file=sys.stderr,
        )
        return 1

    print("Durable vocabulary check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
