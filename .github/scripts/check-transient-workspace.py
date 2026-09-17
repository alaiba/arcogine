#!/usr/bin/env python3
"""Reject tracked files under the transient repository workspace."""

from __future__ import annotations

import os
from pathlib import Path
import subprocess
import sys


ROOT_ENVIRONMENT_VARIABLE = "TRANSIENT_WORKSPACE_ROOT"


def repository_root() -> Path:
    configured_root = os.environ.get(ROOT_ENVIRONMENT_VARIABLE)
    if configured_root:
        return Path(configured_root)

    result = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"],
        capture_output=True,
        check=False,
        text=True,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "unable to resolve the repository root")
    return Path(result.stdout.strip())


def tracked_workspace_files(root: Path) -> list[str]:
    result = subprocess.run(
        ["git", "ls-files", "--", "workspace/"],
        cwd=root,
        capture_output=True,
        check=False,
        text=True,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "unable to inspect tracked files")
    return [path for path in result.stdout.splitlines() if path]


def main() -> int:
    try:
        files = tracked_workspace_files(repository_root())
    except (OSError, RuntimeError) as error:
        print(f"Transient workspace check failed: {error}", file=sys.stderr)
        return 1

    if files:
        print("Tracked files under reserved transient workspace/ are not allowed:", file=sys.stderr)
        for path in files:
            print(f"  {path}", file=sys.stderr)
        return 1

    print("No tracked files under reserved transient workspace/.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
