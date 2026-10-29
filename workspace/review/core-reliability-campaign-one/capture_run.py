"""Capture actual JUnit discovery, refusals and compiled class hashes for one isolated run."""
import hashlib
import json
import pathlib
import sys
import xml.etree.ElementTree as ET

root = pathlib.Path(__file__).resolve().parents[3]
source = root / "logs/source"
destination = pathlib.Path(__file__).parent / "results"
destination.mkdir(exist_ok=True)
suites = []
for module in ("types", "domains/factory", "storage", "governance", "simulation"):
    for file in sorted((source / "product" / module / "build/test-results/test").glob("*.xml")):
        xml = ET.parse(file).getroot()
        cases = []
        for case in xml.findall("testcase"):
            failures = [{"tag": child.tag, "type": child.get("type"), "message": child.get("message")}
                        for child in case if child.tag in ("failure", "error", "skipped")]
            cases.append({"class": case.get("classname"), "name": case.get("name"), "outcomes": failures})
        suites.append({"module": module, "name": xml.get("name"), "startedAt": xml.get("timestamp"),
                       "counts": {k: int(xml.get(k, "0")) for k in ("tests", "skipped", "failures", "errors")},
                       "xmlSha256": hashlib.sha256(file.read_bytes()).hexdigest(), "cases": cases})
classes = {}
for file in sorted((source / "product").glob("**/build/classes/java/**/*.class")):
    classes[file.relative_to(source).as_posix()] = hashlib.sha256(file.read_bytes()).hexdigest()
sources = {}
for file in sorted((source / "product").glob("**/src/**/*.java")):
    sources[file.relative_to(source).as_posix()] = hashlib.sha256(file.read_bytes()).hexdigest()
log = root / "logs" / (sys.argv[1] + ".log")
result = {"stage": sys.argv[1], "exit": int(sys.argv[2]), "suites": suites, "classesSha256": classes, "sourcesSha256": sources,
          "localLogSha256": hashlib.sha256(log.read_bytes()).hexdigest() if log.exists() else None}
target = destination / (sys.argv[1] + ".json")
target.write_text(json.dumps(result, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
counts = {k: sum(s["counts"][k] for s in suites) for k in ("tests", "skipped", "failures", "errors")}
print(sys.argv[1], "exit", sys.argv[2], "suites", len(suites), counts, "classes", len(classes))
assert suites, "no discovered tests"
