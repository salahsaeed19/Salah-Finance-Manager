# ADR-003 — Offline-First Local Source of Truth

**Status:** Accepted  
**Date:** 23 July 2026  

## Context

The application contains private financial data and must remain useful without internet access.

## Decision

Room is the canonical source of business data.

Proto DataStore stores only preferences and small application state.

No network service is required in the first release.

## Consequences

- All critical reads work offline.
- UI observes Room-backed flows.
- Backups are explicit and encrypted.
- Future cloud sync must preserve ledger invariants.
