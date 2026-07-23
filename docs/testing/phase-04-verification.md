# Phase 04 verification

## Verified local tooling

The final clean-state validation ran on 2026-07-23 with Android Studio JBR 21.0.10
at `C:\Program Files\Android\Android Studio\jbr` and Android SDK
`C:\Android\Sdk`. The build session set `JAVA_HOME`, `ANDROID_HOME`, and
`ANDROID_SDK_ROOT`; ignored `android-app/local.properties` supplies the SDK path to
Gradle. Required SDK packages available were `platform-tools`, `platforms;android-35`,
`build-tools;35.0.0`, and Android command-line tools.

Run from `android-app`:

```powershell
.\gradlew.bat --version
.\gradlew.bat clean
.\gradlew.bat spotlessCheck
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

| Command | Result |
| --- | --- |
| `gradlew.bat --version` | PASS — Gradle 8.10.2 / JBR 21.0.10 |
| `gradlew.bat clean --no-daemon` | PASS |
| `gradlew.bat spotlessCheck --no-daemon` | PASS |
| `gradlew.bat test --stacktrace --no-daemon` | PASS |
| `gradlew.bat lintDebug --stacktrace --no-daemon` | PASS |
| `gradlew.bat assembleDebug --stacktrace --no-daemon` | PASS |
| `gradlew.bat connectedDebugAndroidTest --stacktrace --no-daemon` | NOT RUN — POCO X3 Pro cancelled installation of `app-debug-androidTest.apk` with `INSTALL_FAILED_USER_RESTRICTED`; production APK smoke testing completed |

Tests are deterministic and do not call external services. The unit suite covers money
display, currency mismatch safety, Proto defaults, and Proto serialization. A Compose
connected smoke test now checks all five destinations and the quick-action sheet using
stable semantic tags; the phone cancelled its test-APK installation before execution.

## APK validation

The generated APK is at
`android-app/app/build/outputs/apk/debug/app-debug.apk`; the byte-identical handoff
copy is `artifacts/phase-04/SalahFinanceManager-phase04-debug.apk`.

- Size: 18,973,403 bytes
- SHA-256: `5E652FDFA7C354B369752458552A659F615B9189841F410EECDE2D8F35A23887`
- Application ID: `com.salahabusaif.financemanager`
- Version: `0.4.0` (code `1`)
- Minimum / target SDK: `26` / `35`
- Signature: `apksigner verify --verbose --print-certs` passed using v2 with an
  `Android Debug` certificate.
- Permissions: `aapt2 dump permissions` found no `INTERNET` or broad-storage
  permission. WorkManager-related `WAKE_LOCK`, `ACCESS_NETWORK_STATE`,
  `RECEIVE_BOOT_COMPLETED`, and `FOREGROUND_SERVICE` permissions are present.

## Physical-device runtime validation

The production debug APK was installed and launched on a POCO X3 Pro (model
M2102J20SG, product `vayu_global`, codename `vayu`) using its authorized ADB
connection. Arabic default, RTL layout, all five destinations, quick actions, English
LTR, System/Light/Dark themes, and both hide/show amount states were manually checked.
Arabic and English locale selections persisted after force-stop and relaunch; Android
13 reported the configured per-app locale as `[ar]` or `[en]` respectively. Focused
logcat inspection found no application fatal exception or Compose, Hilt, Room,
DataStore, locale, or WorkManager initialization error.

`connectedDebugAndroidTest` built the test APK but did not execute because MIUI
cancelled its installation (`INSTALL_FAILED_USER_RESTRICTED`). This is a device policy
limitation, not a test failure. Re-run the command after accepting the phone's install
prompt to execute `FinanceAppSmokeTest`.

The same MIUI policy also cancelled a final non-interactive reinstall of the rebuilt
production APK after artifact validation. The prior production smoke test completed on
the same UI build before the test-only semantic-tag addition; accepting the phone's
install prompt is required to replace the currently installed package with the exact
artifact again.

Sanitized physical-device captures are in `docs/screenshots/phase-04/`, including
Arabic and English Home/Settings, all Arabic empty states, dark Home, quick actions,
and both bottom-navigation directions.
