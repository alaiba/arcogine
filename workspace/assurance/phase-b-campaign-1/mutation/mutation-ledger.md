| Mutant | Selection | Outcome | Killing retained classes | Killing experiment classes | Seconds |
|---|---|---|---|---|---|
| `T-CTOR-ADMITS-MINUS-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-CANADD-NEG-OFF-BY-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-CANADD-POS-OFF-BY-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 11 |
| `T-PLUS-UNCHECKED` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-MINUS-UNCLAMPED` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 10 |
| `T-CRID-NO-VARIANT` | types | KILLED | ControlledRevisionIdTest | Campaign1ValueTimeExperimentTest | 11 |
| `T-CRID-NO-CANONICAL` | types | KILLED | ControlledRevisionIdTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-FP-CASE-FOLD` | types | KILLED | ModelFingerprintTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-MACHINEID-REVERSED` | types+factory | KILLED | FactoryModelCanonicalFormTest, MissingEligibleResourceOrderTest, EligibleMachineOrderTest, MachineIdTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest, Campaign1ValueTimeExperimentTest | 23 |
| `F-VAL-CONCURRENCY-ZERO` | factory | KILLED | FactoryModelArtifactTest, FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 30 |
| `F-VAL-DURATION-ZERO` | factory | KILLED | FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 30 |
| `F-VAL-NO-DUPLICATE-PRODUCT` | factory | KILLED | FactoryModelValidatorTest | Campaign1PublicationExperimentTest | 30 |
| `F-VAL-LONE-LOW-SURROGATE` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1PublicationExperimentTest | 30 |
| `F-VAL-HIGH-AT-END` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1PublicationExperimentTest | 25 |
| `F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE` | factory | SURVIVED | - | - | 32 |
| `F-VAL-DANGLING-POSITIVE-ONLY` | factory | KILLED (experiment only) | - | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-VAL-NO-OPERATION-REFERENCE` | factory | KILLED | FactoryModelArtifactTest, FactoryModelDefinitionBindingTest, FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 28 |
| `F-U2-HASH-ORDER` | factory | KILLED | MissingEligibleResourceOrderTest, EligibleMachineOrderTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-ENC-NO-SORT` | factory | SURVIVED | - | - | 30 |
| `F-ENC-RAW-NAN` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1CodecExperimentTest | 28 |
| `F-ENC-LONGBITS` | factory | SURVIVED | - | - | 31 |
| `F-ENC-PREFIX` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 27 |
| `F-ENC-LITTLE-ENDIAN` | factory | KILLED | FactoryModelSemanticComparatorTest, FactoryModelArtifactTest, FactoryModelCanonicalFormTest, FactoryModelDefinitionBindingTest | Campaign1ChangeAssemblyExperimentTest, Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 26 |
| `F-DEC-NO-TRAILING-CHECK` | factory | SURVIVED | - | - | 29 |
| `F-DEC-NO-ROUND-TRIP` | factory | KILLED | FactoryModelArtifactTest | Campaign1CodecExperimentTest | 28 |
| `F-DEC-NO-CONCURRENCY-RANGE` | factory | SURVIVED | - | - | 29 |
| `F-DEC-NEGATIVE-COUNT` | factory | KILLED (experiment only) | - | Campaign1CodecExperimentTest | 29 |
| `F-DEC-UTF8-REPLACE` | factory | SURVIVED | - | - | 29 |
| `F-DEC-ANY-MARKER` | factory | SURVIVED | - | - | 29 |
| `F-SUPPORT-ANY-ALGORITHM` | factory | KILLED | FactoryModelSemanticComparatorTest, FactoryModelArtifactTest | Campaign1CodecExperimentTest | 29 |
| `F-BINDING-DROPS-ELIGIBLE` | factory | KILLED | FactoryModelDefinitionBindingTest | Campaign1CodecExperimentTest | 28 |
| `F-VERSION-NO-VALIDATION` | factory | KILLED | FactoryModelArtifactTest, FactoryModelDefinitionBindingTest, FactoryRuntimeAssemblerTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 29 |
| `F-MODEL-ALIAS-RESOURCES` | factory | KILLED | FactoryModelImmutabilityTest | Campaign1PublicationExperimentTest | 30 |
| `F-STEP-ALIAS-ELIGIBLE` | factory | KILLED | FactoryModelImmutabilityTest, MissingEligibleResourceOrderTest, EligibleMachineOrderTest | Campaign1ChangeAssemblyExperimentTest, Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-CMP-NO-SETUP` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 29 |
| `F-CMP-UNBOXED-CAPACITY` | factory | KILLED (experiment only) | - | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-NO-ORDER` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-IGNORES-STEPS` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-NO-PRODUCT-OPERATION` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 28 |
| `F-ASM-DROPS-CAPACITY` | factory | KILLED | ConfiguredResourceMetadataTest | Campaign1ChangeAssemblyExperimentTest | 32 |
| `F-ASM-FIRST-ROUTING` | factory | KILLED (experiment only) | - | Campaign1ChangeAssemblyExperimentTest | 29 |
| `G-SNAPSHOT-UNVERIFIED` | seam | KILLED | ChangeSetFactoryTest | Campaign1ChangeSeamExperimentTest | 17 |
| `G-NO-EXTRACTOR-SUPPORT` | seam | KILLED | ChangeSetFactoryTest | - | 14 |
| `G-SNAPSHOT-BASE-FINGERPRINT` | seam | KILLED | ChangeSetFactoryTest | - | 14 |

Totals: KILLED 33, KILLED (experiment only) 4, SURVIVED 7
