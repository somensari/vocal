# AGENTS.md

Vocal is an **offline-first Android AAC app** (package `org.openaac.vocal`). There is no
backend — Room, DataStore, and local files hold all data; speech uses platform TTS.

This file serves two audiences:

| Section | Audience |
|---------|----------|
| [Human quickstart](#human-quickstart) | Any contributor (Cursor, Android Studio, or other) |
| [Agent instructions](#agent-instructions) | AI coding agents and Cursor Cloud |

Full contribution workflow: [CONTRIBUTING.md](CONTRIBUTING.md).

---

## Human quickstart

### Modules

| Module | Role |
|--------|------|
| `app` | Application entry, Hilt wiring, navigation shell |
| `core/domain` | Models, repository interfaces, use cases |
| `core/data` | Room, DataStore, repository implementations |
| `core/ui` | Shared Compose theme and components |
| `feature/board` | Main AAC board (MVVM) |
| `feature/settings` | Caregiver settings (MVVM) |

Architecture: MVVM in feature modules; business logic in `core/domain` use cases;
repository interfaces in domain, implementations in `core/data`. Hilt for DI.

### CI

Canonical command (matches GitHub Actions):

```bash
./scripts/ci.sh    # test, lint, :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

See [README.md](README.md) for local setup (JDK 17, Android SDK). Cursor/VS Code users:
set `JAVA_HOME` to JDK 17+ (see `.vscode/settings.json`).

### Accessibility

UI changes must meet AAC accessibility requirements:

- Read [docs/accessibility.md](docs/accessibility.md) before merging UI work
- Test with TalkBack on a tablet emulator or device
- PR checklist: [.github/pull_request_template.md](.github/pull_request_template.md)

**Cursor:** run `/onboard` for a project summary; `/feature-request` to draft/file a GitHub Feature Request; `/feature-board` for board work; `/a11y-check` before UI PRs; `/pr-ready` to package the PR.

---

## Agent instructions

Read this section when implementing or reviewing code in Agent mode.

### Project rules (required context)

Cursor loads these automatically; follow them even when not `@`-mentioned:

- `.cursor/rules/project-conventions.mdc` — architecture, offline-first, i18n, code style
- `.cursor/rules/aac-accessibility.mdc` — scoped to UI Kotlin files (touch targets, content descriptions)
- `.cursor/rules/gradle-android.mdc` — scoped to `*.gradle.kts` and `gradle/**` (KSP/Hilt, modules, version catalog)

Also read [docs/accessibility.md](docs/accessibility.md) for any UI or speech change.

### Constraints

- **Offline-first:** no network calls unless explicitly requested for a cloud feature
- **Strings:** never hardcode user-visible text in Composables or ViewModels; use `strings.xml` in the owning module
- **Persistence:** structured data in Room; preferences in DataStore; icons and recorded audio as local files with paths in Room
- **Scope:** prefer small, reviewable diffs; match existing naming and file layout
- **License:** Apache 2.0; document public APIs and non-obvious AAC behavior

### Before finishing a change

1. Run `./scripts/ci.sh` (or at minimum `./gradlew lint :app:assembleDebug` for non-logic edits)
2. For UI changes, verify against `docs/accessibility.md` and the a11y rule
3. Do not commit secrets, personal data, or files under `local/` (git-ignored)

### Module placement guide

| Change type | Where it goes |
|-------------|---------------|
| New screen / ViewModel | `feature/*` |
| Business logic | `core/domain` use case |
| Database / preferences | `core/data` |
| Shared UI component | `core/ui` |
| Navigation / DI module | `app` |

### GitHub (MCP or `gh`)

When GitHub MCP is enabled (`~/.cursor/mcp.json`, see `.cursor/mcp.json.example`):

- Look up issues and PRs for `somensari/vocal` before implementing from a ticket
- Check CI status on open PRs when babysitting or reviewing
- Prefer MCP for structured GitHub queries; fall back to `gh` CLI if MCP is unavailable

Do not commit tokens or copy MCP config with real credentials into the repo.

---

## Cursor Cloud specific instructions

These notes apply to Cloud Agent VMs, not typical local Android Studio setups.

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
