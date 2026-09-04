#!/usr/bin/env python3
"""Focused tests for check-durable-doc-vocabulary.py."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import tempfile

SCRIPT = Path(__file__).with_name("check-durable-doc-vocabulary.py")
SPEC = importlib.util.spec_from_file_location("durable_doc_vocabulary", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def check_content(text: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        path = Path(directory) / "doc.md"
        path.write_text(text, encoding="utf-8")
        return [identifier for _, identifier in MODULE.content_violations(path)]


def check_path(relative: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        try:
            MODULE.ROOT = Path(directory)
            path = MODULE.ROOT / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("semantic content\n", encoding="utf-8")
            return MODULE.path_violations(path)
        finally:
            MODULE.ROOT = old_root


def scanned_paths(paths: list[str]) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_files = MODULE.DURABLE_FILES
        old_dirs = MODULE.DURABLE_DIRS
        try:
            MODULE.ROOT = Path(directory)
            MODULE.DURABLE_FILES = (MODULE.ROOT / "README.md", MODULE.ROOT / "docs" / "README.md")
            MODULE.DURABLE_DIRS = tuple(
                MODULE.ROOT / path
                for path in ("docs/architecture", "docs/product", "docs/reference", "docs/examples")
            )
            for relative in paths:
                path = MODULE.ROOT / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("semantic content\n", encoding="utf-8")
            return [path.relative_to(MODULE.ROOT).as_posix() for path in MODULE.durable_markdown_files()]
        finally:
            MODULE.ROOT = old_root
            MODULE.DURABLE_FILES = old_files
            MODULE.DURABLE_DIRS = old_dirs


def test_semantic_vocabulary_is_allowed() -> None:
    assert check_content("Resource selection is deterministic. Supported runtime events use a monotonic sequence.\n") == []


def test_numeric_gate_identifier_is_rejected() -> None:
    assert check_content("Gate 4 established supported runtime events.\n") == ["Gate 4"]


def test_prefixed_stage_and_slice_identifiers_are_rejected() -> None:
    text = "G1.3 persists revisions; G4-C closes observations; O2 owns authority; W1 decomposes work.\n"
    assert check_content(text) == ["G1.3", "G4-C", "O2", "W1"]


def test_distribution_slice_identifier_is_rejected() -> None:
    assert check_content("DH-E adds recovery hardening.\n") == ["DH-E"]


def test_durable_filename_coordinate_is_rejected() -> None:
    assert check_path("docs/architecture/decisions/0007-gate-3-session-control.md") == ["gate-3-session"]


def test_semantic_filename_is_allowed() -> None:
    assert check_path("docs/architecture/decisions/0007-consumer-neutral-session-control.md") == []


def test_scan_scope_includes_durable_reader_facing_surfaces_only() -> None:
    paths = [
        "README.md",
        "docs/README.md",
        "docs/architecture/a.md",
        "docs/product/b.md",
        "docs/reference/c.md",
        "docs/examples/d.md",
        "docs/planning/plan.md",
        "docs/development/process.md",
        ".github/agents/reviewer.md",
        "AGENTS.md",
        "product/module/README.md",
    ]
    assert scanned_paths(paths) == [
        "README.md",
        "docs/README.md",
        "docs/architecture/a.md",
        "docs/examples/d.md",
        "docs/product/b.md",
        "docs/reference/c.md",
    ]


def main() -> None:
    test_semantic_vocabulary_is_allowed()
    test_numeric_gate_identifier_is_rejected()
    test_prefixed_stage_and_slice_identifiers_are_rejected()
    test_distribution_slice_identifier_is_rejected()
    test_durable_filename_coordinate_is_rejected()
    test_semantic_filename_is_allowed()
    test_scan_scope_includes_durable_reader_facing_surfaces_only()
    print("All durable documentation vocabulary tests passed.")


if __name__ == "__main__":
    main()
