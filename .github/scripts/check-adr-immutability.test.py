#!/usr/bin/env python3

from __future__ import annotations

from pathlib import Path
import subprocess
import tempfile

SCRIPT = Path(__file__).with_name("check-adr-immutability.py").resolve()


def run(cwd: Path, *args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        list(args),
        cwd=cwd,
        check=check,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )


def write(cwd: Path, name: str, text: str) -> None:
    path = cwd / "docs/architecture/decisions" / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def git_init(cwd: Path) -> None:
    run(cwd, "git", "init", "-q")
    run(cwd, "git", "config", "user.email", "ci@example.invalid")
    run(cwd, "git", "config", "user.name", "CI")


def commit(cwd: Path, message: str) -> str:
    run(cwd, "git", "add", ".")
    run(cwd, "git", "commit", "-qm", message)
    return run(cwd, "git", "rev-parse", "HEAD").stdout.strip()


def check_case(name: str, mutate, expected_ok: bool) -> None:
    with tempfile.TemporaryDirectory() as tmp:
        repo = Path(tmp)
        git_init(repo)
        write(
            repo,
            "0001-example.md",
            "# ADR-0001: Example\n\nStatus: Accepted\nDate: 2026-01-01\n\n## Decision\n\nKeep history.\n",
        )
        write(
            repo,
            "0002-proposed.md",
            "# ADR-0002: Proposed\n\nStatus: Proposed\n\n## Decision\n\nStill open.\n",
        )
        base = commit(repo, "base")
        mutate(repo)

        result = run(
            repo,
            "python3",
            str(SCRIPT),
            "--base-ref",
            base,
            check=False,
        )
        ok = result.returncode == 0
        if ok != expected_ok:
            raise AssertionError(
                f"{name}: expected ok={expected_ok}, got {ok}\nstdout={result.stdout}\nstderr={result.stderr}"
            )
        print(f"PASS: {name}")


def main() -> None:
    check_case("unchanged accepted ADR", lambda repo: None, True)

    def edit_body(repo: Path) -> None:
        path = repo / "docs/architecture/decisions/0001-example.md"
        path.write_text(path.read_text().replace("Keep history.", "Rewrite history."))

    check_case("accepted ADR body edit is rejected", edit_body, False)

    def delete_accepted(repo: Path) -> None:
        (repo / "docs/architecture/decisions/0001-example.md").unlink()

    check_case("accepted ADR deletion is rejected", delete_accepted, False)

    def edit_proposed(repo: Path) -> None:
        path = repo / "docs/architecture/decisions/0002-proposed.md"
        path.write_text(path.read_text().replace("Still open.", "Still changing."))

    check_case("proposed ADR may change", edit_proposed, True)

    def supersede_without_new(repo: Path) -> None:
        path = repo / "docs/architecture/decisions/0001-example.md"
        text = path.read_text().replace(
            "Status: Accepted\n", "Status: Superseded\nSuperseded by: ADR-0003\n"
        )
        path.write_text(text)

    check_case("supersession requires matching new ADR", supersede_without_new, False)

    def supersede_with_proposed(repo: Path) -> None:
        path = repo / "docs/architecture/decisions/0001-example.md"
        text = path.read_text().replace(
            "Status: Accepted\n", "Status: Superseded\nSuperseded by: ADR-0003\n"
        )
        path.write_text(text)
        write(
            repo,
            "0003-replacement.md",
            "# ADR-0003: Replacement\n\nStatus: Proposed\nSupersedes: ADR-0001\n\n## Decision\n\nCandidate decision.\n",
        )

    check_case("proposed ADR cannot supersede accepted ADR", supersede_with_proposed, False)

    def valid_supersession(repo: Path) -> None:
        path = repo / "docs/architecture/decisions/0001-example.md"
        text = path.read_text().replace(
            "Status: Accepted\n", "Status: Superseded\nSuperseded by: ADR-0003\n"
        )
        path.write_text(text)
        write(
            repo,
            "0003-replacement.md",
            "# ADR-0003: Replacement\n\nStatus: Accepted\nSupersedes: ADR-0001\n\n## Decision\n\nNew decision.\n",
        )

    check_case("metadata-only supersession with matching accepted ADR is allowed", valid_supersession, True)

    print("All ADR immutability tests passed.")


if __name__ == "__main__":
    main()
