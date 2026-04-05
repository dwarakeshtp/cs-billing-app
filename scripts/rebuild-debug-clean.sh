#!/usr/bin/env bash
# Fix: mergeProjectDexDebug "Directory does not exist ... mixed_scope_dex_archive/.../dexBuilderDebug/out"
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew --stop || true
# Drop project-local Gradle task state (not ~/.gradle); fixes IDE vs CLI stale UP-TO-DATE.
rm -rf app/build build .gradle
./gradlew :app:assembleDebug --no-build-cache --rerun-tasks
echo "Done. APK under app/build/outputs/apk/debug/"
