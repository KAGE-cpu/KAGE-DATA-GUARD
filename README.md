# KAGE DATA GUARD

Native Android Java app for offline-first mobile-data monitoring.

## Current prototype
- Configurable daily MB budget
- Usage Access setup shortcut
- Device-wide mobile-data usage query
- Foreground monitoring service with a 15-minute check interval
- Notification when the configured budget is reached
- GitHub Actions debug APK build

## Important Android limitation
A normal third-party Android app cannot directly disable mobile data on modern Android. KAGE DATA GUARD alerts the user and opens Android data settings. For a hard cutoff, configure Android's built-in mobile-data warning/limit as an independent safeguard.

Usage statistics may be delayed or aggregated. Huawei battery management may stop background work; configure KAGE DATA GUARD to run in the background and test after installation.

## Build
Requires JDK 17, Gradle 8.9 and Android SDK platform 35.
Run: `gradle assembleDebug`
Artifact: `app/build/outputs/apk/debug/app-debug.apk`

This is an early prototype, not a finished or device-verified release. Validate usage reporting, dual-SIM behavior, Huawei background restrictions, and notification behavior on the target phone before relying on it.
