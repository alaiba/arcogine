#!/usr/bin/env python3
"""Behavioral tests for check-delivery-labels.py.

Each test builds a disposable temporary git repository, writes fixture files into it, commits
them (so `git ls-files` sees them), and runs the checker against it via DELIVERY_LABELS_ROOT --
proving the actual invariant (which coordinates pass/fail, where) rather than pinning internal
directory lists.
"""

from __future__ import annotations

import subprocess
import sys
import tempfile
from pathlib import Path

SCRIPT = Path(__file__).with_name("check-delivery-labels.py")


def run_checker(files: dict[str, str]) -> tuple[int, str]:
    """Write `files` (relative path -> content) into a fresh temp git repo and run the checker."""
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
            env={**__import__("os").environ, "DELIVERY_LABELS_ROOT": str(root)},
            capture_output=True,
            text=True,
        )
        return result.returncode, result.stderr


def assert_passes(files: dict[str, str]) -> None:
    code, stderr = run_checker(files)
    assert code == 0, f"expected pass, got failure:\n{stderr}"


def assert_fails(files: dict[str, str], expect_substring: str) -> None:
    code, stderr = run_checker(files)
    assert code == 1, f"expected failure containing {expect_substring!r}, but check passed"
    assert expect_substring in stderr, f"expected {expect_substring!r} in:\n{stderr}"


# --- Canonical PLAN-* namespace is allowed in planning ------------------------------------------


def test_canonical_plan_label_allowed_in_planning() -> None:
    assert_passes({"docs/planning/example.md": "PLAN-ENG-4 is in progress.\n"})


def test_hierarchical_plan_labels_allowed_in_planning() -> None:
    assert_passes(
        {
            "docs/planning/example.md": (
                "PLAN-ENG-4-B is complete. PLAN-GOV-1-3 depends on PLAN-ENG-4-B.\n"
            )
        }
    )


# --- Legacy planning-coordinate forms are rejected at their planning source ---------------------


def test_old_gate_syntax_rejected_in_planning() -> None:
    assert_fails(
        {"docs/planning/example.md": "Gate 4 is in progress.\n"},
        "legacy delivery-coordinate form `Gate 4`",
    )


def test_old_dotted_governance_syntax_rejected_in_planning() -> None:
    assert_fails(
        {"docs/planning/example.md": "Governance G1.3 is complete.\n"},
        "legacy delivery-coordinate form `G1.3`",
    )


def test_old_compact_forms_rejected_in_planning() -> None:
    for legacy, label in [
        ("D1 is the canonical model contract.\n", "D1"),
        ("C3 is the evaluation capability.\n", "C3"),
        ("O2 identity/trust/authority.\n", "O2"),
        ("The accepted W1 decomposition contract.\n", "W1"),
        ("Distribution hardening (DH-E) remains outstanding.\n", "DH-E"),
    ]:
        assert_fails({"docs/planning/example.md": legacy}, f"legacy delivery-coordinate form `{label}`")


# --- Durable semantic material must not acquire PLAN-*/REV-NNN labels ---------------------------


def test_plan_label_in_durable_markdown_fails() -> None:
    assert_fails(
        {"docs/architecture/overview.md": "This is governed by PLAN-GOV-1.\n"},
        "durable artifact contains a PLAN-* delivery coordinate",
    )


def test_plan_label_in_java_javadoc_fails() -> None:
    assert_fails(
        {
            "product/domains/factory/src/main/java/com/example/Foo.java": (
                "/** Implements PLAN-ENG-4's contract. */\npublic class Foo {}\n"
            )
        },
        "durable artifact contains a PLAN-* delivery coordinate",
    )


def test_plan_label_in_broad_source_surfaces_fails() -> None:
    for relative in (
        "product/interfaces/web/src/App.tsx",
        "infra/dev/pr-watch.mjs",
        ".github/scripts/some-tool.py",
        ".github/workflows/ci.yml",
    ):
        assert_fails({relative: "// tracks PLAN-ENG-4\n"}, "durable artifact contains a PLAN-* delivery coordinate")


def test_rev_identifier_in_durable_material_fails() -> None:
    assert_fails(
        {"docs/architecture/overview.md": "REV-123 fixed a parser edge case.\n"},
        "durable artifact contains a REV-NNN delivery coordinate",
    )


def test_rev_identifier_in_planning_or_delivery_history_passes() -> None:
    assert_passes({"docs/planning/example.md": "REV-123 corrected this section.\n"})


# --- Policy documents can describe the syntax without a literal example -------------------------


def test_policy_metavariable_syntax_passes_without_exemption() -> None:
    assert_passes(
        {
            "AGENTS.md": (
                "Planning coordinates use PLAN-<TRACK>-<LOCAL-ID>. "
                "PR-local review identifiers use REV-NNN.\n"
            )
        }
    )


# --- False-positive protection -------------------------------------------------------------------


def test_ordinary_compact_identifiers_outside_planning_are_not_rejected() -> None:
    assert_passes(
        {
            "product/consumer/challenge/src/main/java/com/example/C1.java": (
                "public class C1 {}\n"
            ),
            "docs/architecture/overview.md": "See D3 and O1 in the dependency graph library.\n",
        }
    )


def test_generated_and_untracked_output_is_not_scanned() -> None:
    # Not `git add`-ed, so `git ls-files` never reports it -- proves the checker relies on the
    # tracked-file list rather than walking the filesystem directly.
    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        subprocess.run(["git", "init", "-q"], cwd=root, check=True)
        subprocess.run(["git", "config", "user.email", "test@example.com"], cwd=root, check=True)
        subprocess.run(["git", "config", "user.name", "Test"], cwd=root, check=True)
        tracked = root / "docs" / "architecture" / "overview.md"
        tracked.parent.mkdir(parents=True, exist_ok=True)
        tracked.write_text("Semantic content only.\n", encoding="utf-8")
        subprocess.run(["git", "add", "-A"], cwd=root, check=True)
        untracked = root / "product" / "build" / "generated.md"
        untracked.parent.mkdir(parents=True, exist_ok=True)
        untracked.write_text("PLAN-ENG-4 leaked into generated output.\n", encoding="utf-8")
        result = subprocess.run(
            [sys.executable, str(SCRIPT)],
            cwd=root,
            env={**__import__("os").environ, "DELIVERY_LABELS_ROOT": str(root)},
            capture_output=True,
            text=True,
        )
        assert result.returncode == 0, f"expected pass, got failure:\n{result.stderr}"


# --- Planning filenames must stay semantic --------------------------------------------------------


def test_coordinate_derived_planning_filename_is_rejected() -> None:
    assert_fails(
        {"docs/planning/gate-4-runtime-observation.md": "Semantic content.\n"},
        "planning filename still embeds a legacy delivery-coordinate form",
    )


def test_semantic_planning_filename_is_allowed() -> None:
    assert_passes({"docs/planning/runtime-observation-event-delivery.md": "Semantic content.\n"})


# --- The checker's own fixtures are exempt (they document/exercise these patterns) --------------


def test_checker_self_files_are_exempt() -> None:
    assert_passes(
        {
            ".github/scripts/check-delivery-labels.py": "# doc mentions PLAN-ENG-4 and REV-123\n",
            ".github/scripts/check-delivery-labels.test.py": "# fixture text PLAN-ENG-4\n",
        }
    )


def main() -> None:
    tests = [obj for name, obj in globals().items() if name.startswith("test_") and callable(obj)]
    for test in tests:
        test()
    print(f"All {len(tests)} delivery-label tests passed.")


if __name__ == "__main__":
    main()
