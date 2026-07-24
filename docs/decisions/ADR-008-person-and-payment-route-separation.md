# ADR-008: Separate managed people from payment routes

## Decision

A managed person has no permanent provider or financial-account field. Every financial operation explicitly selects one of Salah's financial accounts as its route.

## Consequences

One person can deposit through Bank of Palestine and withdraw through a wallet without duplicating that person or combining their holdings with Salah's own assets. Person balances remain derived from person-specific ledger postings by currency.
