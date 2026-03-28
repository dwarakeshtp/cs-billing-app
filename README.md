# cs-billing-app
Crumbs &amp; Soul Android Billing App

## Versioning &amp; changelog

- **App version** lives in the repo root [`version.properties`](version.properties) (`versionCode`, `versionName`). [`app/build.gradle.kts`](app/build.gradle.kts) reads these values for every build.
- **Changelog** is [`CHANGELOG.md`](CHANGELOG.md), kept in [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) style.

### Per-commit bump (git hook)

After cloning, run once:

```bash
./scripts/install-git-hooks.sh
```

On each `git commit`, the **pre-commit** hook increments `versionCode`, bumps the **patch** segment of `versionName` (e.g. `1.1.0` → `1.1.1`), prepends a dated section to `CHANGELOG.md` listing **staged** changed paths, and re-stages `version.properties` and `CHANGELOG.md` so they are part of the same commit.

To make a commit **without** bumping (emergency / merge tweak):

```bash
SKIP_CHANGELOG_VERSION=1 git commit ...
```

If you do not install hooks, update `version.properties` and `CHANGELOG.md` yourself before releases.
