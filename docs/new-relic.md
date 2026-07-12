# New Relic monitoring (maintainers)

Vocal can optionally report crashes, ANRs, handled exceptions, and app-start /
performance metrics to [New Relic Mobile](https://docs.newrelic.com/docs/mobile-monitoring/new-relic-mobile-android/get-started/introduction-new-relic-mobile-android/).
Instrumentation is **silent** (no caregiver or AAC-user UI) and is an explicit
network exception to the offline-first product rule.

## Defaults

- Agent is **off** unless both an enable flag and application token are supplied
  at build time.
- Normal local and CI builds do **not** require a token.
- Board and settings UI are unchanged.

## Enable for a local build

Add to git-ignored `local.properties` (repo root):

```properties
sdk.dir=/path/to/Android/sdk
newrelic.enabled=true
newrelic.token=YOUR_NEW_RELIC_MOBILE_APP_TOKEN
```

Then assemble as usual:

```bash
./gradlew :app:assembleDebug
# or
./scripts/ci.sh
```

Create the Mobile app and token in New Relic:
[Install the Android agent](https://docs.newrelic.com/docs/mobile-monitoring/new-relic-mobile-android/install-configure/install-android-agent-gradle/).

## Enable in CI

Set repository or organization secrets / variables (never commit them):

| Secret / env | Purpose |
|--------------|---------|
| `NEW_RELIC_ENABLED` | Set to `true` to turn the agent on for that build |
| `NEW_RELIC_TOKEN` | New Relic Mobile application token |

Gradle also accepts `-Pnewrelic.enabled=true` and `-Pnewrelic.token=…`.

Precedence: Gradle `-P` → environment → `local.properties`.

Example CI step (optional; default workflow leaves the agent off):

```yaml
- name: Configure SDK path and optional New Relic
  env:
    NEW_RELIC_TOKEN: ${{ secrets.NEW_RELIC_TOKEN }}
    NEW_RELIC_ENABLED: ${{ vars.NEW_RELIC_ENABLED }}
  run: |
    echo "sdk.dir=${ANDROID_SDK_ROOT}" > local.properties
    # Agent stays off unless both are present (see app/build.gradle.kts).
```

Or export the env vars before `./scripts/ci.sh` / `./gradlew`.

## Disable without code changes

Omit the keys, set `newrelic.enabled=false`, or leave `NEW_RELIC_ENABLED` unset.
Rebuild — `BuildConfig.NEW_RELIC_ENABLED` will be `false` and
`NewRelicMonitoring.start` no-ops.

## What is reported

When enabled, the agent uses New Relic Mobile defaults for:

- Crashes and ANRs
- Handled exceptions (`NewRelicMonitoring.recordHandledException` / agent defaults)
- App start and screen / interaction performance metrics

## Privacy (AAC content)

**Do not** send phrase text, custom icon paths, recorded audio paths, or other
AAC communication content as custom attributes or events. Use
`NewRelicMonitoring.recordHandledException` only with non-content metadata
(for example component names or error codes).

## ProGuard / R8

Agent 7.6.8+ ships keep rules via the Gradle plugin. Do not commit
`app/newrelic.properties` with a real token; map upload is disabled when the
agent is not active for the build.

## Manual verification

1. Enable with a real token (local or CI secrets).
2. Install an instrumented APK; force a test crash or call
   `NewRelicMonitoring.recordHandledException`.
3. Confirm events in the New Relic Mobile dashboard.
4. Cold-start and confirm app-start / performance data.
5. Rebuild with the agent disabled; confirm startup logs skip init and CI needs
   no token.
6. Smoke board + settings — UI unchanged.
