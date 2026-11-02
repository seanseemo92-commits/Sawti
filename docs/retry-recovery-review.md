# Category loading and retry recovery

Branch: fix/prelaunch-cleanup-hint. Main, version and signing configuration are unchanged.

## Changes
- A single ViewModel-owned controller seeds vocabulary and observes categories.
- Initial seed/query errors have independent loading state and retry UI.
- Retry waits for the previous observer to stop before starting another load.
- Rapid taps cannot start parallel loading attempts.
- Existing categories survive later failures; an update-retry action remains visible.
- Splash exits on success or failure and does not return over a subsequent retry.
- Backup navigation guards still gate retry UI and click handling.
- General editor errors remain separate from category loading errors.

## Automated tests added
Success; failed seed then success; failed query then resubscription; rapid taps; repeated failure; empty list; empty completed flow; retaining categories after a later query error.

Status: added, NOT executed by the assistant. Results must be checked in GitHub Actions for the resulting commit.

## CI validation
Separate matrix jobs run testDebugUnitTest, lintDebug, assembleDebug and assembleRelease; reports, APKs and R8 mapping outputs are uploaded when present. Release is validation-only and may be unsigned. It is NOT an approved production artifact.

## Outstanding manual validation
Light/dark error UI, large font, screen rotation, splash transition during retry, active backup/restore, low-end device performance, and preservation of user content. Full phase 6 remains open. Phase 7 is not complete until all results are verified and the approved signing certificate is available and checked. No merge without explicit approval.
