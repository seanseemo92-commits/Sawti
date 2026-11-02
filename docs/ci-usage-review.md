# Actions usage and resource cleanup

Branch: fix/prelaunch-cleanup-hint. No main, signing, version or publishing changes.

## Retired hint resources
Remove only sentence_hint and field_sentence from values/strings.xml. Existing lamp strings, sentence-strip functionality and persisted user data remain. This completes the requested default XML label cleanup; historical notes about pending cleanup predate this commit.

## Duplicate-run finding
android-ci.yml previously ran Debug on both push and PR. retry-validation.yml ran four separate matrix jobs on both push to the development branch and PR. Together with build.yml's original PR job, the observed head had 11 checks. Multiple Gradle environments repeated dependency/setup/compilation work and uploads.

## New policy
- build.yml remains unchanged: one automatic job on PRs targeting main, with baseline checks, unit tests, schema checks and reports. Its full path still runs on main pushes or explicit full_build dispatch.
- android-ci.yml is manual-only: a requested Debug APK, retained 7 days.
- retry-validation.yml is manual-only: tests, lintDebug, Debug and Release in one job/Gradle invocation with --continue. Failure remains a failed job. Reports/mapping retained 3 days; APK files are not uploaded by this workflow.
- Manual workflows cancel an older run in their own branch-specific concurrency group. Original PR concurrency remains unchanged.
- No old artifacts or running jobs are deleted/canceled by this commit itself. Retention changes apply to new uploads, and cannot refund already consumed compute.

Manual dispatch availability depends on the workflow existing on the default branch. Existing build.yml can be dispatched for a selected branch with full_build=true when the new workflow entries are not available in the UI. Do not merge merely to expose manual buttons.

Full validation must still be performed and verified on the final release commit before signing/approval. Saving quota is not evidence that checks passed. Exact minutes/storage savings and account balances have not been measured. Existing build.yml retention policies are unchanged.
