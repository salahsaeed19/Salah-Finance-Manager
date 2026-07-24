# Database ERD — Phase 05

```text
owner_profile (one local owner)
    └── independent private profile used to prefill personal account setup

financial_accounts ──1:1── ledger_accounts
ledger_accounts ──< ledger_postings >── ledger_transactions ──< transaction_groups
ledger_transactions ──< audit_events (by entity reference)
```

- Every financial account maps to one asset ledger account and one ISO currency.
- Provider grouping is configuration and UI metadata; it never merges ledger accounts or currencies.
- Bank of Palestine, cash, and wallet configurations may coexist. ILS, USD, and JOD always retain independent ledger balances.
- `owner_profile` is local application data and is not a financial account, person/debt account, or ledger balance.
- Personal payable ledger roles are reserved for future debt workflows. A person’s balance with Salah and the account that physically moved money remain separate concepts.
