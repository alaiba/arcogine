"""Run after run_controls.py: assembly frontier and exact-live-main retained regressions."""
import pathlib
import shutil
import subprocess
import sys

HERE = pathlib.Path(__file__).parent
ROOT = HERE.parents[2]
SOURCE = ROOT / "logs/source"
CONTAINER = "arcogine-541-challenge"
BASE = "a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4"

def run(stage, selection, expected):
    for module in ("types", "domains/factory", "storage", "governance", "simulation"):
        path = SOURCE / "product" / module / "build/test-results"
        if path.exists(): shutil.rmtree(path)
    with (ROOT / "logs" / (stage + ".log")).open("wb") as log:
        result = subprocess.run(["docker", "exec", CONTAINER, "sh", "./gradlew", "--console=plain", "--continue", *selection], stdout=log, stderr=subprocess.STDOUT)
    subprocess.run([sys.executable, "-I", str(HERE / "capture_run.py"), stage, str(result.returncode)], check=True)
    assert (result.returncode == 0) == (expected == 0), stage

shutil.copyfile(HERE / "IndependentFactoryChallengeTest.java", SOURCE / "product/domains/factory/src/test/java/com/arcogine/factory/model/IndependentFactoryChallengeTest.java")
run("assembly-frontier-baseline", [":factory:test", "--tests", "com.arcogine.factory.model.IndependentFactoryChallengeTest"], 0)
path = "product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java"
original = subprocess.check_output(["git", "show", BASE + ":" + path], cwd=ROOT)
old = b"for (OperationDefinition operation : model.operations()) {"
new = old + b"\n            if (model.products().stream().noneMatch(p -> p.operationId() == operation.id())) continue;"
try:
    assert original.count(old) == 1
    (SOURCE / path).write_bytes(original.replace(old, new))
    run("assembly-unreferenced-producer", [":factory:test", "--tests", "com.arcogine.factory.model.Campaign1ChangeAssemblyExperimentTest"], 0)
    run("assembly-unreferenced-independent", [":factory:test", "--tests", "com.arcogine.factory.model.IndependentFactoryChallengeTest.assemblyPreservesAnOperationWithNoReferencingProduct"], 1)
finally:
    (SOURCE / path).write_bytes(original)

files = [
    ("F-VAL-DANGLING-POSITIVE-ONLY", "validation/FactoryModelValidatorTest.java", "com.arcogine.factory.model.validation.FactoryModelValidatorTest"),
    ("F-CMP-UNBOXED-CAPACITY", "../change/FactoryModelSemanticComparatorTest.java", "com.arcogine.factory.change.FactoryModelSemanticComparatorTest"),
    ("F-ASM-FIRST-ROUTING", "FactoryRuntimeAssemblerTest.java", "com.arcogine.factory.model.FactoryRuntimeAssemblerTest"),
]
for _, suffix, _ in files:
    relative = pathlib.PurePosixPath("product/domains/factory/src/test/java/com/arcogine/factory/model") / suffix
    normalized = (ROOT / str(relative)).resolve().relative_to(ROOT).as_posix()
    (SOURCE / normalized).write_bytes(subprocess.check_output(["git", "show", "27b59eab19efd492d9d759cead4bfd8742fed856:" + normalized], cwd=ROOT))
selection = [":factory:test"]
for _, _, name in files: selection += ["--tests", name]
run("merged-retention-baseline", selection, 0)
for ident, _, name in files:
    try:
        subprocess.run(["docker", "exec", CONTAINER, "java", "/packet/tools/Mutator.java", "/packet/mutation/mutants.txt", "apply", ident, "/repo"], check=True)
        run("merged-retention-" + ident.lower(), [":factory:test", "--tests", name], 1)
    finally:
        for rel in ("product/types/src/main", "product/governance/src/main", "product/storage/src/main", "product/domains/factory/src/main"):
            subprocess.run(["git", "-C", str(SOURCE), "checkout", "--", rel], check=True)
run("final-restored", selection + ["--tests", "com.arcogine.factory.model.IndependentFactoryChallengeTest",
                                  ":storage:test", "--tests", "com.arcogine.governance.change.IndependentStorageChallengeTest"], 0)
