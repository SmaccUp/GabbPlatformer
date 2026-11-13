# Pocket Platformer — Java Android starter project

A native Android 2D platformer written in Java. It uses Android's `SurfaceView` and `Canvas` APIs; it is not an HTML/CSS or browser game.

## Included
- Landscape game screen
- Touchscreen left, right, and jump controls
- Gravity, jumping, platform collision, camera scrolling
- Coins, spikes, score, win/lose states, and restart
- No external game-engine dependencies

## Build with Android Studio
1. Install Android Studio on a Windows, Mac, or Linux computer.
2. Install Android SDK Platform 35 and Android SDK Build-Tools when prompted.
3. Open this folder (`GabbPlatformer`) in Android Studio.
4. Allow Gradle to sync. Use JDK 17 if Android Studio asks.
5. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. The debug APK will be created at:
   `app/build/outputs/apk/debug/app-debug.apk`

You can also build from a terminal with a compatible Gradle 8.9 installation:
`gradle assembleDebug`

## Installing on a Gabb Phone 4
Building an APK does not guarantee that the Gabb Phone 4 will permit installing it. Gabb controls which apps can be installed and may block sideloading or unknown-source installation. Only try installation through a method allowed by Gabb and the phone's parent/guardian settings. Do not bypass device management or security controls.

## Controls
- Left button: move left
- Right button: move right
- JUMP: jump (hold to keep the jump input active; the character jumps when grounded)
- Tap after winning or losing to restart

## Project details
- Package: `com.example.gabbplatformer`
- Minimum Android version: Android 6.0 (API 23)
- Target SDK: 28 (kept modest for compatibility; Android Studio may recommend a newer target for publishing)

This is a starter game project intended to be customized. It has not been tested on Gabb Phone 4 hardware.
