# Phase 02 — Acceptance Scenarios

**Status:** Approved Baseline  
**Date:** 23 July 2026  

These scenarios must become automated tests during implementation.

## Financial Core

1. Person deposits 1,000 ILS as entrusted money.
2. Person withdraws 200 ILS from held funds.
3. Wallet transfer of 200 ILS applies 1.5 ILS commission.
4. Wallet transfer becomes receivable when no held balance exists.
5. Mixed held-balance and receivable settlement.
6. User lends 300 ILS.
7. Person repays 100 ILS.
8. Salary of 1,200 ILS.
9. Personal expense of 50 ILS.
10. Internal transfer does not affect income or expense.
11. Exchange 500 USD into 1,657 ILS.
12. Reversal cancels the original financial effect.
13. Replacement transaction creates the corrected effect.
14. ILS and USD remain separate.
15. Every posted transaction balances.

## Import

1. Parse sanitized Bank of Palestine ILS file.
2. Parse sanitized Bank of Palestine USD file.
3. Import identical file twice.
4. Import overlapping statement periods.
5. Handle missing reference number.
6. Handle malformed date.
7. Handle Excel numeric date.
8. Preserve raw text.
9. Roll back failed import.
10. Explain duplicate reason.

## Reconciliation

1. Exact balance.
2. Missing transaction produces difference.
3. Reviewed missing movement clears difference.
4. Closed reconciliation cannot be silently changed.

## Budget

1. Salary and personal spending totals.
2. Loan principal excluded from expenses.
3. Repayment excluded from income.
4. Internal transfer excluded from spending.
5. Savings contribution excluded from expenses.
6. Safe daily spending calculation.

## Backup

1. Valid encrypted backup.
2. Tampered backup rejected.
3. Restore returns identical balances.
4. Migration during restore preserves data.
