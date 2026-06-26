#!/usr/bin/env bash
# Shared CI entry point — run locally and in GitHub Actions.
set -euo pipefail

cd "$(dirname "$0")/.."

chmod +x gradlew

./gradlew test lint :app:assembleDebug --stacktrace
