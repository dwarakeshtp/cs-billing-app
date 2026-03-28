#!/usr/bin/env bash
# Bump versionCode / versionName in version.properties and prepend a CHANGELOG section
# from staged files. Intended for .githooks/pre-commit. Compatible with Bash 3.2 (macOS).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

PROPS="$ROOT/version.properties"
CHANGELOG="$ROOT/CHANGELOG.md"

if [[ ! -f "$PROPS" ]]; then
  echo "bump-version-changelog: missing version.properties" >&2
  exit 1
fi

code=$(grep '^versionCode=' "$PROPS" | head -1 | cut -d= -f2-)
name=$(grep '^versionName=' "$PROPS" | head -1 | cut -d= -f2-)

if [[ -z "$code" || -z "$name" ]]; then
  echo "bump-version-changelog: could not read versionCode/versionName" >&2
  exit 1
fi

new_code=$((code + 1))

# Bump last numeric segment of versionName (1.2 -> 1.2.1, 1.2.3 -> 1.2.4)
if [[ "$name" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  new_name="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.$((${BASH_REMATCH[3]} + 1))"
elif [[ "$name" =~ ^([0-9]+)\.([0-9]+)$ ]]; then
  new_name="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.1"
else
  new_name="${name}.1"
fi

{
  echo "# Bumped automatically by .githooks/pre-commit (edit manually only if you skip hooks)."
  echo "versionCode=$new_code"
  echo "versionName=$new_name"
} >"$PROPS.tmp"
mv "$PROPS.tmp" "$PROPS"

DATE="$(date +%Y-%m-%d)"

FILES=()
while IFS= read -r line; do
  [[ -n "$line" ]] && FILES+=("$line")
done < <(git diff --cached --name-only --diff-filter=ACMRT 2>/dev/null || true)

LIST=""
max=25
n=${#FILES[@]}
if ((n == 0)); then
  LIST="_No staged file paths (version bump only)._"
else
  i=0
  for f in "${FILES[@]}"; do
    if ((i >= max)); then
      more=$((n - max))
      LIST="${LIST}, … and ${more} more"
      break
    fi
    if ((i > 0)); then
      LIST="${LIST}, "
    fi
    LIST="${LIST}\`${f}\`"
    i=$((i + 1))
  done
fi

ENTRY=$(printf '## [%s] - %s\n\n### Changed\n\n- %s\n\n' "$new_name" "$DATE" "$LIST")

tmp="$(mktemp)"
printf '%s' "$ENTRY" >"$tmp"
if [[ -f "$CHANGELOG" ]]; then
  cat "$CHANGELOG" >>"$tmp"
fi
mv "$tmp" "$CHANGELOG"

git add "$PROPS" "$CHANGELOG"

echo "bump-version-changelog: versionCode $code → $new_code, versionName $name → $new_name"
