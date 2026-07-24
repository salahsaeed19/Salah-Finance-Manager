# Product Requirements and Project Foundation

**Project Working Name:** Salah Finance Manager  
**Document:** Phase 01 — Product Requirements  
**Status:** Approved Baseline  
**Date:** 23 July 2026  
**Product Owner:** Salah Abu Saif  
**Target Platform:** Android  

> Product position update (Phase 05): Salah Finance Manager is a private personal finance application customized for Salah Abu Saif and his personal accounts. It is not organization onboarding software or a public multi-tenant product.
**Planned Technology Stack:** Kotlin, Jetpack Compose, Room, Android Studio  
**Supported Languages:** Arabic and English  

---

## 1. Purpose

The purpose of this project is to build a secure and professional Android application that manages both personal finances and money held or managed on behalf of other people.

The application must reduce the current weekly reconciliation process from several hours of manual work to a short review process focused only on new, unknown, or conflicting transactions.

The system will manage:

- Personal income and expenses.
- Monthly and annual budgeting.
- Savings and financial goals.
- Money belonging to other people and held in the user's accounts.
- Money owed to the user.
- Money owed by the user.
- Bank of Palestine accounts in ILS and USD.
- PalPay wallet transactions.
- Jawwal Pay wallet transactions.
- Imported bank statements.
- Person-specific statements.
- Wallet commissions.
- Monthly and annual financial reports.
- WhatsApp-ready account statement messages.

---

## 2. Product Vision

The application is not a simple expense tracker.

It is a combination of:

> Personal Finance Manager + Family Ledger + Debt Manager + Bank Statement Review System

At any time, the application should clearly answer the following questions:

- How much money do I personally own in ILS?
- How much money do I personally own in USD?
- How much money exists in each bank account or wallet?
- How much of the available money belongs to other people?
- Who has money deposited with me?
- Who owes me money?
- Whom do I owe money to?
- How much have I spent this month?
- How much remains from my salary?
- How much have I saved this month and this year?
- Which transactions still need review?
- Does the application balance match the real bank and wallet balances?
- How much did each person deposit, withdraw, borrow, or repay?

---

## 3. Core Product Principles

The application must follow these principles:

1. **Financial correctness before visual design.**
2. **Personal money must never be mixed with money belonging to others.**
3. **ILS and USD must remain separate unless an exchange transaction is explicitly recorded.**
4. **Imported bank data must remain unchanged.**
5. **Balances must be calculated from approved transactions, not manually overwritten.**
6. **Every important edit must be traceable.**
7. **The user remains the final authority for transaction classification.**
8. **Every feature must be delivered as a complete vertical slice: data, logic, UI, validation, and tests.**
9. **The application must work offline in its first release.**
10. **Sensitive personal and financial data must never be committed to Git.**

---

## 4. Visual Direction

The provided finance application design has been selected as the official visual reference for the project.

The design will be used as inspiration only and will not be copied directly.

### 4.1 Visual Style

The application should use:

- A clean and modern financial interface.
- A purple gradient as the primary brand direction.
- Light backgrounds with white content cards.
- Large and readable financial values.
- Clear spacing and visual hierarchy.
- Simple charts and progress indicators.
- A prominent floating action button.
- Bottom navigation.
- Rounded cards and modern icons.
- Consistent empty, loading, success, warning, and error states.
- Dark mode support in a later stage.

### 4.2 Functional Colors

- **Purple:** Primary brand, navigation, and actions.
- **Green:** Income, deposits, positive balances, and successful completion.
- **Red:** Withdrawals, overdue debts, negative balances, and critical warnings.
- **Orange:** Transactions that require review.
- **Blue:** Informational items, neutral account balances, and reports.
- **Gray:** Secondary information and disabled states.

Color must not be the only way to communicate meaning. Text labels and icons must also be used.

### 4.3 Arabic and English Layouts

The application must support:

- Arabic as a complete RTL interface.
- English as a complete LTR interface.
- Correct alignment of values, icons, lists, charts, and navigation.
- Proper number and date formatting for each language.
- Switching languages from the Settings screen.
- No hardcoded visible text inside Compose screens.

---

## 5. Target Users

### 5.1 First Release

The first release is designed for a single owner:

- The application owner manages all accounts, people, transactions, budgets, and reports.
- All financial data is stored locally on the device unless the user creates an encrypted backup.

### 5.2 Future Roles

The following are outside the first release but may be added later:

- Read-only family members.
- A person who can view only their own account statement.
- Multi-device synchronization.
- Shared household management.
- Administrator and reviewer roles.

---

## 6. Main Functional Areas

### 6.1 Dashboard

The dashboard must provide a clear summary of the user's full financial position.

It should show:

- Personal ILS balance.
- Personal USD balance.
- Bank of Palestine ILS balance.
- Bank of Palestine USD balance.
- PalPay balance.
- Jawwal Pay balance.
- Total money belonging to other people.
- Total receivables.
- Total payables.
- Current month income.
- Current month spending.
- Remaining salary.
- Actual savings.
- Savings goal progress.
- Transactions requiring review.
- Recent transactions.
- Reconciliation warnings.
- Monthly spending comparison.

The dashboard must allow switching between:

- Current month.
- Previous month.
- Custom period.
- Year-to-date view.

---

## 7. People and Personal Accounts

The application must provide a complete people management module.

### 7.1 People List

The people list should display:

- Profile image.
- Full name.
- Alternative names.
- Amount they have with the user.
- Amount they owe the user.
- Related currencies.
- Last transaction date.
- Account status.
- Unreviewed transaction count.
- Overdue debt indicator.

The user must be able to:

- Search by official name.
- Search by nickname.
- Search by alternative name.
- Filter by currency.
- Filter by positive balance.
- Filter by debt.
- Filter by inactive account.
- Sort by highest balance, highest debt, or recent activity.

### 7.2 Person Profile

Each person must have a dedicated page containing:

- Profile photo.
- Full name.
- Nickname.
- Alternative names.
- Phone number.
- Notes.
- Creation date.
- ILS summary.
- USD summary.
- Amount the person has with the user.
- Amount the person owes the user.
- Optional net position.
- Full transaction history.
- Wallet commissions.
- Beneficiaries.
- Loans and repayments.
- Account statements.
- Attachments and notes.

ILS and USD balances must always be displayed separately.

### 7.3 Person Actions

The user must be able to:

- Add a deposit.
- Record a withdrawal.
- Record a loan.
- Record a repayment.
- Record a wallet transfer.
- Record a bank transfer.
- Add an opening balance.
- Add a correction transaction.
- Edit transaction classification.
- Generate an account statement.
- Copy a WhatsApp-ready statement.
- Export a statement later to PDF or Excel.

---

## 8. Person Photos

The application must support optional profile photos for people.

The user should be able to:

- Take a new photo.
- Select a photo from the device.
- Crop the photo.
- Replace the photo.
- Remove the photo.
- Use an automatic initials-based avatar when no photo exists.

Images should be resized and compressed to avoid unnecessary storage use.

Photos are for identification only and must never affect financial calculations.

---

## 9. Financial Accounts and Wallets

The first release must support:

- Bank of Palestine — ILS account.
- Bank of Palestine — USD account.
- PalPay wallet.
- Jawwal Pay wallet.
- Cash account.
- Custom account types for future use.

Each account must include:

- Name.
- Type.
- Currency.
- Opening balance.
- Current calculated balance.
- Last reconciliation date.
- Active or archived state.
- Optional notes.
- Optional color or icon.

---

## 10. Transaction Types

The application must support the following transaction types:

- Deposit.
- Withdrawal.
- Personal expense.
- Personal income.
- Salary.
- Loan given.
- Loan received.
- Loan repayment.
- Debt repayment.
- Bank transfer.
- Wallet transfer.
- Internal transfer.
- Currency exchange.
- Savings contribution.
- Savings withdrawal.
- Commission.
- Refund.
- Reversal.
- Correction.
- Opening balance.
- Imported bank transaction.
- Imported wallet transaction.

Each transaction must include:

- Transaction date.
- Posting date when available.
- Source account.
- Destination account when applicable.
- Person.
- Beneficiary.
- Original description.
- User-edited description.
- Original amount.
- Commission.
- Calculated amount.
- Currency.
- Category.
- Transaction type.
- Review status.
- Import source.
- Reference number.
- Notes.
- Creation date.
- Modification date.
- Audit information.

---

## 11. Financial Rules

### 11.1 Currency Separation

ILS and USD are separate currencies.

The application must not combine them automatically.

They may only be combined for reporting when:

- The user provides an exchange rate.
- The exchange rate date is stored.
- The original currency amounts remain available.

### 11.2 Personal Money and Other People's Money

The balance shown by the bank is not equal to the user's personal balance.

The application must separately calculate:

- Actual balance in each financial account.
- Money belonging to the user.
- Money belonging to other people.
- Receivables.
- Payables.
- Savings.
- Personal net worth.

### 11.3 “Has Money With Me” and “Owes Me”

The same person may simultaneously:

- Have money stored with the user.
- Owe money to the user.

These must remain separate values.

The system may display an optional net amount, but it must not replace the original values.

### 11.4 Calculated Balances

Balances must be calculated from transactions.

The user must not directly edit a balance.

Any adjustment must be recorded as:

- Opening balance.
- Correction transaction.
- Reversal.
- Repayment.
- Transfer.

### 11.5 Imported Raw Data

Imported bank transactions must remain unchanged.

The system must maintain two layers:

1. **Raw imported transaction**
2. **User-reviewed accounting transaction**

The original data must always remain available for review.

### 11.6 Wallet Commission

Wallet commission must be stored separately from the transferred amount.

Current rules:

| Transfer Amount | Commission |
|---|---:|
| Up to 50 ILS | 0.5 ILS |
| 51–100 ILS | 1 ILS |
| 101–250 ILS | 1.5 ILS |
| 251–500 ILS | 2 ILS |
| 501–1,000 ILS | 3 ILS |

Amounts above 1,000 ILS must require:

- A configurable rule, or
- Manual review.

Example:

- Original transfer: 200 ILS.
- Commission: 1.5 ILS.
- Amount charged to the person's account: 201.5 ILS.

### 11.7 Internal Transfers

A transfer between the user's own accounts:

- Is not income.
- Is not an expense.
- Reduces one account.
- Increases another account.
- Must be stored as one linked transfer operation.

### 11.8 Currency Exchange

A USD-to-ILS exchange must create linked movements:

- Outgoing amount from USD account.
- Incoming amount to ILS account.
- Exchange rate.
- Exchange fee or difference.
- Transaction date.

It must not be counted as new income.

### 11.9 Reversals and Refunds

A reversed or refunded transaction must not be deleted.

It must be linked to the original transaction and clearly marked.

### 11.10 Duplicate Prevention

Imported transactions must be protected against duplication.

The system should first use:

- Bank reference number.

When no reliable reference exists, it should generate a fingerprint using:

- Account.
- Currency.
- Date.
- Amount.
- Normalized description.

Potential duplicates must be presented for review.

### 11.11 Editing and Deletion

- Transactions must use soft deletion.
- Every edit must be recorded.
- Recalculation must occur after an approved edit.
- Historical data should remain traceable.
- Closed periods should require a correction transaction instead of silent editing.

---

## 12. Bank Statement Import

The application must support importing Bank of Palestine XLSX statements.

### 12.1 Import Workflow

1. User selects an XLSX file.
2. Application identifies the account and currency.
3. Application validates the file structure.
4. Application reads transactions.
5. Application checks for duplicates.
6. Application displays an import preview.
7. User confirms or cancels the import.
8. Raw transactions are saved.
9. New transactions enter the review queue.
10. Account balance is recalculated.

### 12.2 Imported Data

The importer should attempt to read:

- Bank date.
- Effective date.
- Description.
- Debit amount.
- Credit amount.
- Running balance.
- Reference number.
- Account number.
- Currency.
- Statement period.

### 12.3 Import Safety

- Failed imports must not damage existing data.
- The same file may be uploaded again safely.
- Overlapping statements must add only new transactions.
- Suspicious dates must be flagged.
- Unsupported rows must be reported.
- The original file name and import date must be stored.
- Sensitive sample files must not be uploaded to Git.

---

## 13. Transaction Review

After import, the system should suggest:

- Person.
- Beneficiary.
- Transaction type.
- Category.
- Account.
- Whether the transaction is personal or belongs to someone else.
- Whether a wallet commission applies.

The application must never approve uncertain classifications automatically.

Possible review states:

- Unreviewed.
- Suggested.
- Confirmed.
- Needs attention.
- Duplicate.
- Excluded.
- Reversed.

The user should be able to approve several clear transactions quickly while reviewing uncertain transactions individually.

---

## 14. Reconciliation

The application must provide a reconciliation screen for each account.

It should compare:

- Bank or wallet balance.
- Calculated application balance.
- Difference.
- Unreviewed transactions.
- Excluded transactions.
- Pending corrections.
- Last reconciliation date.

A period should not be considered reconciled while an unexplained difference exists.

Example:

- Bank balance: 24,493 ILS.
- Application balance: 24,443 ILS.
- Difference: 50 ILS.
- One unreviewed transaction: 50 ILS.

---

## 15. Personal Budgeting

The application must support monthly personal financial management.

### 15.1 Monthly Income

The user can record:

- Salary.
- Additional income.
- Freelance income.
- Gifts.
- Refunds.
- Other income.

### 15.2 Monthly Budget

The user can define:

- Monthly income target.
- Total spending limit.
- Category limits.
- Savings target.
- Emergency reserve.
- Planned commitments.

### 15.3 Expense Categories

Initial categories may include:

- Food and groceries.
- Home.
- Transportation.
- Mobile and internet.
- Health and medicine.
- Clothing.
- Family support.
- Education.
- Entertainment.
- Subscriptions.
- Work expenses.
- Donations.
- Other.

Categories must be editable.

### 15.4 Monthly Summary

The application should show:

- Total income.
- Total spending.
- Remaining salary.
- Remaining budget.
- Actual savings.
- Savings rate.
- Highest spending category.
- Comparison with previous month.
- Average daily spending.
- Safe daily spending until month-end.

### 15.5 Monthly Closing

The user should be able to close a month.

After closing:

- Silent edits should not be allowed.
- Changes should require a correction transaction.
- Monthly totals should remain stable.
- A closing report should be generated.

---

## 16. Savings Goals

The user must be able to create savings goals.

Each goal should include:

- Name.
- Target amount.
- Currency.
- Start date.
- Target date.
- Current saved amount.
- Progress percentage.
- Remaining amount.
- Status.
- Notes.

Examples:

- Emergency fund.
- Laptop.
- Marriage.
- Medical treatment.
- Business project.
- Annual expenses.

Savings must remain part of the user's assets and must not be treated as an expense.

---

## 17. Reports

The application should eventually provide:

- Person account statement.
- Monthly financial report.
- Annual financial report.
- Debt report.
- Receivables report.
- Payables report.
- Money held for others report.
- Wallet commission report.
- Spending report.
- Income report.
- Savings report.
- Net worth report.
- Account reconciliation report.
- Unreviewed transactions report.

### 17.1 WhatsApp Statements

The user should be able to generate an editable WhatsApp-ready message containing:

- Greeting.
- Statement date.
- Opening balance.
- Deposits.
- Withdrawals.
- Commissions.
- Loans.
- Repayments.
- Closing balance.
- Amount the person has with the user.
- Amount the person owes the user.

The user must be able to edit the text before copying or sharing it.

### 17.2 Future Export Formats

Later phases may add:

- PDF.
- Excel.
- CSV.
- Print-ready statement.

---

## 18. Languages and Localization

### 18.1 Arabic

- Default language.
- Full RTL layout.
- Arabic financial terminology.
- Proper alignment of icons and navigation.
- Regional date formatting.
- Optional Arabic or Latin digits.

### 18.2 English

- Full LTR layout.
- Complete English translation.
- Correct financial terminology.
- Independent UI validation.

### 18.3 Localization Rules

- All visible strings must exist in Android resources.
- No hardcoded UI text.
- Currency names and symbols must be localized.
- Dates must use locale-aware formatting.
- User-entered names must remain unchanged.
- Reports must support the selected language.

---

## 19. Security and Privacy

The first release must include or prepare for:

- PIN lock.
- Biometric authentication.
- Automatic lock timeout.
- Screenshot and recent-app protection.
- Encrypted backups.
- Local-only storage by default.
- No bank credentials.
- No direct bank login.
- No sensitive values in application logs.
- Audit records for edits and deletions.
- Backup verification.
- Restore verification.

The application must never ask for or store the Bank of Palestine password.

---

## 20. Main Screens

The planned application screens are:

1. App lock.
2. Home dashboard.
3. Accounts and wallets.
4. People list.
5. Person profile.
6. Transactions list.
7. Transaction details.
8. Add transaction.
9. Edit transaction.
10. Import statement.
11. Import preview.
12. Review queue.
13. Duplicate review.
14. Reconciliation.
15. Monthly budget.
16. Expense categories.
17. Savings goals.
18. Reports.
19. WhatsApp statement editor.
20. Settings.
21. Language settings.
22. Backup and restore.
23. Audit log.

---

## 21. Bottom Navigation

The proposed bottom navigation is:

- Home.
- Transactions.
- People.
- Plans.
- Settings.

A central primary action button should allow the user to add:

- Transaction.
- Person.
- Loan.
- Repayment.
- Income.
- Expense.
- Savings goal.

---

## 22. Vertical Feature Delivery

Every feature must be implemented as a complete vertical slice.

A feature is not complete until it includes:

1. Data model.
2. Database support.
3. Business rules.
4. Repository.
5. Use case.
6. ViewModel.
7. Compose UI.
8. Input validation.
9. Loading state.
10. Empty state.
11. Error state.
12. Unit tests.
13. Database tests when relevant.
14. UI tests.
15. Documentation.
16. Screenshot of the completed result.

Example:

The “Add Person” feature is not complete when only the database table exists. It is complete only when the user can add, edit, validate, view, search, and test a person from the real application interface.

---

## 23. Quality Requirements

The application must:

- Use precise monetary values.
- Never use floating-point types for financial calculations.
- Prefer minor currency units or a decimal-safe representation.
- Work without internet access.
- Preserve data after process termination.
- Support common Android phone sizes.
- Support Arabic and English completely.
- Prevent duplicate imports.
- Recover safely from failed imports.
- Preserve earlier functionality after new phases.
- Include automated tests for financial rules.
- Include migration tests for database changes.
- Keep personal financial data outside source control.
- Provide deterministic calculations.
- Produce the same balance after backup and restore.
- Handle thousands of transactions without becoming unusable.

---

## 24. Out of Scope for the First Release

The following are not included in the first release:

- Direct Bank of Palestine API integration.
- Storing bank usernames or passwords.
- Automatic WhatsApp sending without user confirmation.
- Real financial transfers from inside the application.
- Multi-user cloud synchronization.
- iOS application.
- Web application.
- Automatic AI approval of transactions.
- Public account access for family members.
- Investment portfolio management.
- Cryptocurrency management.
- Tax reporting.

---

## 25. Documentation and LinkedIn Tracking

The project must be documented continuously.

Recommended structure:

```text
docs/
  phases/
  decisions/
  screenshots/
  testing/
  linkedin/
sample-data/
```

### 25.1 Phase Documentation

Each phase should document:

- Phase name.
- Objective.
- Start date.
- Completion date.
- Features delivered.
- Backend work.
- Frontend work.
- Database changes.
- Technical decisions.
- Problems encountered.
- Solutions applied.
- Tests executed.
- Test results.
- Screenshots.
- Commit hash.
- Lessons learned.
- Known limitations.
- Next phase.

### 25.2 Phase Documentation Template

```markdown
# Phase XX — Phase Name

## Objective

## Scope

## Features Delivered

## Backend Work

## Frontend Work

## Database Changes

## Testing

## Problems and Solutions

## Screenshots

## Commit

## Known Limitations

## Lessons Learned

## Next Step
```

### 25.3 LinkedIn Content

Future LinkedIn documentation may include:

- The real-world problem.
- The manual workflow before the application.
- Product discovery.
- Why Android was selected.
- Financial architecture decisions.
- Separation of personal and entrusted money.
- Bank statement import.
- Duplicate prevention.
- Arabic and English UI.
- Before and after workflow.
- Test coverage.
- Performance results.
- Time saved.

Real names, account numbers, balances, statements, and private financial details must never be published.

---

## 26. Phase 01 Acceptance Criteria

Phase 01 is complete when the following are documented and approved:

- [x] Product purpose.
- [x] Product vision.
- [x] Target user.
- [x] Main modules.
- [x] Account types.
- [x] Transaction types.
- [x] Core financial rules.
- [x] Currency separation.
- [x] Personal and entrusted money separation.
- [x] Debt and deposit model.
- [x] Bank import expectations.
- [x] Wallet commission rules.
- [x] Arabic and English support.
- [x] Person profile photos.
- [x] Visual direction.
- [x] Vertical feature delivery rule.
- [x] Security baseline.
- [x] Reporting expectations.
- [x] Documentation strategy.
- [x] First-release boundaries.
- [x] Quality requirements.

---

## 27. Approved Phase 01 Decisions

The following decisions are approved:

- Native Android application.
- Kotlin.
- Jetpack Compose.
- Room database.
- Offline-first design.
- Arabic and English support.
- Arabic as the default language.
- RTL and LTR support from the beginning.
- Purple-gradient modern finance design.
- Optional person profile photos.
- Raw imported data remains immutable.
- Accounting data is stored separately.
- Financial balances are transaction-driven.
- Personal money is separated from other people's money.
- Every feature is delivered with backend, frontend, and tests.
- Full project documentation is required.
- Real financial data must not be committed to Git.

---

## 28. Next Phase

The next phase is:

# Phase 02 — Technical Architecture and Database Design

It will define:

- Application architecture.
- Module structure.
- Database entities.
- Relationships.
- Money representation.
- Transaction posting rules.
- Ledger model.
- Balance calculation strategy.
- Audit model.
- Import model.
- Duplicate detection strategy.
- Reconciliation model.
- Backup model.
- Testing strategy.
- Sanitized test scenarios based on real workflows.
