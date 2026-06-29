#!/usr/bin/env bash
# Ensure Gradle runs with JDK 17+. Sources Android Studio's bundled JDK on macOS when needed.
set -euo pipefail

required_major=17

java_major() {
  "$1" -version 2>&1 | awk -F '"' '/version/ {split($2, parts, "."); print parts[1]}'
}

use_java_home() {
  local home="$1"
  if [ -x "$home/bin/java" ]; then
    local major
    major="$(java_major "$home/bin/java")"
    if [ "$major" -ge "$required_major" ] 2>/dev/null; then
      export JAVA_HOME="$home"
      export PATH="$JAVA_HOME/bin:$PATH"
      echo "Using JAVA_HOME=$JAVA_HOME (Java $major)"
      return 0
    fi
  fi
  return 1
}

if [ -n "${JAVA_HOME:-}" ] && use_java_home "$JAVA_HOME"; then
  return 0 2>/dev/null || exit 0
fi

if command -v java >/dev/null 2>&1; then
  current_major="$(java_major "$(command -v java)")"
  if [ "$current_major" -ge "$required_major" ] 2>/dev/null; then
    return 0 2>/dev/null || exit 0
  fi
else
  current_major="unknown"
fi

for candidate in \
  "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
  if use_java_home "$candidate"; then
    return 0 2>/dev/null || exit 0
  fi
done

cat >&2 <<EOF
ERROR: Vocal requires JDK ${required_major}+, but the active Java is ${current_major}.

Fix options:
  1. Build from Android Studio (recommended) — it uses the bundled JDK automatically.
  2. Point your terminal at Android Studio's JDK:
       export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
       export PATH="\$JAVA_HOME/bin:\$PATH"
  3. Install JDK 17+ and set JAVA_HOME before running ./gradlew.

Then verify with: java -version
EOF
return 1 2>/dev/null || exit 1
