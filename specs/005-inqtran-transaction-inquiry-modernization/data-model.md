# Data Model - Feature 005 INQTRAN Transaction List Modernization

## 1. Purpose
This artifact defines the conceptual and logical data model for the INQTRAN transaction-list inquiry modernization.

It describes domain representations, inquiry controls, result structures, persistence representation, derived values, invariants, and unresolved modeling decisions.

It does not define API routes, protocol outcomes, final external schemas, implementation tasks, or OpenAPI contract syntax.

## 2. Authoritative Inputs
Frozen upstream artifacts used as authoritative inputs:
- supporting/program-analysis.md
- supporting/dependency-map.md
- supporting/business-rules.md
- supporting/mapping-matrix.md
- supporting/intended-system.md
- supporting/architecture.md
- research.md

Evidence status labels used in this document:
- Confirmed upstream constraint
- Approved modernization direction
- Data-model design decision
- Remaining uncertainty

Dependency direction for this artifact:
- Mapping Matrix and Research -> Data Model -> Requirements, Specification, and OpenAPI

## 3. Modelling Principles
- Separate model layers: raw controls, normalized controls, domain transaction data, result metadata, and persistence representation. (Data-model design decision)
- Preserve behavior-defining semantics from upstream artifacts before introducing modernization abstractions. (Confirmed upstream constraint)
- Preserve exact-value significance for identifiers and amount precision. (Confirmed upstream constraint)
- Defer unresolved external contract representation details to downstream artifacts. (Approved modernization direction)
- Avoid inventing new business attributes not evidenced upstream. (Confirmed upstream constraint)

## 4. Conceptual Domain Model
Primary model concepts:
- AccountIdentity
	- sortCode
	- accountNumber
- TransactionListInquiry
	- raw controls provided for an account identity
- TransactionRecord
	- one transaction row representation for inquiry output
- TransactionIdentifier (derived value)
	- deterministic identifier derived from transaction record components
- InquiryResult
	- metadata plus transaction collection

Model layering distinction:
- Raw inquiry controls are not the same as normalized effective controls.
- Domain transaction representation is not the same as persistence row representation.
- External contract representation is downstream and not finalized here.

Classification:
- Data-model design decision constrained by confirmed upstream behavior

## 5. Inquiry Control Model
Raw inquiry control values:
- sortCode (required identity component)
- accountNumber (required identity component)
- fromDateRaw (optional raw lower-bound control)
- toDateRaw (optional raw upper-bound control)
- limitRequested (raw requested page size)
- offsetRequested (raw requested row skip count)

Normalized/effective inquiry controls:
- lowerDateBoundNormalized (optional normalized lower bound)
- upperDateBoundNormalized (optional normalized upper bound)
- limitEffective
- offsetEffective

Normalization rules represented by this model:
- limitRequested `0` normalizes to limitEffective `50`. (Confirmed upstream constraint)
- limitRequested values greater than `100` normalize to limitEffective `100`. (Confirmed upstream constraint)
- offset is applied after filtering and ordering. (Confirmed upstream constraint)
- legacy date sentinels are control signals, not meaningful calendar dates. (Confirmed upstream constraint plus remaining uncertainty)

## 6. Transaction Record Model
Logical transaction record attributes:
- transactionIdentifier (derived)
- sortCode
- accountNumber
- transactionDate
- transactionTime
- transactionReference
- transactionType
- transactionDescription
- transactionAmount

Representation principles:
- sortCode and accountNumber preserve leading-zero significance. (Confirmed upstream constraint)
- transactionAmount uses exact decimal representation, not floating-point. (Confirmed upstream constraint)
- fixed-width sourced values require compatibility-preserving handling; silent destructive normalization is disallowed. (Confirmed upstream constraint plus remaining uncertainty)

## 7. Inquiry Result Model
Inquiry result structure:
- account identity context (sortCode, accountNumber)
- effective controls context (normalized bounds, limitEffective, offsetEffective)
- totalCount
- returnedCount
- transactions (collection of transaction records)
- success state representation (external expression remains downstream)

Count semantics:
- totalCount: number of matching rows before pagination.
- returnedCount: number of rows returned after pagination.

Classification:
- Confirmed upstream constraint

## 8. Relationships and Cardinality
- One AccountIdentity has zero to many TransactionRecord entries in an inquiry result. (Confirmed upstream constraint)
- InquiryResult has exactly one totalCount value and one returnedCount value. (Data-model design decision)
- InquiryResult has zero to many transactions. (Confirmed upstream constraint)
- Persistence source relationship to a separate account table is not evidenced. (Remaining uncertainty)
- TransactionIdentifier is derived from transaction attributes and is not modeled as a proven persistent primary key. (Confirmed upstream constraint plus remaining uncertainty)

## 9. Derived Values
Derived value: TransactionIdentifier

Source components:
- sortCode
- accountNumber
- transactionDate
- transactionTime
- transactionReference

Derived-value assertions:
- Deterministic derivation is confirmed upstream.
- Database uniqueness is not confirmed.
- It is not a proven primary key.
- Padding behavior must not be silently changed.
- Exact external serialization details are deferred downstream except where mapping evidence fixes component ordering.

Classification:
- Confirmed upstream constraint plus remaining uncertainty

## 10. Data Invariants
Success-path invariants:
- transaction collection is never null on success.
- returnedCount == transactions.size().
- returnedCount does not exceed limitEffective.
- returnedCount does not exceed 100.
- totalCount >= returnedCount.
- capability remains read-only.
- transactionIdentifier is derived and not claimed as globally unique.
- transactionAmount preserves exact decimal precision semantics.

Behavioral invariants tied to upstream constraints:
- ordering is by date descending then time descending.
- no partial successful result after technical retrieval failure.

Classification:
- Confirmed upstream constraint plus data-model design decision

## 11. Persistence Model

| Logical attribute | Legacy/source column | Source type | Proposed H2 type | Nullability | Key status | Domain exposure | Mapping notes | Confidence/uncertainty |
|---|---|---|---|---|---|---|---|---|
| sourceEyecatcher | PROCTRAN_EYECATCHER | CHAR(4) | CHAR(4) | nullable in source declaration | not a proven key | excluded from modern transaction result | retain only for source compatibility where required | Confirmed source presence; business-use absence confirmed |
| sortCode | PROCTRAN_SORTCODE | CHAR(6) NOT NULL | CHAR(6) | not null | part of filter identity, not proven table key | exposed | preserve fixed-width and leading-zero significance | Confirmed upstream constraint |
| accountNumber | PROCTRAN_NUMBER | CHAR(8) NOT NULL | CHAR(8) | not null | part of filter identity, not proven table key | exposed | preserve fixed-width and leading-zero significance | Confirmed upstream constraint |
| transactionDateSource | PROCTRAN_DATE | CHAR(8) per PROCDB2 declaration | CHAR(8) or VARCHAR(10) compatibility decision deferred | source nullable | not a proven key | exposed as normalized domain date representation | host/path evidence uses X(10) ISO conversion; compatibility unresolved | Remaining uncertainty |
| transactionTime | PROCTRAN_TIME | CHAR(6) | CHAR(6) | source nullable | not a proven key | exposed | character-to-numeric-display legacy mapping compatibility unresolved | Remaining uncertainty |
| transactionReference | PROCTRAN_REF | CHAR(12) | CHAR(12) | source nullable | component of derived identifier, not proven key | exposed | legacy output model has numeric-display mismatch risk for nonnumeric values | Remaining uncertainty |
| transactionType | PROCTRAN_TYPE | CHAR(3) | CHAR(3) | source nullable | not a proven key | exposed | fixed-width compatibility retained; trimming policy unresolved | Remaining uncertainty |
| transactionDescription | PROCTRAN_DESC | CHAR(40) | CHAR(40) | source nullable | not a proven key | exposed | fixed-width compatibility retained; trimming policy unresolved | Remaining uncertainty |
| transactionAmount | PROCTRAN_AMOUNT | DECIMAL(12,2) | DECIMAL(12,2) | source nullable | not a proven key | exposed | exact decimal semantics required; no floating-point substitution | Confirmed upstream constraint with nullability uncertainty |

Persistence-model prohibitions:
- Do not invent primary key, audit fields, status/category/balance/merchant/currency/posting metadata.

Eyecatcher treatment:
- retained only for source compatibility concerns where needed,
- does not participate in inquiry filtering behavior,
- excluded from modern domain transaction result and external result model.

## 12. Source-to-Target Type Mapping

| Source layer element | Target data-model element | Mapping intent | Classification |
|---|---|---|---|
| COMMAREA sortCode numeric-display signal | AccountIdentity.sortCode | preserve exact identifier semantics including leading zeros in modern representation | Confirmed upstream constraint |
| COMMAREA account number numeric-display signal | AccountIdentity.accountNumber | preserve exact identifier semantics including leading zeros | Confirmed upstream constraint |
| Raw from/to control signals and sentinel states | lowerDateBoundNormalized, upperDateBoundNormalized | normalize control intent; do not treat sentinels as meaningful business calendar dates | Confirmed upstream constraint plus remaining uncertainty |
| Legacy limit control | limitRequested -> limitEffective | enforce `0 -> 50`, cap `>100 -> 100` | Confirmed upstream constraint |
| Legacy offset control | offsetRequested -> offsetEffective | apply after filtering and ordering | Confirmed upstream constraint |
| PROCTRAN row values | TransactionRecord attributes | preserve value semantics with explicit compatibility handling for fixed-width and type mismatches | Confirmed upstream constraint plus remaining uncertainty |
| Count cursor result | totalCount | pre-pagination match cardinality | Confirmed upstream constraint |
| Accepted row count | returnedCount | post-pagination returned cardinality | Confirmed upstream constraint |

## 13. Nullability and Fixed-Width Handling
Nullability stance:
- No explicit indicator-variable handling is evidenced upstream for nullable source columns.
- Modern data model must not assume permissive null mapping behavior without downstream approval and verified behavior.

Fixed-width stance:
- Leading zeros for sortCode/accountNumber are semantically significant.
- Fixed-width fields must not be silently trimmed where meaning could change.
- Reference, time, and other fixed-width fields require compatibility-preserving handling.

Classification:
- Confirmed upstream constraint plus remaining uncertainty

## 14. Sentinel and Control Normalization Model
Control-normalization representation:
- fromDateRaw and toDateRaw may include legacy sentinel signals.
- lowerDateBoundNormalized and upperDateBoundNormalized represent effective model controls after normalization.
- limitRequested and limitEffective are distinct.
- offsetRequested and offsetEffective are distinct (offsetEffective equals requested value unless downstream policy introduces an approved rule).

Model intent:
- represent sentinel controls as normalization inputs,
- do not model sentinel values as business calendar dates.

Classification:
- Confirmed upstream constraint plus data-model design decision plus remaining uncertainty

## 15. Empty Successful Result Representation
For successful empty result:
- transactions is an empty, non-null collection.
- returnedCount is `0`.
- totalCount is `0` when no rows match.
- no error representation is included in the successful result model.

Classification:
- Confirmed upstream constraint

## 16. Deferred Contract Decisions
Explicitly deferred to downstream artifacts:
- exact external serialized field formats,
- final request/response schema structure,
- transport-level success and technical-failure expression,
- exact contract treatment of control-field exposure,
- validation-message conventions.

Classification:
- Approved modernization direction plus remaining uncertainty

## 17. Remaining Uncertainties
- Date representation compatibility uncertainty between PROCDB2 declaration and host conversion path evidence.
- Null-handling behavior for source-nullable fields without indicator-variable evidence.
- Nonnumeric value behavior where legacy mapping crosses character and numeric-display representations.
- Tie ordering behavior for rows sharing the same date/time (no proven third tie-breaker).
- Final downstream external representation of derived identifier serialization details.

Classification:
- Remaining uncertainty

## 18. Upstream Alignment

| Major model decision | Program Analysis | Dependency Map | Business Rules | Mapping Matrix | Intended System | Architecture | Research | Classification |
|---|---|---|---|---|---|---|---|---|
| Separate raw controls from normalized effective controls | Confirms legacy control handling flow | Supports flow boundary understanding | Confirms limit/sentinel behavior | Confirms host/intermediate normalization paths | Requires preserved behavior through modern capability | Assigns normalization ownership to application layer | Supports preservation-first modeling choice | Confirmed upstream constraint plus data-model design decision |
| Keep pre-pagination totalCount and post-pagination returnedCount as distinct model attributes | Confirms count and retrieval are separate paths | Confirms read dependency sequence | Confirms total versus returned semantics | Confirms count and fetch mappings | Confirms final response flow constraints | Confirms orchestration boundaries | Confirms pagination preservation decision | Confirmed upstream constraint |
| Model TransactionIdentifier as derived and not proven unique | Confirms construction path | Confirms no direct detail dependency requirement | Confirms deterministic shape and non-proven uniqueness | Confirms component mapping and uncertainty | Confirms capability boundary with deferred contract detail | Confirms scope boundary with INQTRAND separation | Confirms domain representation principle | Confirmed upstream constraint plus remaining uncertainty |
| Preserve fixed-width and leading-zero semantics | Confirms legacy field handling context | Confirms dependency on data access semantics | Confirms output mirrors selected row values | Confirms type and fixed-width concerns | Confirms behavior preservation goal | Supports domain and mapping layer roles | Confirms representation-preservation rationale | Confirmed upstream constraint |
| Keep persistence model read-only and adapter-friendly | Confirms read-only legacy behavior | Confirms DB2 dependency and separable operational concerns | Confirms read-only and failure semantics | Confirms persistence exposure boundaries | Confirms intended persistence responsibilities | Confirms repository abstraction boundary | Confirms repository abstraction decision | Approved modernization direction plus data-model design decision |
| Defer unresolved external contract and nullability behavior | Flags unresolved evidence gaps | Identifies missing runtime definitions | Separates business behavior from contract details | Marks provisional mapping and uncertainty | Defers contract specifics downstream | Keeps architecture-level boundaries | Explicitly records deferred decisions | Remaining uncertainty |

## Optional Illustrative Implementation Examples (Non-authoritative)
The following Java-like records are illustrative only and do not replace the conceptual data model above.

```java
public record InquiryControlsRaw(
		String sortCode,
		String accountNumber,
		String fromDateRaw,
		String toDateRaw,
		Integer limitRequested,
		Integer offsetRequested
) {}

public record InquiryControlsEffective(
		String sortCode,
		String accountNumber,
		String lowerDateBoundNormalized,
		String upperDateBoundNormalized,
		int limitEffective,
		int offsetEffective
) {}

public record TransactionRecordModel(
		String transactionIdentifier,
		String sortCode,
		String accountNumber,
		String transactionDate,
		String transactionTime,
		String transactionReference,
		String transactionType,
		String transactionDescription,
		java.math.BigDecimal transactionAmount
) {}

public record InquiryResultModel(
		int totalCount,
		int returnedCount,
		java.util.List<TransactionRecordModel> transactions
) {}
```
