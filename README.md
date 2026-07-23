## Salah Finance Manager

An offline-first, privacy-sensitive Android personal finance foundation. Phase 04
bootstraps the native Kotlin/Compose application under
[`android-app`](android-app/README.md), including Arabic RTL and English LTR shell
support and persisted application preferences.

### Build and verify

From `android-app` in PowerShell:

```powershell
.\\gradlew.bat spotlessCheck
.\\gradlew.bat test
.\\gradlew.bat lintDebug
.\\gradlew.bat assembleDebug
```

See [Phase 04](docs/phases/04-android-project-bootstrap-and-foundation.md) and
[verification](docs/testing/phase-04-verification.md) for architecture and results.

### Privacy

Do not place real financial data, statements, account numbers, exports, backups,
or personal screenshots in this repository. The application has no network backend
and does not request Internet or broad-storage permissions.
