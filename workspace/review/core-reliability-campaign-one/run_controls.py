"""Bounded independent controls; mutate only logs/source, restore every changed source in finally."""
import pathlib
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[3]
HERE = pathlib.Path(__file__).parent
SOURCE = ROOT / "logs/source"
CONTAINER = "arcogine-541-challenge"
BASE = "a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4"

def run(stage, args, expected):
    for module in ("types", "domains/factory", "storage", "governance", "simulation"):
        target = SOURCE / "product" / module / "build/test-results"
        if target.exists():
            shutil.rmtree(target)
    with (ROOT / "logs" / (stage + ".log")).open("wb") as log:
        outcome = subprocess.run(["docker", "exec", CONTAINER, "sh", "./gradlew", "--console=plain", "--continue", *args], stdout=log, stderr=subprocess.STDOUT)
    subprocess.run([sys.executable, "-I", str(HERE / "capture_run.py"), stage, str(outcome.returncode)], check=True)
    print(stage, "expected", expected, "observed", outcome.returncode, flush=True)
    assert (outcome.returncode == 0) == (expected == 0), stage

def original(path):
    return subprocess.check_output(["git", "show", BASE + ":" + path], cwd=ROOT)

def replace(path, before, after):
    data = original(path)
    assert before in data
    (SOURCE / path).write_bytes(data.replace(before, after))

validator = "product/domains/factory/src/main/java/com/arcogine/factory/model/validation/FactoryModelValidator.java"
storage = "product/storage/src/main/java/com/arcogine/storage/FileArcogineStorage.java"
try:
    replace(validator, b'"must contain only valid Unicode scalar values"',
            b'field.startsWith("resources[") ? "must contain only valid Unicode scalar values" : "CHALLENGE changed text"')
    run("diagnostic-message-producer", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1PublicationExperimentTest"], 0)
    run("diagnostic-message-independent", [":factory:test", "--tests", "com.arcogine.factory.model.IndependentFactoryChallengeTest.fullDiagnosticWitnessPreservesEveryFieldAndMessage"], 1)
finally:
    (SOURCE / validator).write_bytes(original(validator))

try:
    replace(storage, b"storedArtifact ? STORAGE_INTEGRITY : FINGERPRINT_MISMATCH;", b"storedArtifact ? STORAGE_INTEGRITY : STORAGE_INTEGRITY;")
    run("storage-category-producer", [":storage:test", "--tests", "com.arcogine.governance.change.Campaign1ChangeSeamExperimentTest"], 0)
    run("storage-category-independent", [":storage:test", "--tests", "com.arcogine.governance.change.IndependentStorageChallengeTest"], 1)
finally:
    (SOURCE / storage).write_bytes(original(storage))

mutants = [
    ("T-CANADD-POS-OFF-BY-ONE", [":types:test", "--tests", "com.arcogine.types.Campaign1ValueTimeExperimentTest"], 1),
    ("F-DEC-NO-ROUND-TRIP", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1CodecExperimentTest"], 1),
    ("F-DEC-NO-TRAILING-CHECK", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1CodecExperimentTest"], 0),
    ("F-DEC-NEGATIVE-COUNT", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1CodecExperimentTest"], 1),
    ("F-ENC-RAW-NAN", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1CodecExperimentTest"], 1),
    ("G-SNAPSHOT-UNVERIFIED", [":storage:test", "--tests", "com.arcogine.governance.change.Campaign1ChangeSeamExperimentTest"], 1),
]
for ident, selection, expected in mutants:
    try:
        subprocess.run(["docker", "exec", CONTAINER, "java", "/packet/tools/Mutator.java", "/packet/mutation/mutants.txt", "apply", ident, "/repo"], check=True)
        run("spot-" + ident.lower(), selection, expected)
    finally:
        for rel in ("product/types/src/main", "product/governance/src/main", "product/storage/src/main", "product/domains/factory/src/main"):
            subprocess.run(["git", "-C", str(SOURCE), "checkout", "--", rel], check=True)

run("restored-independent", [":factory:test", "--tests", "com.arcogine.factory.model.IndependentFactoryChallengeTest",
                             ":storage:test", "--tests", "com.arcogine.governance.change.IndependentStorageChallengeTest"], 0)
