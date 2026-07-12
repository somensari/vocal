---
name: vocal-aac
description: >-
  Vocal AAC workflows — phrases, Room database, TalkBack checks, and adb
  debugging. Use when editing board/settings UI, phrase data, Room schemas,
  seed data, or verifying accessibility on emulator/device.
---

# Vocal AAC skill

Offline-first Android AAC app (`org.openaac.vocal`). User flows have no network.

## When to use

- Adding or editing phrases, board layout, or caregiver settings
- Debugging Room / phrase persistence
- Verifying TalkBack, touch targets, or tablet layout
- Running headless emulator checks (Cloud Agent or CI-like verification)

## Architecture reminders

| Task | Module |
|------|--------|
| Board UI | `feature/board` |
| Settings / phrase editor | `feature/settings` |
| Use cases | `core/domain` |
| Room, DataStore, TTS | `core/data` |
| Shared UI (`AacCellButton`, theme) | `core/ui` |

- Route speech through `SpeechRepository`, never direct TTS in Composables
- User-visible text in `strings.xml`; board cell `contentDescription` = spoken phrase text
- Board cells ≥ 56dp; other controls ≥ 48dp

## Phrase model

```kotlin
Phrase(id, boardId, label, spokenText, row, column, iconPath?, audioPath?)
```

- `label` — short text on button; `spokenText` — what TTS speaks (and TalkBack announces)
- `iconPath` — bundled keys like `vocal://bundled-icons/starter/water` or custom file path
- `audioPath` — optional recorded audio file; TTS is default when null

Grid sizing: `computeBoardGrid(phraseCount)` in `core/domain` (max 32 phrases v1).

## Room debugging

**On device/emulator** (package `org.openaac.vocal`):

```bash
adb shell "run-as org.openaac.vocal sqlite3 /data/data/org.openaac.vocal/databases/vocal.db 'SELECT id, label, spokenText FROM phrases;'"
adb shell "run-as org.openaac.vocal sqlite3 /data/data/org.openaac.vocal/databases/vocal.db 'SELECT * FROM boards;'"
```

Schema exports live in `core/data/schemas/` — update when changing `@Entity` classes.

## Emulator / adb (Cloud VM)

Software emulation only on Cloud VMs — use API 30 image, not API 35:

```bash
emulator -avd <name> -no-accel -no-snapshot -no-window -gpu swiftshader_indirect -no-audio
adb -e shell getprop sys.boot_completed   # wait for 1
adb -e install -r app/build/outputs/apk/debug/app-debug.apk
adb -e shell am start -n org.openaac.vocal/.MainActivity
```

Verify UI without a window:

```bash
adb -e exec-out screencap -p > /tmp/vocal-screen.png
adb -e shell uiautomator dump /sdcard/ui.xml && adb -e pull /sdcard/ui.xml
```

Re-dump UI after opening soft keyboard — dialog coordinates shift.

## TalkBack manual check

1. Tablet emulator (10" class), enable TalkBack
2. Traverse board — each cell announces **spoken text**
3. Tap → immediate speech, no extra confirmation on user speak actions
4. Settings screen: caregiver controls only; board stays calm

Full checklist: `docs/accessibility.md` · rule: `.cursor/rules/aac-accessibility.mdc`

## Related commands

- `/feature-board` — implement board changes
- `/a11y-check` — pre-PR accessibility review
- `/pr-ready` — CI + PR packaging

Run `./scripts/ci.sh` before finishing implementation work.
