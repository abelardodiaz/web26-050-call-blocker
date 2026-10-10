# Call Blocker (web26-050)

Android app to block unwanted calls.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![F-Droid](https://img.shields.io/badge/F--Droid-pending-yellow.svg)](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092)
[![Android](https://img.shields.io/badge/Android-9%20--%2017-3DDC84.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)

## Screenshots

<p align="center">
  <img src="docs/screenshots/01-blocked-calls.jpg" width="200" alt="Blocked calls history"/>
  <img src="docs/screenshots/02-block-list.jpg" width="200" alt="Block list"/>
  <img src="docs/screenshots/03-settings-sim.jpg" width="200" alt="Settings and SIM"/>
  <img src="docs/screenshots/04-settings-backup.jpg" width="200" alt="Backup and restore"/>
</p>

## Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Min SDK**: 28 (Android 9+)

## Requirements

- JDK 17
- Android SDK 34
- Device/Emulator Android 9+ (API 28+)

## Build

### On server (server005)

```bash
cd /home/ubuntu/projects/web26-050-call-blocker

# Build debug APK
./gradlew assembleDebug --no-daemon

# Output
ls app/build/outputs/apk/debug/app-debug.apk
```

### Local (Android Studio)

```bash
# Clone
git clone <repo-url>
cd web26-050-call-blocker

# Open in Android Studio and sync Gradle
# Or from CLI:
./gradlew assembleDebug
```

### Install on device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
app/src/main/java/com/callblocker/
|-- core/
|   |-- di/              # Hilt modules
|   |-- service/         # CallScreeningService
|   +-- receiver/        # BootReceiver
|
|-- data/
|   |-- local/
|   |   |-- entity/      # Room entities
|   |   |-- dao/         # Room DAOs
|   |   +-- migration/   # Room migrations
|   +-- repository/      # Repository implementations
|
|-- domain/
|   |-- model/           # Domain models
|   |-- repository/      # Repository interfaces
|   +-- usecase/         # Use cases
|
+-- presentation/
    |-- components/      # Reusable composables
    |-- navigation/      # Navigation setup
    |-- screens/         # Screens (ViewModels + Composables)
    +-- theme/           # Material 3 theme
```

## Features

- [x] Block specific numbers
- [x] **Block by prefix** (e.g.: 442 blocks all numbers starting with 442)
- [x] View blocked calls history
- [x] Configure blocking of private numbers
- [x] **Dual SIM support** - individual blocking per card
- [x] **Dark theme** forced for better readability
- [x] **Interface in Spanish and English**
- [x] Compatible with Android 9 - 17
- [x] **Complete Backup/Restore** with optional AES-256-GCM encryption
- [ ] Blocked call notifications

## Prefix Blocking

Feature that allows blocking number ranges:

1. Open the app > Block List > (+) button
2. Enter the prefix (e.g.: "442")
3. Enable "Block as prefix" switch
4. Save

All numbers starting with that prefix will be blocked.

## Backup and Restore

### Complete Backup
1. Open the app > Settings > Backup
2. Tap "Complete Backup"
3. Choose whether to protect with password
4. File is saved in Downloads

### Restore Backup
1. Settings > Backup > "Restore Backup"
2. Select file (.json or .cbbk)
3. If encrypted, enter password
4. Data is added without duplicating existing entries

**Formats:**
- `.json` - Unencrypted backup (readable)
- `.cbbk` - Encrypted backup with AES-256-GCM

## Permissions

| Permission | Usage |
|------------|-------|
| `READ_PHONE_STATE` | Identify incoming calls |
| `READ_CALL_LOG` | Log blocked calls |
| `ANSWER_PHONE_CALLS` | Reject calls |
| `POST_NOTIFICATIONS` | Notify blocks |
| `ROLE_CALL_SCREENING` | Act as screening service |

## Compatibility Notes

### Samsung devices (One UI)

Early testing (May 2026, Galaxy S23) suggested that calls from numbers saved in Contacts were not delivered to the screening service (Samsung's `SamsungAutoRejectIncomingCallFiltering` reported `contact exists` and allowed the call). **This could not be reproduced later**: with the call screening role correctly held, contacts were blocked on the S23 (July 2026), and on the Galaxy S26 (One UI 9) a blocked contact was rejected by Call Blocker. The May result was probably caused by the role not being bound at the time, or by a system update.

If a number is not being blocked, check first that **Call Blocker is set as the default Call screening app** (Settings > Apps > Default apps) and that no overly broad prefix rule is catching the wrong number. If it still fails and the number is in Contacts, removing it from Contacts is a workaround; please open an issue with `dumpsys telecom` output.

Other OEMs with custom call managers (Xiaomi MIUI, Oppo ColorOS, Vivo FunTouch) may behave differently — not yet verified.

Confirmed on: Samsung Galaxy S23 (SM-S911B), Android 16, One UI 8.5. Also reported working on Samsung Galaxy S26, Android 17, One UI 9 (`dumpsys telecom` confirms calls from Contacts are delegated to Call Blocker's screening service on this device; see `.claude/doc/SAMSUNG_CONTACT_BYPASS.md`).

## Commands

| Command | Description |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests |
| `./gradlew clean` | Clean build |

## Documentation

| Document | Content |
|----------|---------|
| `docs/DEV_NOTES.md` | Technical development notes |
| `CHANGELOG.md` | Change history |
| `.claude/doc/` | Architecture documentation 996 |

## Development Environment (Server005)

```bash
# Environment variables (already in ~/.bashrc)
export ANDROID_HOME=/home/ubuntu/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:/home/ubuntu/gradle-8.7/bin
```

## F-Droid

This app is being reviewed for inclusion in F-Droid.

**Status:** Pending review
**MR:** [#32092](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/32092)

Once approved, it will be available in the F-Droid catalog.

## License

This project is licensed under **GNU General Public License v3.0**.

See [LICENSE](LICENSE) for more details.
