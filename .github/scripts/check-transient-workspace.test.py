#!/usr/bin/env python3
"""Focused tests for the tracked transient-workspace guard."""

from __future__ import annotations

import os
from pathlib import Path
import subprocess
import sys
import tempfile


SCRIPT = Path(__file__).with_name("check-transient-workspace.py")


def run_checker(files: dict[str, str]) -> tuple[int, str]:
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        subprocess.run(["git", "init", "-q"], cwd=root, check=True)
        for relative, content in files.items():
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        subprocess.run(["git", "add", "-A"], cwd=root, check=True)
        result = subprocess.run(
            [sys.executable, str(SCRIPT)],
            cwd=root,
            env={**os.environ, "TRANSIENT_WORKSPACE_ROOT": str(root)},
            capture_output=True,
            text=True,
        )
        return result.returncode, f"{result.stdout}{result.stderr}"


def assert_passes(files: dict[str, str]) -> None:
    code, output = run_checker(files)
    assert code == 0, f"expected pass, got {code}:\n{output}"


def assert_fails(files: dict[str, str]) -> None:
    code, output = run_checker(files)
    assert code == 1, f"expected failure, but check passed:\n{output}"
    assert "workspace/" in output, f"expected workspace path in:\n{output}"


def test_no_workspace_path_passes() -> None:
    assert_passes({"docs/development/policy.md": "Durable policy.\n"})


def test_one_tracked_workspace_file_fails() -> None:
    assert_fails({"workspace/implementation/prompt.md": "Transient prompt.\n"})


def test_nested_workspace_file_fails() -> None:
    assert_fails({"workspace/research/packet/checkpoint.md": "Transient checkpoint.\n"})


def test_similarly_named_paths_outside_workspace_pass() -> None:
    assert_passes(
        {
            "workspace-not-reserved/notes.md": "Not the reserved root.\n",
            "docs/workspace/research.md": "Durable documentation.\n",
        }
    )


def main() -> None:
    tests = [obj for name, obj in globals().items() if name.startswith("test_") and callable(obj)]
    for test in tests:
        test()
    print(f"All {len(tests)} transient-workspace tests passed.")


if __name__ == "__main__":
    main()
