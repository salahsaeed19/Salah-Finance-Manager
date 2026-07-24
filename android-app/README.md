# Android application

`SalahFinanceManager` is a Kotlin, Jetpack Compose, Material 3 multi-module app.
It is offline-first: Room will own business data and Proto DataStore owns small app
preferences. No network client or Internet permission is included.

Run the standard checks from this directory:

```powershell
.\\gradlew.bat spotlessCheck
.\\gradlew.bat test
.\\gradlew.bat lintDebug
.\\gradlew.bat assembleDebug
```

The local environment must provide a JDK 17+ and Android SDK Platform 35 with Build
Tools 35.0.0 (or update the centralized build configuration deliberately).

For the verified Windows setup, use Android Studio JBR at
`C:\Program Files\Android\Android Studio\jbr` and Android SDK `C:\Android\Sdk`:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
```

`local.properties` is intentionally ignored and must contain the local SDK location.
The verified Phase 04 APK is output at
`app/build/outputs/apk/debug/app-debug.apk`; the local handoff copy is at
`../artifacts/phase-04/SalahFinanceManager-phase04-debug.apk` with its checksum in
`../artifacts/phase-04/SHA256.txt`.

The final Phase 04 production APK was launched on a POCO X3 Pro (Android 13), where
Arabic RTL/English LTR locale changes and persisted theme/privacy settings were checked.
The connected test APK installation was cancelled by the phone (`INSTALL_FAILED_USER_RESTRICTED`);
accept the device install prompt before rerunning `connectedDebugAndroidTest`.

Phase 05 adds a private “My Accounts” configuration surface with independent ILS, USD,
and JOD ledger balances for the owner’s bank, wallets, and cash. The optional local owner
bootstrap is read only from `../private-config/salah-profile.properties`, which is ignored;
copy the safe example file and supply private values locally. The Phase 05 artifact is
`../artifacts/phase-05/SalahFinanceManager-phase05-debug.apk`.

Phase 06 adds people accounts and bilingual person statements. People remain distinct from
the owner’s payment routes: deposits, withdrawals, loans, repayments, and transfers select
one of the owner’s real accounts per movement. The verified Phase 06 artifact is
`../artifacts/phase-06/SalahFinanceManager-phase06-debug.apk`; artifacts and device captures
remain local and ignored.
