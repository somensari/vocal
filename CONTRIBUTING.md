# Contributing to Vocal

Thank you for helping make AAC tools more accessible. This project is intended to be welcoming to therapists, parents, developers, and designers.

## Code of conduct

Be respectful and patient. Many contributors and users live with disabilities or support people who do. Harassment is not tolerated.

## How to contribute

1. Check existing issues or open one to discuss larger changes.
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

## Questions

Open a GitHub discussion or issue if you are unsure whether a change fits the project direction.
