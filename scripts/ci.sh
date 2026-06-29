#!/usr/bin/env bash
# Shared CI entry point — run locally and in GitHub Actions.
set -euo pipefail

cd "$(dirname "$0")/.."

# GitHub Actions sets up JDK 17 explicitly; locally we may need Android Studio's JDK.
if [ -z "${CI:-}" ]; then
  # shellcheck source=ensure-java.sh
  source "$(dirname "$0")/ensure-java.sh"
fi

chmod +x gradlew

./gradlew test lint :app:assembleDebug --stacktrace
