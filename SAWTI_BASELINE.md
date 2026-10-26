# Sawti validated source baseline

The canonical baseline for continued Android development is `Sawti-Baseline.zip`, exported from the **successful Android build verification #70** (run 40327413334, commit 5390f31b).

Contains the Sawti 4.0 Android Gradle source with localized dialect settings and redesigned Family module: relationship vocabulary as the first tab, empty personal-family groups with add/edit/delete, photo recording/audio upload, and Room migration 3→4. The archive excludes signing keys, local.properties and generated build output.

Release verification: Gradle build and unit tests, data-safety checks, signed-release generation passed. Manual validation on a device remains necessary before installing over real user data.

For future updates, build from this baseline source rather than replaying the old MyWords bootstrap patches. Keep Android applicationId unchanged and use the existing release signing identity when publishing an update.
