# CI SDK setup fix

The workflow no longer calls `sdkmanager` manually and does not request the obsolete SDK package `tools`. It delegates package installation to `android-actions/setup-android@v3`, requesting only:

- `platform-tools`
- `platforms;android-35`
- `build-tools;35.0.0`

It then verifies those directories exist before starting Gradle. Gradle 8.9 is provisioned explicitly to match Android Gradle Plugin 8.7.3. The workflow will still need a real GitHub Actions run to verify hosted-runner behavior; this local ZIP edit cannot guarantee the remote job succeeds.
