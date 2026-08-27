#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).with_name("check-markdown-links.py")
SPEC = importlib.util.spec_from_file_location("check_markdown_links", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
checker = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(checker)


class MarkdownLinkCheckerTest(unittest.TestCase):
    def test_markdown_target_parsing_policy(self) -> None:
        cases = [
            (
                "inline relative link",
                "See [guide](guide.md).",
                [(1, "guide.md")],
            ),
            (
                "reference-style definition",
                "See [guide][g].\n\n[g]: guide.md",
                [(3, "guide.md")],
            ),
            (
                "inline code historical path is ignored",
                "Historical path: `[removed](docs/removed.md)`.",
                [],
            ),
            (
                "fenced code historical path is ignored",
                "```text\n[removed](docs/removed.md)\n```",
                [],
            ),
            (
                "external link is still parsed before locality filtering",
                "[OpenAI](https://openai.com/)",
                [(1, "https://openai.com/")],
            ),
        ]

        for name, text, expected in cases:
            with self.subTest(name=name):
                self.assertEqual(expected, list(checker.markdown_targets(text)))

    def test_local_candidate_policy(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir).resolve()
            docs = repo / "docs"
            docs.mkdir()
            source = docs / "README.md"
            source.write_text("# Docs\n", encoding="utf-8")
            guide = docs / "guide.md"
            guide.write_text("# Guide\n", encoding="utf-8")

            cases = [
                (
                    "valid relative repository link",
                    "guide.md",
                    guide,
                ),
                (
                    "broken relative repository link",
                    "missing.md",
                    docs / "missing.md",
                ),
                (
                    "root repository link",
                    "/docs/guide.md",
                    guide,
                ),
                (
                    "root web route",
                    "/api/health",
                    None,
                ),
                (
                    "external URL",
                    "https://example.com/docs",
                    None,
                ),
                (
                    "in-page anchor",
                    "#section",
                    None,
                ),
            ]

            for name, target, expected in cases:
                with self.subTest(name=name):
                    self.assertEqual(expected, checker.local_candidate(repo, source, target))

            self.assertTrue(checker.local_candidate(repo, source, "guide.md").exists())
            self.assertFalse(checker.local_candidate(repo, source, "missing.md").exists())


if __name__ == "__main__":
    unittest.main()
