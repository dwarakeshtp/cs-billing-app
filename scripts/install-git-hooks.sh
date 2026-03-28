#!/usr/bin/env bash
# Point this repo at .githooks so pre-commit bumps version + CHANGELOG on every commit.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit scripts/bump-version-changelog.sh 2>/dev/null || true
echo "Git hooks path set to .githooks (pre-commit: version + CHANGELOG)."
echo "To skip once: SKIP_CHANGELOG_VERSION=1 git commit ..."
