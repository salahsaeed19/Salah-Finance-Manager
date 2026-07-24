# Phase 05 — Financial accounts and double-entry ledger

## Scope

Phase 05 introduces local financial accounts, immutable balanced postings, opening balances, personal income and expenses, transaction reversal, audit events, and real account-derived dashboard balances. Salah Finance Manager is a private personal application for Salah Abu Saif, not organization onboarding software. All monetary values are stored as `Long` minor units.

## Implementation

- Room database version 3 adds a local owner-profile record alongside the Phase 05 ledger tables. Migration `2 -> 3` is additive and non-destructive.
- System ledger accounts are seeded idempotently for ILS, USD, and JOD: opening equity, income, expense, transfer clearing, FX clearing, and the reserved personal-payable role foundation.
- Creating a financial account with a positive opening balance posts a balanced opening-balance transaction; it does not mutate a balance field.
- Personal income and expenses post balanced entries. Reversal creates a new opposite transaction and an audit event rather than editing history.
- Currency exchange is represented by a transaction group containing one balanced transaction for each currency.
- Commission rule selection is a pure, date- and range-aware foundation. No commission is guessed or automatically charged in this phase.
- Arabic and English resource entries are supplied together. Compose uses the Android app locale and layout direction established in Phase 04.
- “My Accounts” groups Bank of Palestine ILS/USD/JOD subaccounts, Jawwal Pay, PalPay, and optional cash currencies without merging their ledgers. Every configured row remains an independent asset account and can coexist with the others.
- A local ignored profile file can bootstrap the editable Room owner profile into a private workstation APK. Its tracked example contains no personal data; values are never committed to source, tests, or documentation.
- Accounts use explicit localized display mappings for providers, roles, and currencies; enum names never render in the account UI.

## Deliberate limits

- Person/debt, budget, import, reconciliation, statement and backup workflows remain later phases.
- The first Phase 05 UI exposes account configuration, account balances, income, expenses, transaction history, reversal, transfer, and exchange entry points. Person/debt workflows remain later phase work: a person is whom money belongs to or owes money, while a financial account is where money physically moved.
- Account editing/archive persistence is implemented at the repository level; the initial account list keeps the UI minimal.

## Verification record

The final command results and physical-device result are recorded in `docs/testing/phase-05-verification.md` after the Phase 05 verification pass.
