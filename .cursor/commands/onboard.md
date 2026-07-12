# Onboard to Vocal

You are onboarding a contributor to **Vocal** — an offline-first Android AAC app (`org.openaac.vocal`). Do **not** implement code unless the user explicitly asks after this summary.

## Step 1 — Read required context

Read these files in full before answering:

- @AGENTS.md
- @CONTRIBUTING.md
- @.cursor/rules/project-conventions.mdc

Skim if the user is only doing UI or Gradle work:

- @docs/accessibility.md — UI or speech changes
- @.cursor/rules/aac-accessibility.mdc — UI Kotlin files
- @.cursor/rules/gradle-android.mdc — `*.gradle.kts` or `gradle/**` edits

## Step 2 — Produce an onboarding summary

Reply with the sections below. Keep it concise; use tables and bullets. Link to files rather than pasting long excerpts.

### What Vocal is

- One sentence: purpose, audience (AAC users on tablets), offline-first constraint.

### Module map

| Module | Role | Depends on |
|--------|------|------------|
| … | … | … |

Include all modules from `settings.gradle.kts`. State the dependency rule: `feature → core → domain`.

### Architecture in practice

- MVVM placement (ViewModel, use case, repository interface vs implementation)
- Where new code goes for: screen, business logic, persistence, shared UI, app wiring
- Hilt and offline-first constraints (Room, DataStore, local files, no network in v1)

### Build and CI

- Canonical command: `./scripts/ci.sh` and what it runs
- Debug APK path
- JDK 17 requirement; pointer to README and `.vscode/settings.json` for Cursor users
- When to run CI (before PR, after Gradle or significant Kotlin changes)

### Accessibility (AAC)

- Why a11y is non-negotiable for this app
- Touch target minimums, TalkBack / `contentDescription`, `strings.xml` rule
- Manual test expectation: TalkBack on tablet emulator or device

### Cursor-specific aids

- Project rules (always-on vs file-scoped)
- Other slash commands when available: `/feature-request`, `/pr-ready`, `/a11y-check`, `/feature-board`
- `local/` is git-ignored — do not commit personal notes

### First tasks checklist

Numbered list a new contributor should do on day one:

1. Clone, open in Android Studio or Cursor
2. Set `JAVA_HOME` to JDK 17+ if terminal build fails
3. Run `./scripts/ci.sh` successfully
4. Run the `app` configuration on a tablet emulator
5. Read `docs/accessibility.md` before touching UI
6. Open a draft PR using the GitHub PR template

## Step 3 — Tailor to the user

If the user added context after `/onboard` (e.g. "I'm a designer" or "only fixing Gradle"), emphasize the relevant modules and skip deep dives they don't need.

End by asking: **What are you trying to do first?** (feature, bugfix, docs, or Gradle/module work)
