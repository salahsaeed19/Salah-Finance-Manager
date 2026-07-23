# ADR-001 — Use a Double-Entry Ledger

**Status:** Accepted  
**Date:** 23 July 2026  

## Context

The application must manage real accounts, entrusted money, receivables, payables, income, expenses, transfers, currency exchange, commissions, corrections, and reconciliation.

A simple signed transaction table cannot reliably represent all of these cases.

## Decision

Use a double-entry ledger internally.

Each posted transaction contains balanced debit and credit postings. The UI exposes safe business actions rather than accounting terminology.

## Consequences

### Positive

- Balances are rebuildable.
- Internal transfers do not create false income.
- Entrusted money is separated from personal money.
- Reversals are traceable.
- Reconciliation is reliable.
- Invariant testing is possible.

### Negative

- More entities and domain logic are required.
- Developers must understand posting rules.
- Every transaction template requires tests.

## Guardrails

- No unbalanced transaction may be posted.
- No direct balance editing.
- Posted financial changes use reversal and replacement.
- The UI never asks the user to enter debit or credit.
