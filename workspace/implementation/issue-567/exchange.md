## REPORT — Foundation dependency has not landed

- Kind: REPORT
- From / To: Implementer -> Owner
- Work source: issue #567
- Revision: issue/567-npm-audit based on main at 12d2e6c

The npm manifest and lockfile targeted by issue #567 are absent from live main. Their foundation is in open PR #552, at head b1e48acecbbd9f845eec1364f3cdcb83134a0b01 when inspected. Its CI workflow also introduces game browser checks and changes the aggregate gate, so implementing against main now cannot validate the requested scan against the actual lockfile or reconcile the final CI job graph. Resume issue #567 after the foundation lands, grounding the change in the then-current main and issue.
