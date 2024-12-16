# Sage: Wisdom to Manage your Financials

This README file is specifically for the Sage application. Below are instructions on how to run and test the app on an emulator or Android device as well as some of the app's features.

For the full app report including code review, features, and product specification sheet, read the `Sage_Product_Report.pdf` file.

## Contents
- [Download the APK](#Download-the-APK)
- [Running the App](#Running-the-App)
- [App Trailer](#App-Trailer)
- [Features](#Features)
- [Technology Used](#Technology-Used)
- [TODO List](#To-Do-List-for-App)
- [Version History](#Version-History)

## Download the APK

You can  download the APK from:
- **GitHub Releases**: [latest release](https://github.com/cecoulombe/Sage_App/releases)
OR
- Directly from this repository: Navigate to the [`apk/`](./apk/) folder and download the `sage_app.apk` file.

## Running the App
Below are the instructions for running the app on an Android emulator or physical device. Choose the method that works best for you.

### 1. On an Emulator
1. Download and install **Android Studio**: [Android Studio Download](https://developer.android.com/studio).
2. Set up an **Android Virtual Device (AVD)** using the Android Emulator in Android Studio.
   - Open Android Studio > Tools > Device Manager > Create a Virtual Device.
3. Drag and drop the downloaded APK (`sage_app.apk`) into the running emulator.
4. The app will install and launch.

### 2. On a Physical Android Device (not tested)
1. Enable **Developer Options** on your Android device:
   - Go to Settings > About Phone > Tap "Build Number" 7 times to enable Developer Mode.
2. Enable **Install Unknown Apps** in Settings > Security.
3. Transfer the APK file to your device (via USB, Google Drive, or email).
4. Open the APK file on your device and allow the installation.
5. Launch the app from your app drawer.

## App Trailer

Watch the app in action here:

If on desktop:

[![App Trailer - Best for Desktop viewing](https://img.youtube.com/vi/xRc2w368BJk/0.jpg)](https://youtu.be/xRc2w368BJk)


If on mobile device (it is best if you lock your rotation and watch the video in landscape):

[![App Trailer - Best for Mobile viewing](https://img.youtube.com/vi/I-oB61kQq0U/0.jpg)](https://youtu.be/I-oB61kQq0U)

## Features

For a full list of features, see the `Sage_Product_Report.pdf` file.

There are numerous features of Sage that are worth mentioning:
- Google Firebase sign on and data storage
- Calming and comfortable environment
- Reactive user interface and immediate data sync with Firebase
- Graphs to highlight financial wellbeing and goal progress
- Currency exchange calculator with up-to-date exchange rates

## Technology Used
- **Language**: Java/Kotlin
- **Build Tool**: Gradle
- **Frameworks**: Android SDK

## To-Do List for App

- [ ] Publish the application to the Google Play Store.  
    - App is currently in closed testing due to limited access to testers and physical Android devices.

## Version History

- **v1.0**: Initial publication of the completed application to GitHub. App not yet available on Google Play Store due to closed testing limitations (not enough testers and no Android devices).
