# Phase 05 verification

## Automated coverage

- `PostingEngineTest` validates balanced income, rejection of invalid transfers, reversal direction, ILS/USD and JOD/ILS exchange plans, and date/range-bound commission rules.
- Room schemas version 2 and version 3 are exported. Version 3 adds the private local owner profile.
- Migrations `1 -> 2` and additive `2 -> 3` are registered when the production Room database is constructed.

## Manual smoke checklist

Run on an authorized Android 8.0+ device:

1. Start with Arabic, verify RTL account and transaction screens.
2. Create a cash account with an opening balance and verify its dashboard balance.
3. Record one income and one expense, verify the transaction list and changed balance.
4. Reverse an entry and verify original history is retained and its reversal is visible.
5. Switch to English, verify LTR labels and layout.
6. Force-stop/relaunch and verify the app locale and privacy/theme preferences persist.

## Executed results — 2026-07-24

| Check | Result |
| --- | --- |
| `gradlew.bat clean --no-daemon` | PASS |
| `gradlew.bat spotlessCheck --no-daemon` | PASS |
| `gradlew.bat test --stacktrace --no-daemon` | PASS |
| `gradlew.bat lintDebug --stacktrace --no-daemon` | PASS — final personal-account build |
| `gradlew.bat assembleDebug --stacktrace --no-daemon` | PASS — final 0.5.1 APK |
| APK signature verification | PASS — Android Debug certificate, v2 signature |
| APK inspection | PASS — `com.salahabusaif.financemanager`, version `0.5.1` (3), min SDK 26, target SDK 35; no Internet or broad-storage permission |
| Physical installation | PASS — in-place ADB update on POCO X3 Pro (Android 13), no uninstall or data clear |
| Launch/logcat | PASS — launch, Room migration, Arabic RTL, English LTR, and force-stop/relaunch verified; no app fatal exception found in focused logs |
| `connectedDebugAndroidTest` | NOT COMPLETED — Gradle stayed active beyond the available command window on the physical device; this does not change the successful APK installation and launch result. |

The final debug APK is at `android-app/app/build/outputs/apk/debug/app-debug.apk` and the byte-identical local artifact is at `artifacts/phase-05/SalahFinanceManager-phase05-debug.apk`. Its SHA-256 is `92012FE966FB369AF0ACD97C6F27DAC4711D13F4955BE83C087444BC8A94A7DF` (19,211,858 bytes).

## Device-validation notes

The private owner-profile bootstrap initially exposed an Arabic UTF-8 decoding defect on device. It was corrected by reading the ignored local properties asset with UTF-8 and the repaired 0.5.1 build was installed in place. Device-only captures remain in ignored local reports because the account screen intentionally contains private owner data; no private screenshot was added to version control.
