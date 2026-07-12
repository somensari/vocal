# Feature request — Vocal (Product Managers)

Help a Product Manager turn a rough idea into a **GitHub Feature Request** that an Agent can pick up and implement **without another discovery round**.

**Do not write app code.** Interview → draft → (optional) file the issue.

## Step 0 — Context

Read before drafting:

- @AGENTS.md
- @CONTRIBUTING.md
- @docs/accessibility.md
- @.github/ISSUE_TEMPLATE/feature_request.yml

Treat the user's message after `/feature-request` as a partial brief. Extract anything already answered; **do not re-ask fields they already covered clearly**.

Project constraints to enforce in every FR:

- **Offline-first** — no network in v1 user flows unless they explicitly request a cloud feature
- **Calm board** — caregiver add/edit/delete stays in `feature/settings`, not the main board grid
- **AAC a11y** — TalkBack, ≥56dp board cells / ≥48dp controls, `contentDescription`s
- **Strings** — user-visible text in `strings.xml`, never hardcoded in Compose/ViewModels

## Step 1 — Required intake (block until complete)

If any **required** field below is missing or vague, ask **one batch** of clarifying questions (bullet list). Do **not** drip questions one-by-one. Do **not** file an issue until every required field has a concrete answer.

| Field | Required | What “good” looks like |
|-------|----------|------------------------|
| **Title** | Yes | Short, outcome-oriented (`[FR]` prefix optional; template adds it) |
| **Problem / user need** | Yes | Who (AAC user / caregiver / both) + pain today |
| **Proposed solution** | Yes | User-visible behavior, not code design |
| **Acceptance criteria** | Yes | Checkbox list of observable outcomes |
| **Out of scope** | Yes | Explicit non-goals (even if “none beyond v1”) |
| **Primary audience** | Yes | AAC end user / Caregiver / Both |
| **Primary surface** | Yes | Board / Settings / Both / Shared UI / Speech / Navigation / Other |
| **UI impact** | Yes | Yes (Compose) / No / Unsure |
| **Data / persistence** | Yes | None / Room / DataStore / local files / multiple / Unsure |
| **Offline / network** | Yes | Must remain offline **or** explicit cloud request |
| **Suggested modules** | Yes | Checkboxes or “Unknown — implementer should propose” |
| **Agent implementation brief** | Yes | Starting hint, constraints, likely files, manual test outline |
| **Size** | No | S / M / L / Unsure |
| **Related links** | No | Issues, screenshots, refs |

### Agent implementation brief — must include

1. **Starting hint** — e.g. `/feature-board` for board UI; settings work in `feature/settings`; domain logic in `core/domain`
2. **Hard constraints** — offline, calm board, strings.xml, SpeakPhraseUseCase / `AacCellButton` when relevant
3. **Likely files or areas** — or “unknown; explore `feature/board` / `feature/settings` first”
4. **Manual test outline** — tablet steps; TalkBack steps if UI impact is Yes

If the PM cannot name modules/files, propose a best-guess module map from the surface + data answers and ask them to confirm or correct it in the same batch.

## Step 2 — Duplicate check (light)

Before drafting, use GitHub MCP (`search_issues` on `somensari/vocal`, `is:issue`) or `gh issue list` for a quick search on the title keywords. If a close match exists, show it and ask whether to continue, refine that issue, or stop.

## Step 3 — Draft the issue body

Present a complete draft that mirrors @.github/ISSUE_TEMPLATE/feature_request.yml sections:

```markdown
## Problem / user need

…

## Proposed solution

…

## Acceptance criteria

- [ ] …
- [ ] …

## Out of scope

- …

## Primary audience

…

## Primary surface

…

## UI impact

…

## Data / persistence impact

…

## Offline / network

Must remain offline (no network)
<!-- or: Explicitly requests a cloud / network feature -->

## Suggested modules

- [x] `feature/board`
- [ ] …

## Agent implementation brief

**Starting hint:** …

**Constraints:**
- …

**Likely files / areas:** …

**Manual test:**
1. …
2. …

## Size (estimate)

…

## Related links

…
```

Also show a one-line **Title** proposal (prefer starting with `[FR]`).

Remind the PM: label will be **`enhancement` only**.

## Step 4 — Confirm, then file

1. Wait for explicit approval (“create”, “file it”, “looks good — open the issue”).
2. On approval, create the issue on **`somensari/vocal`** via GitHub MCP `issue_write` (method create) with:
   - `title` as approved
   - `body` as the drafted markdown
   - `labels`: `["enhancement"]`
3. If MCP is unavailable, fall back to:

   ```bash
   gh issue create --repo somensari/vocal --title "…" --label enhancement --body "$(cat <<'EOF'
   …
   EOF
   )"
   ```

4. Print the **issue URL** and number. Do **not** open a PR, branch, or start implementation unless the user explicitly asks.

If they only want a draft (no filing), stop after Step 3 and give copy-pasteable title + body.

## Step 5 — Handoff

End with:

- Issue link (if filed)
- Suggested next steps for implementers: open the issue → `/feature-board` (or settings/domain as appropriate) citing `Fixes #N` → `/a11y-check` if UI → `/pr-ready`
- Note that Cloud Agents / parallel agents should treat the **Agent implementation brief** + acceptance criteria as the source of truth
