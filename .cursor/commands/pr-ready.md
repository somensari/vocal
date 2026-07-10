# PR ready — Vocal

Prepare the current branch for pull request. Package CI results, a diff summary, and a filled PR description.

## Step 1 — Gather context

1. Run `git status` and `git diff main...HEAD` (or `git diff` if no `main` divergence) to understand all changes.
2. If the user passed context after `/pr-ready` (e.g. issue number, "docs only"), use it in the summary and related-issues section.
3. Read @.github/pull_request_template.md — your output must follow this structure.
4. For UI-related changes, also read @docs/accessibility.md and @.cursor/rules/aac-accessibility.mdc. Consider running `/a11y-check` first for a focused review.

Identify:

- Which modules changed (`app`, `core/*`, `feature/*`)
- Whether UI, Gradle, domain logic, or docs were touched
- Any hardcoded strings, missing `contentDescription`, or touch-target risks

## Step 2 — Run CI

Run:

```bash
./scripts/ci.sh
```

- If CI **passes**, note success in the checklist.
- If CI **fails**, report the error summary, do **not** mark "Builds locally" as done, and list what must be fixed before opening the PR.
- Do not attempt broad refactors to fix CI unless the user asks — diagnose and report first.

## Step 3 — Summarize the diff

Before the PR template, provide a short **Diff summary** section:

- **Files changed** (count and key paths)
- **What changed** (1–3 sentences, focus on why)
- **Risk areas** (a11y, Gradle/KSP, Room schema, breaking API) — or "None identified"

## Step 4 — Fill the PR template

Output a complete PR description matching @.github/pull_request_template.md:

- **Summary** — concrete, not generic
- **Type of change** — check exactly one box with `[x]`
- **Checklist** — mark `[x]` only for items you verified; leave `[ ]` for anything not confirmed (explain gaps below the template)
- **UI changes** — if UI was touched: describe TalkBack/tablet testing **steps the author should run** (see Step 5). If no UI changes, write `N/A` and omit TalkBack steps from the main output.
- **Related issues** — `Fixes #N` or `None`

## Step 5 — TalkBack test plan (UI changes only)

If any Compose/UI Kotlin files changed under `feature/`, `core/ui/`, or `app/`, append a **Manual test plan** section with numbered steps, e.g.:

1. Build and install debug APK on a tablet emulator (10" recommended)
2. Enable TalkBack (Settings → Accessibility)
3. Traverse changed screens with swipe navigation — verify each interactive element is announced
4. For board cells: confirm `contentDescription` matches spoken phrase text
5. Verify tap targets feel comfortable at arm's length (56dp board cells, 48dp controls)
6. Test system font scaling if text/layout changed

Tailor steps to the actual screens affected.

## Step 6 — Pre-push reminders

End with a short **Before you open the PR** list:

- [ ] CI green locally (`./scripts/ci.sh`)
- [ ] No files under `local/` or secrets staged
- [ ] Strings in `strings.xml` (if UI changed)
- [ ] Manual TalkBack pass (if UI changed)
- [ ] Branch pushed to remote

Do **not** run `git push` or `gh pr create` unless the user explicitly asks.
