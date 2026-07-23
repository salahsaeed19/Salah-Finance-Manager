# Phase 04 — Android Project Bootstrap and Foundation

**Status:** Technically complete and physically smoke-tested. Connected instrumentation execution remains blocked only by a phone-side test-APK install cancellation.

## Objective

Create the native, offline-first Android foundation before any financial ledger or
real-record feature. The shell deliberately renders only zero-value and empty states.

## Environment and versions

The verified build session uses Android Studio JBR 21.0.10 at
`C:\Program Files\Android\Android Studio\jbr` and Android SDK
`C:\Android\Sdk`. `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` were set
for the build session, and ignored `android-app/local.properties` points Gradle at
that SDK. Installed required packages are `platform-tools`, `platforms;android-35`,
`build-tools;35.0.0`, and Android command-line tools. The project pins AGP 8.8.2,
Gradle 8.10.2, Kotlin 2.1.0, Compose BOM 2025.01.01, Hilt 2.55, Room 2.6.1,
Proto DataStore 1.1.2, and WorkManager 2.10.0.

## Modules and architecture

The project lives in `android-app` and uses Kotlin DSL, a version catalog, an included
`build-logic` convention build, and these modules:

```text
:app
:core:common :core:model :core:money :core:ledger
:core:database :core:data :core:designsystem :core:ui :core:files :core:security :core:testing
:feature:dashboard :feature:accounts :feature:people :feature:transactions :feature:import
:feature:review :feature:reconciliation :feature:budget :feature:savings :feature:reports :feature:settings
```

`app` owns the single activity, Hilt application, WorkManager configuration, and shell.
Features depend on shared core abstractions rather than Room DAOs or other features.
Room contains only a documented internal metadata entity so its configuration can be
verified without fabricating financial data. The ledger remains a boundary interface.

## Delivered UI and localization

The Compose/Material 3 design system implements the approved purple palette, system
type scale, spacing, radii, touch target tokens, money components, state components,
hero card, scaffold, FAB, and quick-action sheet. The five destinations are Home,
Transactions, People, Plans, and Settings. Dashboard hierarchy, hero and summary
cards, grouped settings, empty states, quick actions, and compact five-tab navigation
were refined for phone widths. Creation actions display a localized, honest later-phase
notice.

English and Arabic resources are complete for Phase 04 visible UI; Arabic is the
first-launch default. The app manifest enables RTL and Android 13 locale configuration.
On Android 13+, `LocaleManager.applicationLocales` applies the platform per-app locale;
on API 26–32 AppCompat provides the supported fallback. The selected locale is also
persisted in Proto DataStore. The app supports System, Light, and Dark themes.

## Completed preferences vertical slice

Proto DataStore persists language, theme, and hide-amount values. It has a serializer,
corruption handler, repository, Hilt bindings, and view models. The dashboard reacts
to hide amounts and formatting uses `Long` minor units and `BigDecimal` only to render
those integers; it does not perform financial calculations with floating point.

## Tests and verification

Focused unit tests cover ILS/USD zero formatting, Arabic/negative formatting, currency
mismatch rejection, and Proto defaults/round trip. `FinanceAppSmokeTest` covers all
bottom destinations and the quick-action sheet using UI tags. After a clean state, the following
commands passed: `gradlew.bat --version`, `clean`, `spotlessCheck`, `test`,
`lintDebug`, and `assembleDebug` (all with the Gradle wrapper). The first Gradle 8.10.2
download was allowed to complete instead of being cut off at 60 seconds.

The final debug APK is
`android-app/app/build/outputs/apk/debug/app-debug.apk` (18,973,403 bytes), copied
byte-identically to `artifacts/phase-04/SalahFinanceManager-phase04-debug.apk`.
SHA-256: `5E652FDFA7C354B369752458552A659F615B9189841F410EECDE2D8F35A23887`.
`aapt2` verified application ID `com.salahabusaif.financemanager`, version `0.4.0`
(code 1), min SDK 26, target SDK 35, and an application label. `apksigner` verified
the Android Debug certificate with APK Signature Scheme v2. Manifest inspection found
no Internet or broad-storage permission. The APK was installed, launched, and
smoke-tested on POCO X3 Pro model M2102J20SG (product `vayu_global`, codename `vayu`).
Arabic RTL and English LTR were verified immediately after selection and after
force-stop/relaunch, along with System/Light/Dark themes, hide/show amounts, all five
tabs, and quick actions. Focused logcat had no application fatal exception.
`connectedDebugAndroidTest` built the test APK but did not run because MIUI cancelled
installation with `INSTALL_FAILED_USER_RESTRICTED`. The same device policy cancelled a
final non-interactive production-APK reinstall after artifact validation; accept the
phone's install prompt to install that exact final artifact. Sanitized screenshots are
in `docs/screenshots/phase-04/`.

## Known limitations and next phase

There is no financial schema, double-entry posting implementation, CRUD, import
parser, bank integration, backup, PIN, biometric, network client, or cloud service.
Those are deliberately outside this bootstrap phase. The next phase should introduce
the approved financial-engine vertical slices and Room migrations non-destructively.

## Git

No commit, push, or branch creation was performed. Recommended commit message:

```text
feat(phase-04): finalize bilingual Android foundation and device validation
```
