# Repository guidance

- Read the applicable phase documents and ADRs in `docs/` before implementing work.
- Android code lives in `android-app`; documentation lives in `docs`.
- Never add real financial data, statements, account numbers, exports, backups, or screenshots to Git.
- Deliver every feature as data + domain + UI + validation + tests + documentation.
- Complete Arabic and English together, and verify both RTL and LTR layouts.
- Represent money as `Long` minor units; never use `Float` or `Double`.
- Financial state is transaction-driven; do not directly edit balances or use destructive migrations.
- Do not commit, push, or create a branch unless explicitly requested.

## Android commands (PowerShell)

From `D:\\Works\\Salah-Finance-Manager\\android-app` run:

```powershell
.\\gradlew.bat spotlessCheck
.\\gradlew.bat test
.\\gradlew.bat lintDebug
.\\gradlew.bat assembleDebug
```
