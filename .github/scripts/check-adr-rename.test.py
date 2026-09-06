#!/usr/bin/env python3
"""Focused rename tests for check-adr-immutability.py."""

from __future__ import annotations

from pathlib import Path
import subprocess
import tempfile

SCRIPT = Path(__file__).with_name("check-adr-immutability.py").resolve()


def run(cwd: Path, *args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        list(args), cwd=cwd, check=check, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
    )


def setup_repo() -> tuple[tempfile.TemporaryDirectory[str], Path, str]:
    tmp = tempfile.TemporaryDirectory()
    repo = Path(tmp.name)
    run(repo, "git", "init", "-q")
    run(repo, "git", "config", "user.email", "ci@example.invalid")
    run(repo, "git", "config", "user.name", "CI")
    path = repo / "docs/architecture/decisions/0001-gate-1-example.md"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "# ADR-0001: Example\n\nStatus: Accepted\nDate: 2026-01-01\n\n## Decision\n\nKeep history.\n",
        encoding="utf-8",
    )
    run(repo, "git", "add", ".")
    run(repo, "git", "commit", "-qm", "base")
    base = run(repo, "git", "rev-parse", "HEAD").stdout.strip()
    return tmp, repo, base


def validate(repo: Path, base: str) -> subprocess.CompletedProcess[str]:
    return run(repo, "python3", str(SCRIPT), "--base-ref", base, check=False)


def test_rename_without_amendment_is_rejected() -> None:
    tmp, repo, base = setup_repo()
    try:
        old = repo / "docs/architecture/decisions/0001-gate-1-example.md"
        new = repo / "docs/architecture/decisions/0001-semantic-example.md"
        old.rename(new)
        result = validate(repo, base)
        assert result.returncode != 0, result.stdout + result.stderr
    finally:
        tmp.cleanup()


def test_semantics_preserving_rename_with_amendment_is_allowed() -> None:
    tmp, repo, base = setup_repo()
    try:
        old = repo / "docs/architecture/decisions/0001-gate-1-example.md"
        new = repo / "docs/architecture/decisions/0001-semantic-example.md"
        old.rename(new)
        text = new.read_text(encoding="utf-8").replace(
            "Date: 2026-01-01\n",
            "Date: 2026-01-01\nAmendment: 2026-09-03 — durable filename clarification; no semantic change\n",
        )
        new.write_text(text, encoding="utf-8")
        result = validate(repo, base)
        assert result.returncode == 0, result.stdout + result.stderr
    finally:
        tmp.cleanup()


def main() -> None:
    test_rename_without_amendment_is_rejected()
    print("PASS: rename without amendment is rejected")
    test_semantics_preserving_rename_with_amendment_is_allowed()
    print("PASS: audited semantics-preserving rename is allowed")


if __name__ == "__main__":
    main()
