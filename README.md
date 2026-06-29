# Vocal

[![Android CI](https://github.com/somensari/vocal/actions/workflows/android-ci.yml/badge.svg)](https://github.com/somensari/vocal/actions/workflows/android-ci.yml)

**Vocal** is a free, open-source Android AAC (Augmentative and Alternative Communication) app for tablets. It helps children and people with speech impairments express thoughts and daily needs through simple, customizable communication boards.

## Status

Early development (v0.1.0). Local-first: phrases and settings are stored on device.

## Features (current)

- Communication board with large, accessible tap targets
- Tap a phrase to speak it with Android Text-to-Speech
- Caregiver settings to add, edit, and delete phrases
- Starter board seeded on first launch

## Architecture

Multi-module Kotlin project:

- `app` — navigation shell
- `core/domain` — models, repository interfaces, use cases
- `core/data` — Room, DataStore, TTS, file storage hooks
- `core/ui` — theme and shared AAC UI components
- `feature/board` — communication grid
- `feature/settings` — caregiver phrase editor

Stack: Jetpack Compose, MVVM + Repository, Hilt, Room, DataStore.

## Requirements

- Android Studio Ladybug or newer
- **JDK 17+** for Gradle builds (bundled with Android Studio)
- Android SDK 35
- minSdk 26 (Android 8.0)

## Getting started

1. Open this folder in Android Studio.
2. Let Gradle sync complete.
3. Run the `app` configuration on a tablet emulator or device.

### Building from the terminal

Gradle must use **Java 17 or newer**. If `./gradlew` fails with a Java 9/11 error, your shell is picking up an old JDK.

**Quick fix (macOS with Android Studio):**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # should show 17 or 21
./gradlew :app:assembleDebug
```

Or use the CI script, which auto-selects Android Studio's JDK when needed:

```bash
./scripts/ci.sh
```

### CI checks (same as GitHub Actions)

```bash
./scripts/ci.sh
```

## Accessibility

Vocal is built for AAC users first:

- Minimum 56dp touch targets on board cells
- TalkBack labels use spoken phrase text
- All user-visible strings live in `strings.xml` resources

See `docs/accessibility.md` for the checklist.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
