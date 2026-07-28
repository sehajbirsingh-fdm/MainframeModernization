# Data Model: INQTRAN Transaction Inquiry Modernization (Temporary Placeholder)

This placeholder data model exists only to support SpecKit workflow progression.
It does not define final runtime request models, response models, or persistence schema.

## Entity: LegacyEvidenceSource (provisional)

Purpose: Track legacy artifacts used to derive approved behavior.

Fields:
- artifactName: string (example values: INQTRANL.cbl, INQTRAND.cbl)
- evidenceRole: enum (primary, potential-related)
- relationshipStatus: enum (confirmed, unconfirmed)
- analysisStatus: enum (not-started, in-progress, complete)
- notes: string

Validation rules:
- artifactName is required.
- evidenceRole is required.
- relationshipStatus is required.
- relationshipStatus for INQTRAND.cbl remains unconfirmed until legacy analysis completes.

## Entity: TransactionInquiryCapability (provisional)

Purpose: Represent the capability under modernization at placeholder level.

Fields:
- capabilityName: string (INQTRAN Transaction Inquiry)
- sourceProgram: string (INQTRANL.cbl)
- relatedProgram: string (INQTRAND.cbl, unconfirmed)
- behaviorDefinitionStatus: enum (pending-legacy-analysis, approved)
- approvedSpecReference: string

Validation rules:
- behaviorDefinitionStatus must remain pending-legacy-analysis for this placeholder phase.
- approvedSpecReference remains empty until approved replacement specification is available.

## Relationships

- TransactionInquiryCapability is derived from one or more LegacyEvidenceSource records.
- No operational data-field relationships are defined in this placeholder.

## State Notes

- Current state: provisional workflow placeholder.
- Exit condition: approved starter specification and validated legacy analysis.
