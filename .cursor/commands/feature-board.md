# Board feature — Vocal

Implement or extend a **communication board** feature in Vocal. Follow existing patterns in `feature/board` — do not invent a parallel architecture.

## Step 0 — Understand the request

The user's message after `/feature-board` is the feature spec (e.g. "add theme picker to board", "show loading shimmer", "support phrase categories"). If vague, ask one clarifying question before coding.

Read these references first:

- @feature/board/src/main/kotlin/org/openaac/vocal/feature/board/BoardViewModel.kt
- @feature/board/src/main/kotlin/org/openaac/vocal/feature/board/BoardScreen.kt
- @.cursor/rules/project-conventions.mdc
- @.cursor/rules/aac-accessibility.mdc
- @docs/accessibility.md

## Step 1 — Plan before coding

Reply with a short **implementation plan** (bullet list) covering:

| Layer | Module | What changes |
|-------|--------|--------------|
| UI | `feature/board` | Composables, `BoardUiState`, previews |
| ViewModel | `feature/board` | State, user actions → use cases |
| Business logic | `core/domain` | New use case(s) only if needed |
| Data | `core/data` | Only if persistence/API changes |
| Shared UI | `core/ui` | Reuse `AacCellButton`, `AacSecondaryButton`, `VocalTheme` |
| Wiring | `app` | Only if navigation or new Hilt bindings needed |

**Board screen rules (non-negotiable):**

- **Calm board** — no caregiver controls (add/edit/delete phrases) on the main grid; those live in `feature/settings`
- **Tap → speak immediately** via `SpeakPhraseUseCase` / `SpeechRepository` — no confirmation dialogs for users
- **No network** in v1 user flows
- **Strings** in `feature/board/src/main/res/values/strings.xml` (or owning module) — never hardcoded in Compose/ViewModel

Wait for user approval if the plan touches `core/data`, Room schema, or multiple modules. For small UI-only changes in `feature/board`, proceed after showing the plan.

## Step 2 — Follow existing board patterns

### MVVM structure

```
BoardRoute()          → hiltViewModel(), collect uiState
BoardScreen()         → stateless composable (testable, previews)
BoardViewModel        → StateFlow<BoardUiState>, calls use cases
```

- Expose UI state as `StateFlow`; collect with `collectAsStateWithLifecycle()`
- User actions: `fun onX()` in ViewModel → `viewModelScope.launch { useCase() }`
- Do **not** call repositories from Composables or ViewModels directly — use existing or new use cases in `core/domain`

### Grid and cells

- Layout: `computeBoardGrid()` from `core/domain` for slot dimensions
- Phrase cells: **`AacCellButton`** from `core/ui` — already enforces semantics and sizing
- `contentDescription = phrase.spokenText` (not just `phrase.label`)
- Grid gutter: match existing `BoardGridGutter` spacing; keep ≥ 8dp effective spacing where possible
- Bundled icons: extend `BundledPhraseIconResolver.kt` + `res/drawable/` if adding starter icons

### Speech

- Speaking: `SpeakPhraseUseCase` only — never instantiate TTS in UI
- Speech rate: already handled via `SpeechRepository` / preferences — don't bypass

### Previews

- Add `@Preview` composables in `BoardScreen.kt` for new visual states (tablet width ~800dp)
- Use `VocalTheme { }` wrapper

## Step 3 — Implement

- Match naming and file layout of existing board files
- Prefer small, focused diffs
- If adding domain logic, add unit tests in `core/domain/src/test/` when behavior is non-trivial (follow `BoardGridLayoutTest` pattern)
- Document non-obvious AAC behavior in KDoc where helpful

## Step 4 — Verify

1. Run `./scripts/ci.sh` — fix failures before finishing
2. Self-review against @.cursor/rules/aac-accessibility.mdc:
   - 56dp board cells, 48dp secondary controls
   - `contentDescription` on all interactive elements
   - No hardcoded user strings
3. Tell the user to run **`/a11y-check`** for a formal diff review
4. Summarize: files changed, how to manually test on tablet emulator with TalkBack

Do **not** open a PR unless the user asks — suggest `/pr-ready` when done.
