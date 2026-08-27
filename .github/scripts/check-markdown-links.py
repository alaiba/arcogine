#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

INLINE_LINK = re.compile(r"!?\[[^\]]*\]\(([^)]+)\)")
REFERENCE_LINK = re.compile(r"^\s*\[[^\]]+\]:\s*(\S+)")
INLINE_CODE = re.compile(r"`[^`]*`")
FENCE = re.compile(r"^\s*(```+|~~~+)")
EXTERNAL_SCHEMES = {"http", "https", "mailto", "tel", "data", "javascript"}


def destination(raw: str) -> str:
    raw = raw.strip()
    if raw.startswith("<") and ">" in raw:
        return raw[1 : raw.index(">")]
    return raw.split(maxsplit=1)[0]


def markdown_targets(text: str):
    in_fence = False
    fence_char = ""
    for line_no, line in enumerate(text.splitlines(), 1):
        match = FENCE.match(line)
        if match:
            marker = match.group(1)
            char = marker[0]
            if not in_fence:
                in_fence = True
                fence_char = char
            elif char == fence_char:
                in_fence = False
                fence_char = ""
            continue
        if in_fence:
            continue

        cleaned = INLINE_CODE.sub("", line)
        for match in INLINE_LINK.finditer(cleaned):
            yield line_no, destination(match.group(1))
        ref = REFERENCE_LINK.match(cleaned)
        if ref:
            yield line_no, destination(ref.group(1))


def local_candidate(repo: Path, source: Path, raw_target: str) -> Path | None:
    if not raw_target or raw_target.startswith("#"):
        return None

    parsed = urlsplit(raw_target)
    if parsed.scheme.lower() in EXTERNAL_SCHEMES or parsed.netloc:
        return None

    path_text = unquote(parsed.path)
    if not path_text:
        return None

    if path_text.startswith("/"):
        relative = Path(path_text.lstrip("/"))
        if not relative.parts:
            return None
        # Root-style web routes (for example /api/health) are not repository links.
        if not (repo / relative.parts[0]).exists() and relative.suffix.lower() != ".md":
            return None
        candidate = repo / relative
    else:
        candidate = source.parent / path_text

    try:
        resolved = candidate.resolve(strict=False)
        resolved.relative_to(repo)
    except (OSError, ValueError):
        return candidate
    return resolved


def main() -> int:
    repo = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    failures: list[str] = []

    for source in sorted(repo.rglob("*.md")):
        if ".git" in source.parts:
            continue
        try:
            text = source.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for line_no, target in markdown_targets(text):
            candidate = local_candidate(repo, source, target)
            if candidate is None:
                continue
            if not candidate.exists():
                try:
                    display = candidate.relative_to(repo)
                except ValueError:
                    display = candidate
                failures.append(
                    f"{source.relative_to(repo)}:{line_no}: broken local link {target!r} -> {display}"
                )

    if failures:
        print("Broken repository-local Markdown links found:", file=sys.stderr)
        for failure in failures:
            print(f"  {failure}", file=sys.stderr)
        return 1

    print("Repository-local Markdown links are valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
