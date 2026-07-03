# AGENTS.md

## Cursor Cloud specific instructions

Vocal is a single, **offline-first Android app** (an AAC communication board, package
`org.openaac.vocal`). It is a multi-module Gradle project (`app`, `core/*`, `feature/*`)
that compiles to one debug APK. There is **no backend, database server, or companion
service** to run — Room (SQLite) and DataStore run on-device, and speech uses the
platform Text-to-Speech engine.

### Toolchain (already installed in the VM snapshot)
- **JDK 17** at `/usr/lib/jvm/java-17-openjdk-amd64`. `JAVA_HOME` is exported in
  `~/.bashrc` (the system default `java` is JDK 21). Login shells pick this up; if a
  command runs Gradle without JDK 17, prefix it with
  `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.
- **Android SDK** at `~/android-sdk` (`ANDROID_SDK_ROOT`/`ANDROID_HOME`, also in
  `~/.bashrc`): platform 35, build-tools 35.0.0, platform-tools, emulator.
- `local.properties` is git-ignored and points `sdk.dir` at `~/android-sdk`; the startup
  update script regenerates it, so a fresh checkout does not need manual setup.

### Build / lint / test
- Canonical command: `./scripts/ci.sh` (runs `./gradlew test lint :app:assembleDebug`).
  See `scripts/ci.sh` and `.github/workflows/android-ci.yml`.
- There are currently **no unit/instrumented tests** (v0.1.0), so `./gradlew test` is a
  no-op that still passes; `lint` and `assembleDebug` do the real work.
- Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

### Running the app (emulator caveats — non-obvious)
- This VM has **no `/dev/kvm`** (no hardware acceleration), so the emulator must run in
  software mode. The **API 35 system image watchdog-restarts `system_server` and never
  finishes booting** this way. Use a **lighter, older image** instead (still ≥ `minSdk`
  26), e.g. `system-images;android-30;default;x86_64`.
- Launch headless: `emulator -avd <name> -no-accel -no-snapshot -no-window -gpu
  swiftshader_indirect -no-audio`. Boot to `sys.boot_completed=1` takes ~3–4 min; poll
  with `adb -e shell getprop sys.boot_completed`.
- Install/launch: `adb -e install -r <apk>` then
  `adb -e shell am start -n org.openaac.vocal/.MainActivity`.
- Drive/verify via adb (no desktop window): `adb -e exec-out screencap -p > out.png`,
  `adb -e shell uiautomator dump` for element bounds, `adb -e shell input tap/text`,
  `adb -e shell screenrecord` for video. Input events are **slow** under software
  emulation, and the **soft keyboard shifts dialogs**, so re-dump UI coordinates after
  it opens. Tap ripple animations are usually too fast to capture in `screenrecord`.
- Inspect the Room DB directly:
  `adb -e shell "run-as org.openaac.vocal sqlite3 /data/data/org.openaac.vocal/databases/vocal.db 'SELECT * FROM phrases;'"`.
