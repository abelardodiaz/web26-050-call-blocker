# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### Pending
- Unit and instrumented tests
- Implement notifications functionality
- Implement blocking of unknown numbers
- Implement blocking of private numbers (hidden ID)
- Cloud backup (Google Drive/Firebase)
- Address backlog items (see `.claude/doc/BACKLOG.md`)

### Milestone

- **F-Droid accepted** (MR [#32092](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092) merged 2026-04-05)
  - App published at: https://f-droid.org/packages/com.callblocker/ (v0.3.3)
  - Reviewer: @linsui

---

## [0.3.3] - 2026-02-13

### Fixed

- **F-Droid build: regenerate launcher icon PNGs**
  - All 10 PNG icons were degenerate 91-byte placeholders (48x48 for all densities)
  - AAPT2 failed during `:app:mergeReleaseResources` in F-Droid pipeline
  - Regenerated with correct sizes: mdpi 48, hdpi 72, xhdpi 96, xxhdpi 144, xxxhdpi 192
  - Design matches adaptive vector drawable (purple #6200EE background + white circle/checkmark)

- **Android 13+ language switching**
  - LocaleManager now used instead of AppCompatDelegate for API 33+
  - Fixes language not persisting correctly on Android 13+ devices

### Added

- **Icon generation script** (`scripts/generate_icons.py`)
  - Uses Pillow to generate launcher PNGs programmatically
  - Generates both regular (rounded rectangle) and round (circular) variants
  - Reproduces the vector drawable design at all Android density levels

---

## [0.3.2] - 2026-01-18

### Added

- **In-app language selector**
  - New "Language" section at top of Settings
  - Three options: Automatic (system), Spanish, English
  - Uses AppCompatDelegate.setApplicationLocales() for per-app language
  - Preference persisted in database
  - Language applied on app startup via MainActivity

### Technical

- Database migration v6 → v7 (appLanguage field in Settings)
- New LocaleHelper utility class for locale management
- New methods in SettingsDao/Repository for language persistence
- LanguageSelector composable with RadioButton options
- Added appcompat dependency for AppCompatDelegate

---

## [0.3.1] - 2026-01-18

### Added

- **Internationalization (i18n)**
  - Externalized all ~80 hardcoded strings to resources
  - Spanish as default locale (values/strings.xml)
  - English locale support (values-en/strings.xml)
  - Locale-aware date formatting

- **Accessibility improvements**
  - contentDescription for all interactive icons
  - Screen reader support for delete and add buttons

- **Open source community documentation**
  - CONTRIBUTING.md: Development setup, code style, PR process
  - SECURITY.md: Vulnerability reporting, encryption details
  - PRIVACY.md: Data handling, permissions, no tracking policy
  - GitLab issue templates (bug report, feature request)

- **README improvements**
  - Translated to English
  - Technology badges (Android, Kotlin, Jetpack Compose)

### Technical

- Navigation.kt: Changed `title: String` to `@StringRes titleResId: Int`
- All composables updated to use `stringResource()`
- Date formatting now uses device locale

---

## [0.3.0-fdroid] - 2026-01-18

### Added (Open Source Preparation)

- **GPLv3 License**
  - LICENSE file with full GNU GPL v3.0 text
  - License section in README.md

- **Documentation screenshots**
  - 5 screenshots in `docs/screenshots/`
  - Screenshots section in README.md with gallery

- **F-Droid Metadata (fastlane)**
  - Structure `fastlane/metadata/android/`
  - Locales: es-MX (Spanish Mexico) and en-US (English)
  - Short and long descriptions
  - Changelogs per version
  - Screenshots per locale

- **F-Droid submission sent**
  - MR #32092: https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092
  - File `metadata/com.callblocker.yml` in fdroiddata
  - Tag v0.3.0 created for build reference
  - Documentation in `docs/fdroid-submission.md`

### Documentation

- `docs/fdroid-submission.md` - Complete submission details
- `fdroid-metadata.yml` - Metadata template for F-Droid
- Updated roadmap with publication progress

---

## [0.3.0] - 2026-01-17

### Added

- **Complete backup with optional encryption**
  - Exports blocked numbers, call history, and settings
  - AES-256-GCM encryption with optional password
  - Unencrypted JSON format (.json) or encrypted (.cbbk)
  - Magic header "CBBK" to identify encrypted files
  - PBKDF2 with 100,000 iterations for key derivation

- **Smart restore**
  - Automatically detects if backup is encrypted
  - Smart merge: adds new numbers, skips duplicates
  - Imports complete blocked call history
  - Restores settings if present in backup
  - Compatible with v1 (numbers only) and v2 (complete) backups

- **New UI dialogs**
  - Dialog to choose whether to protect with password
  - Dialog to enter/confirm password
  - Snackbar with import statistics

### Technical

- New models: `BackupData`, `BackupMetadata`, `BackupCounts`, `ImportResult`
- New class `BackupJsonSerializer` with v1 and v2 support
- New class `BackupEncryption` for AES-256-GCM
- Use cases: `ExportFullBackupUseCase`, `ImportFullBackupUseCase`
- `PasswordDialogState` sealed class for state management
- Component `PasswordDialog` and `BackupPasswordPromptDialog`

### New Files

| File | Purpose |
|------|---------|
| `domain/model/BackupData.kt` | Backup models |
| `data/backup/BackupJsonSerializer.kt` | JSON serialization |
| `data/backup/BackupEncryption.kt` | AES encryption |
| `domain/usecase/ExportFullBackupUseCase.kt` | Complete export |
| `domain/usecase/ImportFullBackupUseCase.kt` | Import with merge |
| `presentation/components/PasswordDialog.kt` | UI dialogs |

---

## [0.2.8.1] - 2026-01-17

### Added

- **Hidden developer mode with 2-step activation**
  - Step 1: Tap "System Information" 7 times quickly
  - Step 2: Tap "About" 7 times (within 10 seconds)
  - Snackbar confirms activation/deactivation
  - Developer section only visible when activated

- **SIM detection by number format**
  - New toggle "Detect SIM by format" in developer section
  - Numbers with +52 = SIM 1 (Bait)
  - Numbers without +52 = SIM 2 (AT&T)
  - Individual toggles to enable/disable blocking per SIM

### Technical

- New fields in Settings: `developerModeEnabled`, `devSimDetectionByFormat`, `devBlockSim1`, `devBlockSim2`
- Database migration v5 → v6
- Detection logic in `CallBlockerScreeningService.determineBlockReason()`
- Tap detector states with `mutableIntStateOf` and `mutableLongStateOf`

### Warning

- SIM detection by format is **fragile and device-specific**
- If SIMs are swapped slots or carriers change, the logic will stop working
- This feature is experimental and hidden by default

---

## [0.2.8] - 2026-01-17

### Fixed

- **Calls with country code were not being blocked**
  - SIM 1 sent numbers with prefix `+52` (e.g.: `+524441390343`)
  - SIM 2 sent numbers without prefix (e.g.: `4441390343`)
  - Prefix `444` didn't match `+524441390343`
  - New function `normalizePhoneNumber()` removes country code before checking
  - Supports Mexico (+52) and USA/Canada (+1) codes

### Technical

- `CallBlockerScreeningService.normalizePhoneNumber()` normalizes incoming numbers
- Removes `+52` and `+1` from numbers with more than 10 digits
- Added logs for normalization diagnostics

---

## [0.2.7] - 2026-01-17

### Changed

- **Dual SIM Simplification: Toggles removed**
  - `PhoneAccountHandle` is `null` on Samsung Android 16 during screening
  - Not possible to determine SIM during call screening
  - Per-SIM toggles removed - blocking applies to all SIMs
  - Settings now shows detected SIMs as visual information only

### Added

- **History enrichment: SIM read post-block**
  - New field `simSlot` in `BlockedCall` model and `BlockedCallEntity`
  - After blocking, the system Call Log is queried
  - The `subscription_id` field from Call Log allows identifying the SIM
  - Blocked calls history now shows "SIM 1" or "SIM 2"
  - Database migration v4 → v5

### Technical

- New function `updateBlockedCallWithSimInfo()` in `CallBlockerScreeningService`
- Uses 1.5s delay to give the system time to log the call
- Compares normalized numbers (last 10 digits)
- Maps `subscriptionId` to `simSlotIndex` via `SubscriptionManager`

### Lessons Learned

- `CallScreeningService` doesn't receive reliable `PhoneAccountHandle` on all devices
- System Call Log DOES have the correct `subscription_id` after the fact
- The "enrich after" strategy is more robust than "detect during"

---

## [0.2.6] - 2026-01-17

### Fixed

- **SIMs not detected on Android 12+ (API 31+)**
  - `getActiveSubscriptionInfoList()` requires **both** permissions: READ_PHONE_STATE and READ_PHONE_NUMBERS
  - `READ_PHONE_NUMBERS` moved from optional to required permissions (only on API 31+)
  - PermissionHandler.kt: Adds READ_PHONE_NUMBERS in `getRequiredPermissions()` for API 31+
  - SimManager.kt: Checks READ_PHONE_NUMBERS before querying SIMs on API 31+

### Improved

- **Phone number detection per SIM**
  - API 33+: Uses `SubscriptionManager.getPhoneNumber()`
  - API 31-32: Uses `SubscriptionInfo.number` (deprecated but functional)
  - Supports physical SIMs and eSIM

---

## [0.2.5] - 2026-01-17

### Fixed

- **Critical bug: settings were resetting each other**
  - Changing one setting (e.g. SIM 1) reset others (e.g. Persistent Service)
  - Cause: SettingsDao used OnConflictStrategy.REPLACE
  - Solution: Changed to OnConflictStrategy.IGNORE

### Changed

- **"Block Private" disabled**
  - Functionality not yet implemented
  - Shown as "Coming soon..." like other pending options

---

## [0.2.4] - 2026-01-17

### Added

- **Persistent Service (Foreground Service)**
  - New option "Persistent Service" in Settings
  - Permanent notification "Active Protection" when enabled
  - Improves blocking reliability in low power mode
  - Works on all Android versions (not just Android 9)

- **Auto-start on reboot**
  - BootReceiver starts the service if enabled in settings
  - On Android 9 always starts (required for legacy blocking)

### Changed

- Renamed LegacyCallBlockerService → CallBlockerForegroundService
- Database migration v3 → v4 (field persistentServiceEnabled)

### Known Issues

- Manually disable battery optimization for better operation

---

## [0.2.3] - 2026-01-17

### Added

- **Export/Import of blocked list**
  - Export blocked numbers to JSON file in Downloads
  - Import numbers from JSON file
  - New "Backup" section in Settings
  - Use cases: ExportBlockedNumbersUseCase, ImportBlockedNumbersUseCase
  - SettingsButton component for action buttons

### Fixed

- **Crash on Android 9 when opening Settings**
  - SimManager.getPhoneNumber() now checks API level >= 33
  - Method getPhoneNumber(subscriptionId) only available on API 33+

- **SIMs not appearing on Android 16**
  - Changed from `remember {}` to reactive state with `LaunchedEffect`
  - SIMs are re-evaluated when permission state changes

- **Improved SIM detection for dual SIM**
  - getSubscriptionIdFromCall() now uses multiple detection methods
  - Search by ICC ID for greater compatibility
  - Added logging for diagnostics

### Known Issues

- **Android 9**: Blocking only works with phone unlocked
- **Android 16**: Blocked calls are logged as missed

---

## [0.2.2] - 2026-01-17

### Fixed

- **Dual SIM blocking functional**
  - CallBlockerScreeningService now checks which SIM the call comes from
  - Respects per-SIM settings configuration
  - If no SIMs configured, blocks on all (legacy behavior)

---

## [0.2.1] - 2026-01-17

### Added

- **Dark theme by default**
  - Colors optimized for dark mode
  - Forced theme (no light theme option)
  - Cards with surfaceVariant from theme

- **Complete interface in Spanish**
  - BlockListScreen: "Lista de Bloqueo", "Agregar numero", etc.
  - BlockedCallsScreen: "Llamadas Bloqueadas", etc.
  - AddNumberDialog: "Agregar Numero Bloqueado", etc.
  - Navigation: "Llamadas Bloqueadas", "Lista de Bloqueo", "Ajustes"

- **Dual SIM Support**
  - SimManager for detecting active SIMs
  - Per-SIM configuration section in Settings
  - enabledSimSlots field in Settings for persistence
  - Individual blocking enabled/disabled per SIM

### Changed

- Options "Block Unknown" and "Show Notifications" disabled (pending implementation)
- Theme colors updated for better dark mode readability

### Fixed

- Version history correction (0.1.0 -> 0.2.0 -> 0.2.1)

---

## [0.2.0] - 2026-01-17

### Added

- **Prefix blocking**
  - New field `isPrefix` in BlockedNumber model
  - Switch "Block as prefix" in add number dialog
  - Badge "PREFIX" on blocked number cards
  - Database migration v1 → v2

- **Build environment**
  - Gradle wrapper (gradlew, gradlew.bat)
  - gradle.properties with AndroidX configuration
  - Launcher icons (placeholder)
  - Adaptive icons (vector XML)

- **Documentation**
  - docs/DEV_NOTES.md with technical development notes

### Changed

- Query `isNumberBlocked()` fixed for exact + prefix matching
- AppDatabase version 1 → 2
- AppModule includes migration MIGRATION_1_2

### Fixed

- Blocking query that was doing incorrect bidirectional matching

---

## [0.1.0] - 2026-01-17

### Added

- **Initial project structure**
  - Clean Architecture (domain, data, presentation)
  - Hilt dependency injection
  - Room database with 3 entities

- **CallScreeningService**
  - Intercepts incoming calls
  - Blocks according to configuration
  - Logs blocked calls

- **UI with Jetpack Compose**
  - Blocked calls screen
  - Block list screen
  - Settings screen
  - Bottom navigation

- **Data models**
  - BlockedNumber (number, label, date)
  - BlockedCall (number, timestamp, reason)
  - Settings (configuration flags)

- **Documentation 996**
  - Context session file
  - system-architecture.md
  - ui-design.md
  - app-architecture.md

---

## Change Types

- **Added**: New features
- **Changed**: Changes to existing features
- **Deprecated**: Features that will be removed
- **Removed**: Deleted features
- **Fixed**: Bug fixes
- **Security**: Vulnerability fixes
