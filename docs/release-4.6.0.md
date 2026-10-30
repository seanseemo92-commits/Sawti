# صوتي | Sawti 4.6.0

Signed production APK and AAB; application ID `com.sawti`, versionCode `11`.

Includes welcome-button parity and copyright geometry fixes, app-aware touch feedback, validated restore with first-start rollback, shared-media protection, consistent backup snapshots including legacy recordings, and canonical editing with preserved personal pronunciation across nine dialects.

Room schema 6 uses additive migrations. Personal text, photos, recordings and favorites are preserved. The signed build runs unit tests and Android lint, validates APK metadata and its v2 signature, and compares APK/AAB publisher certificates to the previously published v4.4 APK. SHA256 checksums and source commit information are attached.

Update compatibility requires the same package and publisher certificate. Debug-signed builds and older `com.mywords` installations cannot be updated in place with this APK. Keep existing data and backups; do not uninstall an app merely to bypass a signature error.

Physical-device update, restore, accessibility and speech playback acceptance remains necessary. Automated checks do not establish that the application is free of every vulnerability. Historical already-lost custom text cannot be recovered by migration. The repository remains private.

## Known follow-up work

This release integrates the completed repair stages; it does not close every finding in the earlier audit. Remaining work includes training-session cancellation when leaving the screen, asynchronous recording preparation and cancellation indicators, preserving favorites/progress during historical sample cleanup, synchronizing content-seed versions with restored databases, recovery UI for storage errors, category-order editing, and complete reduced-motion behavior. Search/favorites/onboarding cleanup and a full resolved-dependency/history security scan also remain separate follow-up work.
