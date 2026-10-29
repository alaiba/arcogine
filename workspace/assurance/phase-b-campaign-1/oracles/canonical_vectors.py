"""Independent Factory canonical-byte oracle for Phase B Campaign 1, slice 2.

Written only from docs/architecture/factory-model.md sections 3 and 4 at the campaign source
baseline. It deliberately shares no code, helper or library with the Java product
(FactoryModelCanonicalForm): integers are framed with struct, text with Python's own UTF-8
codec (surrogatepass is NOT used, so an unpaired surrogate raises), binary64 with struct and
SHA-256 with hashlib.

Usage:  python -I canonical_vectors.py > canonical-vectors.json
"""

import hashlib
import json
import math
import struct
import sys

PREFIX = b"arcogine.factory-model\x00"
I64_MIN = -(2 ** 63)
I64_MAX = 2 ** 63 - 1
I32_MAX = 2 ** 31 - 1
CANONICAL_NAN = bytes.fromhex("7ff8000000000000")


def u64(n):
    assert 0 <= n < 2 ** 64
    return struct.pack(">Q", n)


def i64(n):
    assert I64_MIN <= n <= I64_MAX
    return struct.pack(">q", n)


def text(s):
    data = s.encode("utf-8")  # strict: an unpaired surrogate raises UnicodeEncodeError
    return u64(len(data)) + data


def optional_f64(x):
    if x is None:
        return b"\x00"
    if isinstance(x, float) and math.isnan(x):
        return b"\x01" + CANONICAL_NAN
    return b"\x01" + struct.pack(">d", x)


def encode(model):
    out = bytearray(PREFIX)
    out += u64(len(model["resources"]))
    for r in model["resources"]:
        out += i64(r["id"]) + text(r["name"]) + i64(r["concurrency"])
        out += optional_f64(r["capacityLiters"]) + i64(r["setupTime"])
    out += u64(len(model["operations"]))
    for o in model["operations"]:
        out += i64(o["id"]) + text(o["name"]) + u64(len(o["steps"]))
        for s in o["steps"]:
            out += i64(s["stepId"]) + text(s["name"]) + i64(s["duration"])
            eligible = sorted(set(s["eligibleResources"]))  # set-shaped, ascending signed
            out += u64(len(eligible))
            for m in eligible:
                out += i64(m)
    out += u64(len(model["products"]))
    for p in model["products"]:
        out += i64(p["id"]) + text(p["name"]) + i64(p["operationId"])
    return bytes(out)


def fingerprint(data):
    return "factory-model:sha256:" + hashlib.sha256(data).hexdigest()


def resource(id, name, concurrency, capacity, setup):
    return {"id": id, "name": name, "concurrency": concurrency,
            "capacityLiters": capacity, "setupTime": setup}


def step(step_id, name, duration, eligible):
    return {"stepId": step_id, "name": name, "duration": duration, "eligibleResources": eligible}


VECTORS = {
    "empty": {"resources": [], "operations": [], "products": []},
    "representative": {
        "resources": [
            resource(1, "Mixer", 2, 1500.5, 30),
            resource(-7, "Prüf;stand|A", 1, None, 0),
        ],
        "operations": [
            {"id": 10, "name": "Blend", "steps": [
                step(1, "Load", 5, [1, -7]),
                step(2, "Mix \U0001F300", 9, [1]),
            ]},
        ],
        "products": [{"id": 100, "name": "Paint", "operationId": 10}],
    },
    "signed-extremes": {
        "resources": [
            resource(I64_MAX, "max", I32_MAX, -0.0, I64_MIN),
            resource(I64_MIN, "min", 1, 0.0, I64_MAX),
            resource(0, "nan", 1, float("nan"), -1),
            resource(-1, "inf", 1, float("inf"), 0),
            resource(2, "neg-inf", 1, float("-inf"), 0),
        ],
        "operations": [
            {"id": I64_MIN, "name": "", "steps": [
                step(I64_MAX, "\u0000:|;\n", I64_MAX, [I64_MAX, I64_MIN, 0, -1, 2]),
                step(I64_MAX, "dup-step-id", 1, [I64_MIN]),
            ]},
        ],
        "products": [
            {"id": I64_MIN, "name": "日本", "operationId": I64_MIN},
            {"id": I64_MAX, "name": "\U0010FFFF", "operationId": I64_MIN},
        ],
    },
}


def annotate(model):
    """Field-by-field derivation: (offset, field path, hex) rows that concatenate to encode(model)."""
    rows = []
    offset = 0

    def emit(path, data):
        nonlocal offset
        rows.append("%5d  %-46s %s" % (offset, path, data.hex()))
        offset += len(data)

    emit("prefix", PREFIX)
    emit("resources.count U64", u64(len(model["resources"])))
    for i, r in enumerate(model["resources"]):
        emit("resources[%d].id I64" % i, i64(r["id"]))
        emit("resources[%d].name TEXT %r" % (i, r["name"]), text(r["name"]))
        emit("resources[%d].concurrency I64" % i, i64(r["concurrency"]))
        emit("resources[%d].capacityLiters OPTIONAL_F64 %r" % (i, r["capacityLiters"]), optional_f64(r["capacityLiters"]))
        emit("resources[%d].setupTime I64" % i, i64(r["setupTime"]))
    emit("operations.count U64", u64(len(model["operations"])))
    for i, o in enumerate(model["operations"]):
        emit("operations[%d].id I64" % i, i64(o["id"]))
        emit("operations[%d].name TEXT %r" % (i, o["name"]), text(o["name"]))
        emit("operations[%d].steps.count U64" % i, u64(len(o["steps"])))
        for j, s in enumerate(o["steps"]):
            p = "operations[%d].steps[%d]" % (i, j)
            emit(p + ".stepId I64", i64(s["stepId"]))
            emit(p + ".name TEXT %r" % s["name"], text(s["name"]))
            emit(p + ".duration I64", i64(s["duration"]))
            eligible = sorted(set(s["eligibleResources"]))
            emit(p + ".eligible.count U64", u64(len(eligible)))
            for m in eligible:
                emit(p + ".eligible ascending I64 %d" % m, i64(m))
    emit("products.count U64", u64(len(model["products"])))
    for i, pr in enumerate(model["products"]):
        emit("products[%d].id I64" % i, i64(pr["id"]))
        emit("products[%d].name TEXT %r" % (i, pr["name"]), text(pr["name"]))
        emit("products[%d].operationId I64" % i, i64(pr["operationId"]))
    return rows, offset


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--annotate":
        for name, model in VECTORS.items():
            rows, length = annotate(model)
            assert length == len(encode(model))
            sys.stdout.write("## %s (%d bytes, %s)\n" % (name, length, fingerprint(encode(model))))
            sys.stdout.write("\n".join(rows) + "\n\n")
        return
    result = {}
    for name, model in VECTORS.items():
        data = encode(model)
        result[name] = {"length": len(data), "hex": data.hex(), "fingerprint": fingerprint(data)}
    # Ill-formed Unicode must be unencodable by this oracle, mirroring the publication predicate.
    try:
        text("\ud800")
        raise SystemExit("oracle accepted an unpaired surrogate")
    except UnicodeEncodeError:
        pass
    json.dump(result, sys.stdout, indent=2, sort_keys=True)
    sys.stdout.write("\n")


if __name__ == "__main__":
    main()
