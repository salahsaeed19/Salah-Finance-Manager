# Phase 06 progress note

Phase 06 adds private people accounts to Salah Finance Manager without confusing a person with a payment provider. Each person receives currency-separated, ledger-derived balances for money held and money owed; each financial movement chooses the real Salah-owned account used for the route.

The implementation includes deposit, withdrawal, loan, repayment, beneficiary transfer with commission and explicit mixed settlement, plus bilingual statement preview, copy, and Android sharing. It closes with a compact money formatter and live ILS/USD/JOD dashboard selector. Automated build, lint, and unit-test checks pass, along with Arabic RTL and English LTR validation on the physical device.

Phase 06 establishes a privacy-first people ledger for Salah Finance Manager. It separates managed people from the accounts where money physically moves, keeps ILS, USD, and JOD independent, and records deposits, withdrawals, loans, repayments, and person transfers with balanced postings.
