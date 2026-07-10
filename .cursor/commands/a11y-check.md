# Accessibility check — Vocal

Review UI changes for AAC accessibility compliance. **Read-only review** — report findings and suggested fixes; do **not** edit code unless the user explicitly asks.

> **Why "a11y"?** Numeronym for **accessibility**: *a* + 11 letters + *y*. Common in dev (like i18n for internationalization).

## Step 1 — Determine scope

1. Run `git diff main...HEAD` (or `git diff` / focus on `@`-mentioned files if the user specified them).
2. Filter to UI-relevant paths:
   - `feature/**/*.kt`
   - `core/ui/**/*.kt`
   - `app/**/*.kt` (Compose/navigation)
   - `**/strings.xml` in those modules
3. If **no UI files** changed, say so and stop — this command does not apply. Suggest `/pr-ready` instead.

Read before reviewing:

- @docs/accessibility.md
- @.cursor/rules/aac-accessibility.mdc

## Step 2 — Review each changed UI file

For each changed file, check against the criteria below. Cite file paths and line numbers when possible.

### Touch and motor

| Check | Requirement |
|-------|-------------|
| Board phrase cells | Min **56dp** (via `AacMinTouchTarget` or equivalent) |
| Other interactive controls | Min **48dp** |
| Grid spacing | Min **8dp** between cells |
| Gestures | No essential action gated only by multi-step gestures |

### TalkBack / screen readers

| Check | Requirement |
|-------|-------------|
| Interactive composables | Meaningful `contentDescription` on every tappable/focusable element |
| Board cells | Description = **spoken phrase text**, not just short label |
| Icon-only buttons | Description from `strings.xml`, not null or generic "button" |
| Decorative images | `contentDescription = null` only when truly decorative |

### Strings and i18n

| Check | Requirement |
|-------|-------------|
| User-visible text | In `strings.xml` of owning module — not hardcoded in Composables/ViewModels |
| Content descriptions | Prefer `stringResource(R.string.…)` for icon buttons |

### AAC UX

| Check | Requirement |
|-------|-------------|
| Board screen | Calm — no caregiver controls on main grid |
| Tap → speak | Immediate feedback; no extra confirmation for user-facing speak actions |
| Speech routing | Through `SpeechRepository`, not direct TTS in UI |
| Network | No new network calls in v1 user flows |

### Visual and cognitive

| Check | Requirement |
|-------|-------------|
| Font scaling | Layouts tolerate system font scaling |
| Color | Sufficient contrast; meaning not conveyed by color alone |
| Navigation | Simple — board for users, settings for caregivers |

## Step 3 — Output format

Use this structure:

### Summary

One paragraph: overall pass / pass with warnings / fail, and what was reviewed.

### Findings

| Severity | File | Issue | Recommendation |
|----------|------|-------|----------------|
| 🔴 Blocker | … | … | … |
| 🟡 Warning | … | … | … |
| 🟢 OK | … | (notable good pattern, optional) | — |

- **Blocker** — must fix before merge (missing contentDescription, touch target too small, hardcoded user string)
- **Warning** — should fix or justify (minor spacing, unclear description)
- **OK** — optional callouts for patterns done well

If no issues: say **No accessibility issues found** and list what was checked.

### Manual verification (author must still run)

Numbered TalkBack/tablet steps tailored to changed screens (from @docs/accessibility.md):

1. Enable TalkBack on tablet emulator
2. Traverse changed screens — each interactive element announced correctly
3. Tap targets comfortable at arm's length
4. (Add screen-specific steps)

### Next steps

- If blockers exist: list fixes in priority order; offer to implement if user asks
- If clean: suggest `/pr-ready` to package the PR

Do **not** run `./scripts/ci.sh` unless the user asks — this command is review-only.
