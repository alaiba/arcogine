#!/usr/bin/env python3
"""Behavioral tests for check-transient-coordinates.py."""

from __future__ import annotations

import os
import subprocess
import sys
import tempfile
from pathlib import Path

SCRIPT = Path(__file__).with_name("check-transient-coordinates.py")
SHA = "0123456789abcdef0123456789abcdef01234567"


def run_checker(files: dict[str, str]) -> tuple[int, str]:
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        subprocess.run(["git", "init", "-q"], cwd=root, check=True)
        subprocess.run(["git", "config", "user.email", "test@example.com"], cwd=root, check=True)
        subprocess.run(["git", "config", "user.name", "Test"], cwd=root, check=True)
        for relative, content in files.items():
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        subprocess.run(["git", "add", "-A"], cwd=root, check=True)
        result = subprocess.run(
            [sys.executable, str(SCRIPT)],
            cwd=root,
            env={**os.environ, "TRANSIENT_COORDINATES_ROOT": str(root)},
            capture_output=True,
            text=True,
        )
        return result.returncode, result.stderr


def assert_passes(files: dict[str, str]) -> None:
    code, stderr = run_checker(files)
    assert code == 0, f"expected pass, got failure:\n{stderr}"


def assert_fails(files: dict[str, str]) -> None:
    code, stderr = run_checker(files)
    assert code == 1, f"expected failure, but check passed: {files}"
    assert "durable artifact contains a transient workspace coordinate" in stderr


def test_concrete_coordinate_fails_in_markdown() -> None:
    assert_fails({"docs/reference/example.md": f"Evidence: `{SHA} + workspace/research/report.md`.\n"})


def test_concrete_coordinate_fails_in_non_markdown_text() -> None:
    assert_fails({".github/workflows/example.yml": f"artifact: {SHA} workspace/review/result.json\n"})


def test_historical_sha_without_workspace_artifact_path_passes() -> None:
    assert_passes({"docs/development/example.md": f"Baseline commit: {SHA}.\n"})


def test_generic_policy_prose_passes() -> None:
    assert_passes({"AGENTS.md": "Handoffs use commit SHA + workspace path while active.\n"})


def test_workspace_mention_without_concrete_coordinate_passes() -> None:
    assert_passes({"docs/development/example.md": "Temporary evidence lives under workspace/research/.\n"})


def test_durable_delivery_history_reference_passes() -> None:
    assert_passes({"docs/research/seed.md": f"Reconciled in merged PR #322 (baseline {SHA}).\n"})


def test_active_and_transient_surfaces_remain_allowed() -> None:
    assert_passes(
        {
            "docs/planning/example.md": f"Active evidence: {SHA} + workspace/research/report.md.\n",
            "workspace/research/report.md": f"Related artifact {SHA} + workspace/research/source.md.\n",
        }
    )


def test_partial_and_malformed_hex_strings_pass() -> None:
    assert_passes(
        {
            "docs/reference/example.md": (
                f"{SHA[:-1]} + workspace/research/report.md; "
                f"{SHA}a + workspace/research/other.md.\n"
            )
        }
    )


def test_checker_self_fixture_is_exempted() -> None:
    assert_passes(
        {
            ".github/scripts/check-transient-coordinates.test.py": (
                f"# Literal fixture: {SHA} + workspace/research/report.md\n"
            )
        }
    )


if __name__ == "__main__":
    tests = [value for name, value in globals().items() if name.startswith("test_")]
    for test in tests:
        test()
    print(f"{len(tests)} transient-coordinate checker tests passed")
