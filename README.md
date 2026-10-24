# Dev Style Game Booster — Premium Android Project

Native Kotlin Android application with a white minimal UI, cyan accents, user-supplied launcher logo, animated installed-app cards, app search, game launching, Android settings shortcuts, and optional usage analytics.

## Included working features

- Discover launchable apps available to Android's PackageManager.
- Search the detected app list.
- Launch a selected app using its launch intent, with error handling.
- Slide-in card animation in the game list.
- Open Android Do Not Disturb, usage access, wireless, battery, display and app settings.
- Read Android-reported daily app usage only after the user enables Usage Access.
- No account, network backend, analytics SDK, or remote upload.

## Capability boundaries

This is not an OEM-privileged game engine. Ordinary Android apps cannot universally overclock CPU/GPU, force FPS, modify another game's private graphics settings, bypass thermal controls, or guarantee lower ping. This project does not show fake boost toggles or fabricated statistics.

The card slide animation occurs **inside Dev Style**. It does not create a floating side panel over a running game. A floating overlay would require special overlay permission and device-specific testing and is not included in this version.

## Build APK with GitHub Actions

1. Upload the project contents (not the enclosing folder) to the root of a new GitHub repository.
2. Open **Actions** and enable workflows if GitHub prompts you.
3. Select **Build Dev Style APK** and click **Run workflow**, or push to `main` / `master`.
4. Open the completed workflow run. Under **Artifacts**, download `dev-style-apks`.
5. Extract it:
   - `app-debug.apk` is for local testing.
   - `app-release-unsigned.apk` is not ready for public distribution until signed with your private release key.

The GitHub workflow is configured but has not been run from this ZIP. A successful CI run is required to confirm that the current source compiles in the Android build environment.

## Local build requirements

- JDK 17
- Android SDK platform 35
- Gradle 8.9

Run:

```bash
gradle assembleDebug
```

## App details

- App label: **Dev Style**
- Package: `com.devstyle.gamebooster`
- Minimum Android: 8.0 (API 26)
- Target / compile SDK: 35
- Kotlin JVM target: 17

## Permissions

`QUERY_ALL_PACKAGES` is used to discover installed launchable apps. `PACKAGE_USAGE_STATS` requires the user to grant Usage Access manually in Android Settings. The app does not silently grant special access.
