#!/usr/bin/env python3
"""Focused tests for check-durable-vocabulary.py."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import tempfile

SCRIPT = Path(__file__).with_name("check-durable-vocabulary.py")
SPEC = importlib.util.spec_from_file_location("durable_vocabulary", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def check_markdown_content(text: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        path = Path(directory) / "doc.md"
        path.write_text(text, encoding="utf-8")
        patterns = MODULE.BROAD_CONTENT_PATTERNS + MODULE.NARROW_CONTENT_PATTERNS
        return [identifier for _, identifier in MODULE.content_violations(path, patterns)]


def check_broad_content(text: str, suffix: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        path = Path(directory) / f"file{suffix}"
        path.write_text(text, encoding="utf-8")
        return [identifier for _, identifier in MODULE.content_violations(path, MODULE.BROAD_CONTENT_PATTERNS)]


def check_markdown_path(relative: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        try:
            MODULE.ROOT = Path(directory)
            path = MODULE.ROOT / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("semantic content\n", encoding="utf-8")
            return MODULE.path_violations(path, MODULE.BROAD_PATH_PATTERN) + MODULE.path_violations(
                path, MODULE.NARROW_PATH_PATTERN
            )
        finally:
            MODULE.ROOT = old_root


def check_broad_path(relative: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        try:
            MODULE.ROOT = Path(directory)
            path = MODULE.ROOT / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("semantic content\n", encoding="utf-8")
            return MODULE.path_violations(path, MODULE.BROAD_PATH_PATTERN)
        finally:
            MODULE.ROOT = old_root


def check_broad_markdown_content(text: str, relative: str) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_exempt_files = MODULE.EXEMPT_FILES
        try:
            MODULE.ROOT = Path(directory)
            MODULE.EXEMPT_FILES = tuple(
                MODULE.ROOT / rel
                for rel in ("docs/development/reviewing.md", "docs/development/consistency-review.md")
            )
            path = MODULE.ROOT / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
            return [identifier for _, identifier in MODULE.content_violations(path, MODULE.BROAD_CONTENT_PATTERNS)]
        finally:
            MODULE.ROOT = old_root
            MODULE.EXEMPT_FILES = old_exempt_files


def scanned_broad_markdown_paths(paths: list[str]) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_dirs = MODULE.BROAD_MARKDOWN_DIRS
        old_only_files = MODULE.BROAD_ONLY_MARKDOWN_FILES
        old_exempt_dirs = MODULE.EXEMPT_DIRS
        old_exempt_files = MODULE.EXEMPT_FILES
        try:
            MODULE.ROOT = Path(directory)
            MODULE.BROAD_MARKDOWN_DIRS = (MODULE.ROOT / "docs",)
            MODULE.BROAD_ONLY_MARKDOWN_FILES = (
                MODULE.ROOT / "product" / "interfaces" / "web" / "README.md",
            )
            MODULE.EXEMPT_DIRS = (MODULE.ROOT / "docs" / "planning",)
            MODULE.EXEMPT_FILES = tuple(
                MODULE.ROOT / rel
                for rel in ("docs/development/reviewing.md", "docs/development/consistency-review.md")
            )
            for relative in paths:
                path = MODULE.ROOT / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("semantic content\n", encoding="utf-8")
            return [path.relative_to(MODULE.ROOT).as_posix() for path in MODULE.broad_markdown_files()]
        finally:
            MODULE.ROOT = old_root
            MODULE.BROAD_MARKDOWN_DIRS = old_dirs
            MODULE.BROAD_ONLY_MARKDOWN_FILES = old_only_files
            MODULE.EXEMPT_DIRS = old_exempt_dirs
            MODULE.EXEMPT_FILES = old_exempt_files


def scanned_markdown_paths(paths: list[str]) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_files = MODULE.DURABLE_MARKDOWN_FILES
        old_dirs = MODULE.DURABLE_MARKDOWN_DIRS
        old_exempt = MODULE.EXEMPT_DIRS
        try:
            MODULE.ROOT = Path(directory)
            MODULE.DURABLE_MARKDOWN_FILES = (
                MODULE.ROOT / "README.md",
                MODULE.ROOT / "docs" / "README.md",
            )
            MODULE.DURABLE_MARKDOWN_DIRS = tuple(
                MODULE.ROOT / path
                for path in ("docs/architecture", "docs/product", "docs/reference", "docs/examples")
            )
            MODULE.EXEMPT_DIRS = (MODULE.ROOT / "docs" / "planning",)
            for relative in paths:
                path = MODULE.ROOT / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("semantic content\n", encoding="utf-8")
            return [path.relative_to(MODULE.ROOT).as_posix() for path in MODULE.durable_markdown_files()]
        finally:
            MODULE.ROOT = old_root
            MODULE.DURABLE_MARKDOWN_FILES = old_files
            MODULE.DURABLE_MARKDOWN_DIRS = old_dirs
            MODULE.EXEMPT_DIRS = old_exempt


def scanned_broad_paths(paths: list[str]) -> list[str]:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_dirs = MODULE.BROAD_SCAN_DIRS
        old_exempt_dirs = MODULE.EXEMPT_DIRS
        old_exempt_files = MODULE.EXEMPT_FILES
        try:
            MODULE.ROOT = Path(directory)
            MODULE.BROAD_SCAN_DIRS = tuple(
                MODULE.ROOT / path
                for path in ("product", "infra", ".github/workflows", ".github/scripts")
            )
            MODULE.EXEMPT_DIRS = (MODULE.ROOT / "docs" / "planning",)
            MODULE.EXEMPT_FILES = (
                MODULE.ROOT / ".github" / "scripts" / "check-durable-vocabulary.py",
                MODULE.ROOT / ".github" / "scripts" / "check-durable-vocabulary.test.py",
            )
            for relative in paths:
                path = MODULE.ROOT / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("semantic content\n", encoding="utf-8")
            return [path.relative_to(MODULE.ROOT).as_posix() for path in MODULE.broad_scan_files()]
        finally:
            MODULE.ROOT = old_root
            MODULE.BROAD_SCAN_DIRS = old_dirs
            MODULE.EXEMPT_DIRS = old_exempt_dirs
            MODULE.EXEMPT_FILES = old_exempt_files


# --- Durable Markdown: unchanged baseline behavior ---------------------------------------------


def test_semantic_vocabulary_is_allowed() -> None:
    assert check_markdown_content(
        "Resource selection is deterministic. Supported runtime events use a monotonic sequence.\n"
    ) == []


def test_numeric_gate_identifier_is_rejected_in_markdown() -> None:
    assert check_markdown_content("Gate 4 established supported runtime events.\n") == ["Gate 4"]


def test_prefixed_stage_and_slice_identifiers_are_rejected_in_markdown() -> None:
    text = "G1.3 persists revisions; G4-C closes observations; O2 owns authority; W1 decomposes work.\n"
    assert check_markdown_content(text) == ["G1.3", "G4-C", "O2", "W1"]


def test_distribution_slice_identifier_is_rejected_in_markdown() -> None:
    assert check_markdown_content("DH-E adds recovery hardening.\n") == ["DH-E"]


def test_durable_filename_coordinate_is_rejected() -> None:
    assert check_markdown_path("docs/architecture/decisions/0007-gate-3-session-control.md") == [
        "gate-3-session"
    ]


def test_semantic_filename_is_allowed() -> None:
    assert check_markdown_path("docs/architecture/decisions/0007-consumer-neutral-session-control.md") == []


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
        "product/interfaces/web/README.md",
    ]
    # product/interfaces/web/README.md deliberately does NOT appear here: it gets BROAD-only
    # coverage via broad_markdown_files() (see test_broad_markdown_scan_covers_all_of_docs_outside_planning),
    # not the NARROW-compact-form scan this scope function feeds.
    assert scanned_markdown_paths(paths) == [
        "README.md",
        "docs/README.md",
        "docs/architecture/a.md",
        "docs/examples/d.md",
        "docs/product/b.md",
        "docs/reference/c.md",
    ]


# --- New: PR-local review identifier (REV-123) high-confidence detection -----------------------


def test_rev_identifier_is_rejected_in_markdown() -> None:
    assert check_markdown_content("REV-123 fixed a parser edge case.\n") == ["REV-123"]


def test_rev_identifier_is_rejected_in_java_comment() -> None:
    text = "// REV-123: caller must not bypass validation\n"
    assert check_broad_content(text, ".java") == ["REV-123"]


def test_rev_identifier_is_rejected_in_javadoc() -> None:
    text = "/**\n * Load-bearing invariant (ADR-0011 REV-003).\n */\n"
    assert check_broad_content(text, ".java") == ["REV-003"]


def test_rev_identifier_is_rejected_in_js_test_comment() -> None:
    text = "// REV-005: a verdict followed by prose is not final\n"
    assert check_broad_content(text, ".mjs") == ["REV-005"]


def test_rev_identifier_is_rejected_in_workflow_comment() -> None:
    text = "# REV-002 fixed the listener resolution\n"
    assert check_broad_content(text, ".yml") == ["REV-002"]


def test_rev_identifier_is_rejected_in_typescript_comment() -> None:
    text = "// REV-011: keep the reducer pure\n"
    assert check_broad_content(text, ".tsx") == ["REV-011"]
    assert check_broad_content(text, ".ts") == ["REV-011"]


def test_rev_identifier_is_rejected_in_python_tooling() -> None:
    text = "# REV-004: guard against an empty manifest\n"
    assert check_broad_content(text, ".py") == ["REV-004"]


def test_rev_identifier_is_rejected_in_shell_tooling() -> None:
    text = "# REV-007 fixed the classifier regex\n"
    assert check_broad_content(text, ".sh") == ["REV-007"]


def test_own_checker_script_and_fixtures_are_exempt() -> None:
    assert scanned_broad_paths(
        [
            ".github/scripts/check-durable-vocabulary.py",
            ".github/scripts/check-durable-vocabulary.test.py",
            ".github/scripts/check-pr-disposition.sh",
        ]
    ) == [".github/scripts/check-pr-disposition.sh"]


def test_gate_identifier_is_rejected_in_broad_scan() -> None:
    text = "/** Headless Gate 4-C closure evidence. */\n"
    assert check_broad_content(text, ".java") == ["Gate 4-C"]


# --- New: planning-derived test/class/file name detection --------------------------------------


def test_gate_class_name_is_rejected_as_durable_naming() -> None:
    text = "class Gate4SomethingTest {\n}\n"
    assert check_broad_content(text, ".java") == ["Gate4"]
    assert check_broad_path("product/domains/factory/src/test/java/Gate4SomethingTest.java") == [
        "Gate4"
    ]


def test_semantic_class_name_is_allowed() -> None:
    text = "class RuntimeObservationAcceptanceTest {\n}\n"
    assert check_broad_content(text, ".java") == []
    assert (
        check_broad_path(
            "product/domains/factory/src/test/java/RuntimeObservationAcceptanceTest.java"
        )
        == []
    )


# --- Allowed contexts remain allowed -------------------------------------------------------------


def test_planning_docs_remain_allowed() -> None:
    assert (
        scanned_markdown_paths(["docs/planning/gate-4-runtime-observation-event-delivery.md"]) == []
    )


# --- New: broad Markdown coverage beyond the four narrow durable-doc directories ---------------


def test_rev_identifier_is_rejected_in_durable_development_markdown() -> None:
    assert check_broad_markdown_content(
        "REV-042 fixed a stale reference.\n", "docs/development/testing.md"
    ) == ["REV-042"]


def test_broad_markdown_scan_covers_all_of_docs_outside_planning() -> None:
    paths = [
        "README.md",
        "docs/README.md",
        "docs/architecture/a.md",
        "docs/development/testing.md",
        "docs/planning/plan.md",
        "docs/development/reviewing.md",
        "docs/development/consistency-review.md",
        "product/interfaces/web/README.md",
    ]
    assert scanned_broad_markdown_paths(paths) == [
        "docs/README.md",
        "docs/architecture/a.md",
        "docs/development/testing.md",
        "product/interfaces/web/README.md",
    ]


def test_web_readme_gets_broad_but_not_narrow_scanning() -> None:
    # product/interfaces/web/README.md is BROAD-only: an ordinary compact form must NOT be
    # flagged there, unlike the original narrow-scan surfaces (docs/architecture/**, etc.), even
    # though it does get BROAD-pattern coverage (see test_rev_identifier_is_rejected_in_durable_development_markdown
    # for the equivalent REV-123 case on a docs/ file).
    assert (
        check_broad_markdown_content(
            "G1 is an ordinary compact identifier here, not a coordinate leak.\n",
            "product/interfaces/web/README.md",
        )
        == []
    )
    assert check_broad_markdown_content(
        "REV-042 fixed a stale reference.\n", "product/interfaces/web/README.md"
    ) == ["REV-042"]


def test_policy_documents_remain_exempt_from_broad_markdown_scan() -> None:
    with tempfile.TemporaryDirectory() as directory:
        old_root = MODULE.ROOT
        old_exempt_files = MODULE.EXEMPT_FILES
        try:
            MODULE.ROOT = Path(directory)
            MODULE.EXEMPT_FILES = (MODULE.ROOT / "docs" / "development" / "reviewing.md",)
            path = MODULE.ROOT / "docs" / "development" / "reviewing.md"
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("A reviewer's own REV-123 numbering for a single PR's findings.\n", encoding="utf-8")
            assert MODULE.skipped(path) is True
        finally:
            MODULE.ROOT = old_root
            MODULE.EXEMPT_FILES = old_exempt_files


def test_planning_scoped_source_files_remain_allowed() -> None:
    assert scanned_broad_paths(["docs/planning/notes/Gate4Notes.java"]) == []


def test_broad_scan_excludes_generated_and_dependency_output() -> None:
    paths = [
        "product/module/build/generated/Gate4Leftover.java",
        "product/interfaces/web/node_modules/pkg/Gate4Thing.mjs",
        ".github/workflows/ci.yml",
        "product/domains/factory/src/test/java/Real.java",
        "infra/dev/pr-watch.test.mjs",
    ]
    assert scanned_broad_paths(paths) == [
        ".github/workflows/ci.yml",
        "infra/dev/pr-watch.test.mjs",
        "product/domains/factory/src/test/java/Real.java",
    ]


# --- False-positive protection for compact/ordinary identifiers --------------------------------


def test_ordinary_identifiers_are_not_rejected_in_broad_scan() -> None:
    text = (
        "// Revenue and G1 are not coordinates here: G1 is a made-up local var name.\n"
        "int gateway4 = computeGateway();\n"
    )
    assert check_broad_content(text, ".java") == []


def test_compact_forms_are_not_flagged_outside_markdown() -> None:
    text = "// G1 handles admission; O2 tracks ownership; W1 decomposes work.\n"
    assert check_broad_content(text, ".java") == []


# --- Semantic replacement names pass -------------------------------------------------------------


def test_semantic_replacement_prose_passes_everywhere() -> None:
    markdown = "The session-control boundary proves reset reproduces identical results.\n"
    assert check_markdown_content(markdown) == []
    assert check_broad_content(markdown, ".java") == []
    assert check_broad_content(markdown, ".mjs") == []
    assert check_broad_content(markdown, ".yml") == []


def main() -> None:
    tests = [obj for name, obj in globals().items() if name.startswith("test_") and callable(obj)]
    for test in tests:
        test()
    print(f"All {len(tests)} durable vocabulary tests passed.")


if __name__ == "__main__":
    main()
