"""Compare every persisted original output with its freshly rerun counterpart."""
import hashlib
import json
import sys
from pathlib import Path

here = Path(__file__).resolve().parent
original = here.parent / "factory-design-game-strategy-space-experiment" / "results"
build = Path(sys.argv[1])
files = []
for old in sorted(original.rglob("*")):
    if not old.is_file():
        continue
    rel = old.relative_to(original)
    folder = "strategy-space" if rel.parts[0] == "pass-1" else "strategy-space-pass-2"
    new = build / folder / Path(*rel.parts[1:])
    before, after = old.read_bytes(), new.read_bytes()
    assert before == after, str(rel)
    files.append({"path": str(rel), "sha256": hashlib.sha256(after).hexdigest(), "bytes": len(after)})
output = {"main_baseline": "335908a154170655d4a6f694db8826980dee4547",
          "reviewed_report": "df23807640d68512d3ac9bee1b189d462c9efe08",
          "comparison": "byte-for-byte", "matched_files": len(files), "files": files}
(here / "results" / "reproduction.json").write_text(json.dumps(output, indent=2) + "\n")
print("Original output files reproduced byte-for-byte:", len(files))
