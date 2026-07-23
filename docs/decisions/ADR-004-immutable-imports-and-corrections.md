# ADR-004 — Immutable Imports and Auditable Corrections

**Status:** Accepted  
**Date:** 23 July 2026  

## Context

Bank statements are source evidence. Editing imported values destroys the ability to compare the system with the original statement.

Financial history must remain explainable.

## Decision

- Imported raw rows are immutable.
- User classification is stored separately.
- Posted transactions are not silently edited.
- Financial corrections create a reversal and a replacement.
- Reconciled and closed-period records require explicit correction workflows.

## Consequences

- Full traceability.
- Safer reconciliation.
- More records after corrections.
- UI must present history clearly without confusing the user.
