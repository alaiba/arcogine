#!/usr/bin/env python3
"""Reject transient planning-stage identifiers in durable Markdown documentation.

Planning and working/process documents may use initiative-local coordinates while that work is
active. Durable semantic/current-state documentation must instead name the capability, contract,
identity, invariant, or behavior directly so it remains legible after a plan is completed, renamed,
condensed, or removed.

This mechanical check intentionally covers the repository's durable reader-facing documentation
surfaces. Human review applies the broader documentation-lifetime rule where a file does not fit a
purely mechanical classification.
"""

from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
DURABLE_FILES = (ROOT / "README.md", ROOT / "docs" / "README.md")
DURABLE_DIRS = (
    ROOT / "docs" / "architecture",
    ROOT / "docs" / "product",
    ROOT / "docs" / "reference",
    ROOT / "docs" / "examples",
)
SKIP_DIR_NAMES = {".git", "build", "dist", "node_modules", "coverage", "logs"}

CONTENT_PATTERNS = (
    re.compile(r"\bGate\s+\d+(?:-[A-Z0-9]+)?\b"),
    re.compile(r"\b(?:G|O|C|D|W)\d+(?:\.\d+|-[A-Z0-9]+)?\+?\b"),
    re.compile(r"\bDH-[A-Z0-9]+\b"),
)

PATH_PATTERN = re.compile(
    r"(?:^|[-_/])(?:gate[-_ ]?\d+(?:[-_][a-z0-9]+)?|[gocdw]\d+(?:[._-][a-z0-9]+)?|dh[-_][a-z0-9]+)(?=[-_/\.]|$)",
    re.IGNORECASE,
)


def skipped(path: Path) -> bool:
    return any(part in SKIP_DIR_NAMES for part in path.parts)


def durable_markdown_files() -> list[Path]:
    files = {path for path in DURABLE_FILES if path.is_file() and not skipped(path)}
    for directory in DURABLE_DIRS:
        if directory.is_dir():
            files.update(path for path in directory.rglob("*.md") if not skipped(path))
    return sorted(files)


def content_violations(path: Path) -> list[tuple[int, str]]:
    found: list[tuple[int, str]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        for pattern in CONTENT_PATTERNS:
            for match in pattern.finditer(line):
                found.append((line_number, match.group(0)))
    return found


def path_violations(path: Path) -> list[str]:
    relative = path.relative_to(ROOT).as_posix()
    return [match.group(0).lstrip("-_/" ) for match in PATH_PATTERN.finditer(relative)]


def main() -> int:
    errors: list[str] = []
    for path in durable_markdown_files():
        relative = path.relative_to(ROOT)
        for identifier in path_violations(path):
            errors.append(f"{relative}: transient planning identifier `{identifier}` in durable path")
        for line_number, identifier in content_violations(path):
            errors.append(f"{relative}:{line_number}: transient planning identifier `{identifier}`")

    if errors:
        print("Durable documentation vocabulary check failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        print(
            "Use semantic capability/contract names on durable documentation surfaces; keep "
            "plan-local stage/slice identifiers in planning or delivery history.",
            file=sys.stderr,
        )
        return 1

    print("Durable documentation vocabulary check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
