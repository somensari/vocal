# Contributing to Vocal

Thank you for helping make AAC tools more accessible. This project is intended to be welcoming to therapists, parents, developers, and designers.

## Code of conduct

Be respectful and patient. Many contributors and users live with disabilities or support people who do. Harassment is not tolerated.

## How to contribute

1. Check existing issues or open a [Feature request](.github/ISSUE_TEMPLATE/feature_request.yml) (or use `/feature-request` in Cursor) to discuss larger changes.
2. Fork the repository and create a feature branch.
3. Keep changes focused and small when possible.
4. Match existing architecture: MVVM + Repository, multi-module layout.
5. Put user-visible text in `strings.xml`, not hardcoded in Compose.
6. Test on a tablet emulator or device with TalkBack when UI changes.

## Development setup

See [README.md](README.md).

Useful commands:

```bash
./scripts/ci.sh              # unit tests, lint, debug APK (matches GitHub Actions)
./gradlew :app:assembleDebug
./gradlew test
```

Pull requests to `main` run [Android CI](.github/workflows/android-ci.yml) automatically.

## Pull request checklist

- [ ] Builds locally
- [ ] Accessibility considered (touch targets, content descriptions)
- [ ] No secrets or personal data committed
- [ ] Strings externalized to resources
- [ ] README or docs updated if behavior changed

## Accessibility and AAC guidelines

When changing UI or speech behavior, read `docs/accessibility.md` and `.cursor/rules/aac-accessibility.mdc`.

## Workflows

Vocal defines repeatable workflows in `.cursor/commands/` for **Cursor** users. The same steps apply in **Android Studio** or any editor — run them manually using the checklist below.

### Cursor slash commands

Type `/` in Cursor Agent chat:

| Command | When to use |
|---------|-------------|
| `/onboard` | New to the repo — architecture, modules, CI, accessibility overview |
| `/feature-request` | Product Managers — interview, draft, and file a GitHub Feature Request |
| `/feature-board` | Implement or extend the communication board (`feature/board`) |
| `/a11y-check` | Before a PR that touches UI — review diff for AAC accessibility |
| `/pr-ready` | Before opening a PR — run CI, summarize diff, draft PR description |

Command files live in [`.cursor/commands/`](.cursor/commands/) and are committed to the repo. See also [AGENTS.md](AGENTS.md) for agent and Cloud VM notes.

### Manual checklist (all contributors)

Use this if you do not use Cursor, or as a reference for what each command does:

**Starting out (equivalent to `/onboard`)**

1. Read [README.md](README.md), this file, and [AGENTS.md](AGENTS.md) (human quickstart section).
2. Skim module layout: `app`, `core/domain`, `core/data`, `core/ui`, `feature/board`, `feature/settings`.
3. Run `./scripts/ci.sh` once to confirm your environment.

**Feature requests (equivalent to `/feature-request`)**

1. Prefer the [Feature request](.github/ISSUE_TEMPLATE/feature_request.yml) GitHub form, or draft in Cursor with `/feature-request`.
2. Fill every required section (problem, solution, acceptance criteria, out of scope, audience, surface, UI/data/offline impact, agent brief).
3. Include an **Agent implementation brief** so an implementer can start from the issue alone.
4. Label is `enhancement`. Do not start coding until the request is filed and prioritized.

**Board feature work (equivalent to `/feature-board`)**

1. Plan which modules change (UI → `feature/board`; logic → `core/domain`; persistence → `core/data`).
2. Keep the board screen calm — caregiver edit controls stay in `feature/settings`.
3. Use `AacCellButton`, `strings.xml`, and `SpeakPhraseUseCase` — no direct TTS in UI.
4. Run `./scripts/ci.sh` when done.

**Accessibility review (equivalent to `/a11y-check`)**

1. Read [docs/accessibility.md](docs/accessibility.md).
2. For each changed UI file, verify:
   - Board cells ≥ 56dp; other controls ≥ 48dp
   - `contentDescription` on interactive elements (board cells: spoken phrase text)
   - User-visible strings in `strings.xml`, not hardcoded in Compose
3. On a tablet emulator: enable TalkBack, traverse changed screens, confirm announcements and tap targets.

**Before opening a PR (equivalent to `/pr-ready`)**

1. Run `./scripts/ci.sh` — fix any failures.
2. Review your branch diff (`git diff main...HEAD`).
3. Fill out the [pull request template](.github/pull_request_template.md) honestly — check boxes only for items you verified.
4. If UI changed: document TalkBack / tablet testing in the PR description.
5. Confirm no secrets or `local/` files are staged; push and open the PR.

## Questions

Open a GitHub discussion or issue if you are unsure whether a change fits the project direction.
