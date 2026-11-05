# Mutation triage

Producer assessment only. Automatic classification is retained separately. Full failure payloads and discovery are linked per row. No timeout, compile or discovery failure occurred in these 47 runs. No unreachable mutant was inferred.

| Control | Outcome | Rationale and evidence |
|---|---|---|
| CH1-MESSAGE | DETECTED — diagnostic observation only | Non-resource Unicode diagnostic wording changes; full literal sequences must observe it. Required semantic admission/refusal class is unchanged; do not count as a semantic violation. [Observed tests/failures](results/CH1-MESSAGE/run.json) |
| CH2-CATEGORY | KILLED — bounded semantic control | Candidate mismatch is reported as STORAGE_INTEGRITY instead of FINGERPRINT_MISMATCH. [Observed tests/failures](results/CH2-CATEGORY/run.json) |
| CH3-UNREFERENCED | KILLED — bounded semantic control | Published operation with no product reference disappears; minimal routing 10 witness gets UnknownId. Manual triage: both executed test bodies reach RoutingStore and throw UnknownId for an authored operation. Baseline and restoration resolve it; full discovery and single-source delta verified. This is the intended behavioral discrepancy, not test discovery failure. [Observed tests/failures](results/CH3-UNREFERENCED/run.json) |
| F-ASM-DROPS-CAPACITY | KILLED — bounded semantic control | assembled machines lose capacityLiters [Observed tests/failures](results/F-ASM-DROPS-CAPACITY/run.json) |
| F-ASM-FIRST-ROUTING | KILLED — bounded semantic control | every product routed to the first operation [Observed tests/failures](results/F-ASM-FIRST-ROUTING/run.json) |
| F-BINDING-DROPS-ELIGIBLE | DETECTED — binding inclusion observation | definition binding omits a class verification executes Class-load/binding inclusion witness only; no claim of complete executed control flow. [Observed tests/failures](results/F-BINDING-DROPS-ELIGIBLE/run.json) |
| F-CMP-IGNORES-STEPS | KILLED — bounded semantic control | operation equality ignores steps [Observed tests/failures](results/F-CMP-IGNORES-STEPS/run.json) |
| F-CMP-NO-ORDER | KILLED — bounded semantic control | list-position changes ignored [Observed tests/failures](results/F-CMP-NO-ORDER/run.json) |
| F-CMP-NO-PRODUCT-OPERATION | KILLED — bounded semantic control | product operationId change ignored [Observed tests/failures](results/F-CMP-NO-PRODUCT-OPERATION/run.json) |
| F-CMP-NO-SETUP | KILLED — bounded semantic control | setupTime change ignored [Observed tests/failures](results/F-CMP-NO-SETUP/run.json) |
| F-CMP-UNBOXED-CAPACITY | KILLED — bounded semantic control | capacity compared numerically (-0.0 == 0.0, NaN != NaN) [Observed tests/failures](results/F-CMP-UNBOXED-CAPACITY/run.json) |
| F-DEC-ANY-MARKER | SURVIVED — redundant at required refusal boundary | any non-zero optional-f64 marker read as present [Observed tests/failures](results/F-DEC-ANY-MARKER/run.json) |
| F-DEC-NEGATIVE-COUNT | DETECTED — diagnostic observation only | negative counts no longer refused at the count Required semantic admission/refusal class is unchanged; do not count as a semantic violation. [Observed tests/failures](results/F-DEC-NEGATIVE-COUNT/run.json) |
| F-DEC-NO-CONCURRENCY-RANGE | SURVIVED — redundant at required refusal boundary | 32-bit concurrency range guard removed (int cast wraps) [Observed tests/failures](results/F-DEC-NO-CONCURRENCY-RANGE/run.json) |
| F-DEC-NO-ROUND-TRIP | KILLED — bounded semantic control | canonical re-encoding check removed [Observed tests/failures](results/F-DEC-NO-ROUND-TRIP/run.json) |
| F-DEC-NO-TRAILING-CHECK | SURVIVED — redundant at required refusal boundary | trailing-byte guard removed (canonical round-trip still refuses) [Observed tests/failures](results/F-DEC-NO-TRAILING-CHECK/run.json) |
| F-DEC-UTF8-REPLACE | SURVIVED — redundant at required refusal boundary | decoder replaces malformed UTF-8 instead of reporting it [Observed tests/failures](results/F-DEC-UTF8-REPLACE/run.json) |
| F-ENC-LITTLE-ENDIAN | KILLED — bounded semantic control | I64/U64 written little-endian (decoder unchanged) Manual triage: independently literal golden-byte comparison fails at integer byte order. Additional parsing/history exceptions are downstream effects. [Observed tests/failures](results/F-ENC-LITTLE-ENDIAN/run.json) |
| F-ENC-LONGBITS | SURVIVED — equivalent control | expected EQUIVALENT control - doubleToLongBits canonicalizes NaN identically [Observed tests/failures](results/F-ENC-LONGBITS/run.json) |
| F-ENC-NO-SORT | SURVIVED — equivalent control | expected EQUIVALENT control - eligible sets already iterate ascending [Observed tests/failures](results/F-ENC-NO-SORT/run.json) |
| F-ENC-PREFIX | KILLED — bounded semantic control | definition prefix terminator changed Manual triage: independently literal golden-byte comparison fails at the definition prefix. Additional decoding exceptions are downstream effects. [Observed tests/failures](results/F-ENC-PREFIX/run.json) |
| F-ENC-RAW-NAN | KILLED — bounded semantic control | NaN payload no longer canonicalized [Observed tests/failures](results/F-ENC-RAW-NAN/run.json) |
| F-MODEL-ALIAS-RESOURCES | KILLED — bounded semantic control | resources list aliased instead of copied [Observed tests/failures](results/F-MODEL-ALIAS-RESOURCES/run.json) |
| F-STEP-ALIAS-ELIGIBLE | KILLED — bounded semantic control | eligible set aliased (no copy, no ascending order) [Observed tests/failures](results/F-STEP-ALIAS-ELIGIBLE/run.json) |
| F-SUPPORT-ANY-ALGORITHM | KILLED — bounded semantic control | support ignores the algorithm [Observed tests/failures](results/F-SUPPORT-ANY-ALGORITHM/run.json) |
| F-U2-HASH-ORDER | KILLED — bounded semantic control | eligible sets revert to per-JVM hash iteration [Observed tests/failures](results/F-U2-HASH-ORDER/run.json) |
| F-VAL-CONCURRENCY-ZERO | KILLED — bounded semantic control | concurrency 0 admitted [Observed tests/failures](results/F-VAL-CONCURRENCY-ZERO/run.json) |
| F-VAL-DANGLING-POSITIVE-ONLY | KILLED — bounded semantic control | dangling references with non-positive IDs admitted [Observed tests/failures](results/F-VAL-DANGLING-POSITIVE-ONLY/run.json) |
| F-VAL-DURATION-ZERO | KILLED — bounded semantic control | duration 0 admitted [Observed tests/failures](results/F-VAL-DURATION-ZERO/run.json) |
| F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE | SURVIVED — equivalent control | expected EQUIVALENT control - the skipped loop is over an empty set [Observed tests/failures](results/F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE/run.json) |
| F-VAL-HIGH-AT-END | KILLED — bounded semantic control | trailing high surrogate bounds check removed (index error instead of refusal) Manual triage: validator rejects a lone trailing high surrogate via StringIndexOutOfBoundsException instead of structured validation; stack reaches rejectMalformedUnicode in the selected test body. Baseline and restoration pass. [Observed tests/failures](results/F-VAL-HIGH-AT-END/run.json) |
| F-VAL-LONE-LOW-SURROGATE | KILLED — bounded semantic control | lone low surrogate admitted [Observed tests/failures](results/F-VAL-LONE-LOW-SURROGATE/run.json) |
| F-VAL-NO-DUPLICATE-PRODUCT | KILLED — bounded semantic control | duplicate product IDs admitted [Observed tests/failures](results/F-VAL-NO-DUPLICATE-PRODUCT/run.json) |
| F-VAL-NO-OPERATION-REFERENCE | KILLED — bounded semantic control | dangling product operation admitted [Observed tests/failures](results/F-VAL-NO-OPERATION-REFERENCE/run.json) |
| F-VERSION-NO-VALIDATION | KILLED — bounded semantic control | FactoryModelVersion (decode path) no longer validates; publisher still does [Observed tests/failures](results/F-VERSION-NO-VALIDATION/run.json) |
| G-NO-EXTRACTOR-SUPPORT | SURVIVED — uncovered guard | extractor support not checked before comparison Current real comparator supports both artifacts; no unsupported-extractor case was exercised. [Observed tests/failures](results/G-NO-EXTRACTOR-SUPPORT/run.json) |
| G-SNAPSHOT-BASE-FINGERPRINT | KILLED — bounded semantic control | snapshot change set records the base fingerprint as candidate fingerprint [Observed tests/failures](results/G-SNAPSHOT-BASE-FINGERPRINT/run.json) |
| G-SNAPSHOT-UNVERIFIED | KILLED — bounded semantic control | candidate snapshot fingerprint binding not verified [Observed tests/failures](results/G-SNAPSHOT-UNVERIFIED/run.json) |
| T-CANADD-NEG-OFF-BY-ONE | KILLED — bounded semantic control | canAdd refuses delta == -value (exact return to zero) [Observed tests/failures](results/T-CANADD-NEG-OFF-BY-ONE/run.json) |
| T-CANADD-POS-OFF-BY-ONE | KILLED — bounded semantic control | canAdd refuses delta reaching exactly Long.MAX_VALUE [Observed tests/failures](results/T-CANADD-POS-OFF-BY-ONE/run.json) |
| T-CRID-NO-CANONICAL | KILLED — bounded semantic control | parse accepts non-canonical spellings [Observed tests/failures](results/T-CRID-NO-CANONICAL/run.json) |
| T-CRID-NO-VARIANT | KILLED — bounded semantic control | ControlledRevisionId accepts any variant [Observed tests/failures](results/T-CRID-NO-VARIANT/run.json) |
| T-CTOR-ADMITS-MINUS-ONE | KILLED — bounded semantic control | SimTime construction admits -1 [Observed tests/failures](results/T-CTOR-ADMITS-MINUS-ONE/run.json) |
| T-FP-CASE-FOLD | KILLED — bounded semantic control | sha256 digest accepts uppercase hex [Observed tests/failures](results/T-FP-CASE-FOLD/run.json) |
| T-MACHINEID-REVERSED | KILLED — bounded semantic control | MachineId ordering reversed (eligible-set and canonical order) Manual triage: direct numeric ordering assertion expects -1 and observes 1. Additional decode exceptions are downstream effects, not the sole kill evidence. [Observed tests/failures](results/T-MACHINEID-REVERSED/run.json) |
| T-MINUS-UNCLAMPED | KILLED — bounded semantic control | minus returns the raw negative difference [Observed tests/failures](results/T-MINUS-UNCLAMPED/run.json) |
| T-PLUS-UNCHECKED | KILLED — bounded semantic control | plus skips the representability guard (wraps or constructs negative) [Observed tests/failures](results/T-PLUS-UNCHECKED/run.json) |
