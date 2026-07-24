# Phase 06 — People Accounts and Person Statements

## Scope

Phase 06 adds private person records for people with a financial relationship to Salah. A person is not a provider or a financial account: every movement separately selects one of Salah's existing accounts.

## Ledger model

Each person can resolve two posting-derived ledger accounts per currency: `PERSON_FUNDS_HELD` for money held by Salah and `PERSON_RECEIVABLE` for money owed to Salah. ILS, USD, and JOD remain independent and are never merged.

| Operation | Debit | Credit |
| --- | --- | --- |
| Deposit | Salah financial asset | Person funds held |
| Withdrawal | Person funds held | Salah financial asset |
| Loan | Person receivable | Salah financial asset |
| Repayment | Salah financial asset | Person receivable |
| Transfer for person | Held funds and/or receivable | Salah asset and commission income |

The Phase 06 Room migration is additive from version 3 to 4. It creates `people`, `person_aliases`, `person_ledger_accounts`, and `person_operations`; no existing account, posting, or balance is changed. Migration 4 to 5 adds the non-null `fundsHeldChargedMinor` operation field with a zero default, preserving an already-installed Phase 06 preview database while recording the held-funds portion of a mixed transfer.

## Privacy

People photos are represented by a private storage path, not by image blobs in Room. Tracked sources and test data contain no real relatives, financial balances, phone numbers, or account references.

## Implemented behavior

The phase introduces persisted people, alias-aware search, currency-separated summaries, timeline records, and a bilingual People UI. A person never has a permanent bank or wallet provider: each operation selects the actual Salah-owned financial account used for that movement.

Deposits and withdrawals affect `PERSON_FUNDS_HELD`; loans and repayments affect `PERSON_RECEIVABLE`. Transfers record a beneficiary and an optional commission. When held funds are insufficient, the user explicitly chooses cancellation, a held-funds-plus-receivable mixed settlement, or a full receivable. No operation is classified as Salah's personal income merely because it changes a person's balance.

Statements are generated per currency and show opening balances, all posted operations, beneficiaries, commissions, dates, and closing balances. They are available in the selected application language, can be copied, and use the Android share sheet without requiring a particular messaging application.

## Verification evidence

The final code path was clean-built on 2026-07-24 with Gradle 8.10.2 and Android SDK 35. Device validation used a POCO X3 Pro (Android 13) over authorized ADB and only sanitized test records. It verified alias search, ILS deposit, withdrawal, loan, partial repayment, commission transfer, mixed settlement, Arabic statement generation, copy, and Android share-sheet invocation. A physical review found and corrected a selector localization bug where the USD and JOD controls repeated the ILS label.

## Dashboard refinement

The Phase 06 closing refinement makes `MoneyFormatter` the single compact presentation path for dashboard, accounts, people, transactions, and statements. It keeps `Long` minor units internally, removes only trailing all-zero fractions, and preserves real fractional values such as a 1.5 ILS commission. Currency symbols are stable and localized: `₪`, `$`, and `د.أ` in Arabic; `₪`, `$`, and `JOD` in English.

The personal-balance hero now owns real Compose selection state for ILS, USD, and JOD and reads each value from its separate ledger-derived total. Zero currency summary rows are suppressed. When every currency total is zero, the dashboard uses one localized empty message rather than multiple zero totals.

## Closing device validation

The final APK was installed through `adb install -r` after the user approved the MIUI update prompt. It is `com.salahabusaif.financemanager` version `0.6.0` / code `5`, with min SDK 26, target SDK 35, Android Debug v2 signature, and SHA-256 `607298C022C6D1B2430F5736A9CF1BD067A0B0011B34DD8A80A50D357BF8E6B3`. Arabic RTL and English LTR were verified, as were the ILS/USD/JOD selectors, compact whole-value display, zero-summary suppression, app restart persistence, and focused Logcat. No application fatal exception, Room integrity error, or Compose crash was observed. Captured sanitized local screenshots are ignored by Git under `docs/screenshots/phase-06/`.
