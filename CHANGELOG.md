## [1.2.1] - 2026-04-05

### Changed

- `CHANGELOG.md`, `app/build.gradle.kts`, `app/src/main/java/com/crumbsandsoul/billing/MainActivity.kt`, `build.gradle.kts`, `gradle.properties`, `scripts/rebuild-debug-clean.sh`, `version.properties`# Changelog

All notable changes to this project are recorded here. New entries may be prepended automatically on each commit when [git hooks](README.md#versioning--changelog) are installed.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.2.0] - 2026-04-05

### Added

- **Cancel invoice** (replaces delete): same invoice number; PDF is regenerated with a **CANCELLED** watermark so the sequence stays intact.
- **Restore** from Invoice History: cancelled invoices can be set **Active** again; PDF regenerates without the stamp.
- **`cancelled`** flag on invoice records, persisted in local history JSON and **full backup / restore** manifests; PDF regeneration on restore uses the stored flag when no PDF bytes exist in the archive.
- Invoice History **status filter**: All, Active, Cancelled; **Active / Cancelled** chip on each card.
- Sales exports: **Status** column (Active/Cancelled) on **invoice-wise detail** sections in PDF and Excel (Sheet 1).
- PDF helper option **`tightTrailing`** on wrapped text so the **Notes** block can sit closer to the footer rule without stacking errors.

### Changed

- **Aggregated** sales (month/customer summary, grand total, last-12-months preview) counts **active invoices only**; cancelled invoices are excluded from those rupee totals and counts.
- **Item Sales B2B/B2C** and **Customer Sales** Excel sheets aggregate **active invoices only**.
- **Cancelled watermark** on invoice PDFs: **one large**, centered, rotated **CANCELLED** (replaces tiled repeat).
- For **cancelled** invoices in history: payment status row is non-interactive; **payment reminder** is not offered.
- **Notes** area on invoice PDF: tighter spacing above the footer divider; heading and body use correct baseline spacing.

### Fixed

- **Notes** body overlapping the **Notes** heading after spacing changes (restored next-line baseline between title and body; tight trailing only after the last notes paragraph).

## [1.1.1] - 2026-03-28

### Changed

- Versioning and changelog automation (git hooks, `version.properties`, docs); backup scheduling and related receivers; assorted project wiring. See git history for touched paths.

## [1.1.0] - 2026-03-28

### Summary

- Baseline: previous Play metadata used `versionName` **1.1** and `versionCode` **2**; normalized here to **1.1.0** for semver-style patch bumps on every commit.
