# Phase 06 verification

## Build checks completed

| Command | Result |
| --- | --- |
| `:core:data:compileDebugKotlin --no-daemon` | PASS |
| `:feature:people:compileDebugKotlin --no-daemon` | PASS |
| `:app:compileDebugKotlin --no-daemon` | PASS |
| `clean --no-daemon` | PASS |
| `spotlessCheck --no-daemon` | PASS |
| `test --stacktrace --no-daemon` | PASS |
| `lintDebug --stacktrace --no-daemon` | PASS |
| `assembleDebug --stacktrace --no-daemon` | PASS |

## APK validation

The Phase 06 debug APK is `com.salahabusaif.financemanager`, version `0.6.0` / code `5`, min SDK 26 and target SDK 35. The verified source APK and ignored local artifact `artifacts/phase-06/SalahFinanceManager-phase06-debug.apk` are byte-identical: 20,130,665 bytes, SHA-256 `607298C022C6D1B2430F5736A9CF1BD067A0B0011B34DD8A80A50D357BF8E6B3`. `apksigner verify --verbose` confirmed the Android Debug v2 signature. `aapt2 dump badging` showed no Internet or broad-storage permission; declared non-sensitive platform permissions are `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `RECEIVE_BOOT_COMPLETED`, and `FOREGROUND_SERVICE`.

## Device checks completed

On the authorized POCO X3 Pro (Android 13), using sanitized records only:

- Arabic RTL home, People empty state, person editor, account grouping, and person profile rendered without raw enum labels.
- Alias search returned the expected person.
- ILS deposit, withdrawal, loan, partial repayment, beneficiary transfer with commission, and explicit held-funds-plus-receivable mixed settlement posted and persisted.
- The dashboard reflected the ledger-derived asset changes after force-stop and relaunch.
- The Arabic statement contained operation dates, beneficiary and fee details; copy and Android generic share-sheet actions launched.
- A device-only localization defect in the currency controls was fixed: USD and JOD now display their own Arabic labels instead of repeating ILS.

## Closing dashboard correction and device validation

| Check | Result |
| --- | --- |
| `:feature:dashboard:compileDebugKotlin --no-daemon` | PASS |
| `:feature:dashboard:test --no-daemon` | PASS |
| compact whole-value formatting | PASS — `900`, not `900.00` |
| fractional commission formatting | PASS — `1.5` is retained |
| ILS/USD/JOD selector | PASS — independent ledger-derived totals and compact approved symbols |
| zero currency summary rows | PASS — zero USD/JOD rows hidden when ILS is the only active total |
| Arabic RTL / English LTR | PASS |
| force-stop persistence | PASS |
| final focused application Logcat | PASS — no application fatal exception, Room integrity error, or Compose runtime crash |

The final APK was installed through `adb install -r` after user approval of the MIUI update dialog. It remains version `0.6.0` / code `5`; its checksum is stored in the ignored local Phase 06 artifact folder. Sanitized local screenshots and XML hierarchy captures are ignored and not tracked.
