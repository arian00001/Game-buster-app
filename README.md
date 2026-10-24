# Dev Style Premium Game Booster

Kotlin Android app with a locally bundled HTML/CSS premium interface, native installed-app discovery and launch bridge, Android settings shortcuts, and optional Android-reported usage statistics.

## Build using GitHub Actions
1. Upload the contents of this ZIP to the root of a GitHub repository.
2. Open Actions → **Build Dev Style APK** → **Run workflow**.
3. Download `dev-style-apks` from the completed run. Install `app-debug.apk` for testing.
4. `app-release-unsigned.apk` must be signed with your own release key before public distribution.

## SDK workflow fix
The workflow now discovers the actual `sdkmanager` executable under the configured Android SDK root rather than assuming one fixed path, prints the path used, accepts licenses, installs platform-tools, Android SDK platform 35 and Build Tools 35.0.0, and lists installed packages. This specifically improves diagnosis of the previous SDK-manager setup failure. It still needs to be run on GitHub Actions to verify the hosted runner environment.

## Real feature limits
Installed launchable apps are discovered from Android PackageManager. Game launching uses the app's actual launch intent. Usage totals require the user to enable Usage Access. This is not a privileged OEM game engine: ordinary apps cannot universally overclock CPU/GPU, force FPS, alter private game graphics settings, disable thermal protection, or guarantee lower ping. Cards animate inside Dev Style; no floating overlay is included.

App label: **Dev Style**  
Application ID: `com.devstyle.gamebooster`  
Minimum Android: 8.0 (API 26)  
Compile/target SDK: 35
