#!/usr/bin/env bash
# Diagnose emulator/host audio and TTS for Vocal development.
# Run while an AVD is booted. See README troubleshooting or plan docs for GUI steps.
set -euo pipefail

cd "$(dirname "$0")/.."

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
ADB="$ANDROID_HOME/platform-tools/adb"
EMULATOR="$ANDROID_HOME/emulator/emulator"

if [[ ! -x "$ADB" ]]; then
  echo "error: adb not found at $ADB" >&2
  echo "Set ANDROID_HOME or install Android SDK Platform-Tools." >&2
  exit 1
fi

echo "== Host (macOS) =="
if command -v osascript >/dev/null 2>&1; then
  vol="$(osascript -e 'output volume of (get volume settings)' 2>/dev/null || echo '?')"
  echo "  System volume: ${vol}% (should be > 0; unmute in menu bar if needed)"
fi
echo "  Architecture: $(uname -m) (Apple Silicon should use arm64-v8a AVD images)"

echo
echo "== ADB device =="
if ! "$ADB" get-state >/dev/null 2>&1; then
  echo "  No emulator/device connected. Boot an AVD from Android Studio first." >&2
  exit 1
fi
"$ADB" devices -l

echo
echo "== Emulator process (look for -no-audio; should be absent) =="
if pgrep -lf qemu-system >/dev/null 2>&1; then
  pgrep -lf qemu-system | head -1
else
  echo "  qemu-system not found in process list (device may be physical hardware)."
fi

echo
echo "== AVD config =="
AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.android/avd}"
if [[ -d "$AVD_HOME" ]]; then
  while IFS= read -r ini; do
    name="$(basename "$ini" .ini)"
    abi="$(grep -E '^abi\.type=' "${ini%.ini}.avd/config.ini" 2>/dev/null | cut -d= -f2 || true)"
    audio_out="$(grep -E 'hw\.audioOutput' "${ini%.ini}.avd/hardware-qemu.ini" 2>/dev/null | tail -1 || true)"
    echo "  $name: abi=${abi:-unknown} ${audio_out:-hw.audioOutput not found}"
  done < <(find "$AVD_HOME" -maxdepth 1 -name '*.ini' ! -name '*.encoding*' 2>/dev/null)
fi

if [[ -x "$EMULATOR" ]]; then
  echo "  Emulator version: $("$EMULATOR" -version 2>&1 | head -1)"
fi

echo
echo "== In-emulator volume (should not be 0/muted) =="
"$ADB" shell dumpsys audio 2>/dev/null | grep -E 'STREAM_MUSIC:|Muted:|streamVolume:|Devices:' | head -8 || true
"$ADB" shell cmd media_session volume --stream 3 --get 2>&1 | tail -3 || true

echo
echo "== TTS engine =="
tts_default="$("$ADB" shell settings get secure tts_default_synth 2>/dev/null | tr -d '\r')"
if [[ "$tts_default" == "null" || -z "$tts_default" ]]; then
  echo "  tts_default_synth is unset; setting com.google.android.tts"
  "$ADB" shell settings put secure tts_default_synth com.google.android.tts
  tts_default="com.google.android.tts"
fi
echo "  Default engine: $tts_default"
"$ADB" shell pm list packages 2>/dev/null | grep -i tts || echo "  (no TTS packages found)"

echo
echo "== Vocal app =="
if "$ADB" shell pm list packages 2>/dev/null | grep -q org.openaac.vocal; then
  echo "  org.openaac.vocal is installed"
else
  echo "  org.openaac.vocal not installed — run ./gradlew :app:installDebug"
fi

echo
echo "== TTS synthesis smoke test (logcat) =="
"$ADB" logcat -c
"$ADB" shell am start -n org.openaac.vocal/.MainActivity >/dev/null 2>&1 || true
sleep 2
# Tap approximate center of first board cell on landscape tablet layout.
"$ADB" shell input tap 188 372 2>/dev/null || true
sleep 2
if "$ADB" logcat -d 2>/dev/null | grep -q 'GoogleTTSServiceImpl.*Synthesis request'; then
  echo "  OK: Google TTS received a synthesis request (app + TTS stack work)."
else
  echo "  WARN: No TTS synthesis in logcat. Open Vocal, tap a phrase, or check TTS settings."
fi

echo
echo "== Interpretation =="
cat <<'EOF'
  • If synthesis appears in logcat but you hear nothing:
      Emulator → Mac audio routing is broken (not a Vocal bug).
      Try: Device Manager → Cold Boot Now; update SDK Emulator;
      Extended controls → Settings → enable host audio; verify Mac output device.
  • If logcat shows LANG_MISSING_DATA or TTS init errors:
      Settings → Accessibility → Text-to-speech → install Google TTS voice data.
  • For reliable AAC testing, use a physical Android device via USB debugging.
EOF

echo
echo "Manual checks (Android Studio GUI):"
echo "  1. Extended controls (⋯) → Settings → enable audio / max volume"
echo "  2. Settings → Sound → preview a ringtone"
echo "  3. Settings → Accessibility → Text-to-speech → Listen to an example"
