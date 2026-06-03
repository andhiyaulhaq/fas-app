# Running the Silent Face Anti-Spoofing Project

**Created At**: 2026-05-04 09:15 AM (GMT+7)  
**Last Modified**: 2026-05-04 11:40 AM (GMT+7)

This project is the Android deployment code for **Minivision Technology's** silent liveness detection algorithm. It uses NCNN for high-performance inference on mobile devices.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Arctic Fox or newer is recommended.
- **Android SDK**: Compile SDK 33 (Android 13) is used.
- **NDK & CMake**: Required for building the native `engine` module.
- **Android Device**: A physical device with a ARM processor (`armeabi-v7a` or `arm64-v8a`) is highly recommended. The project does not support x86/x86_64 architectures (common in older emulators).

## Step-by-Step Instructions

### 1. Open the Project
1. Open **Android Studio**.
2. Select **Open an Existing Project**.
3. Navigate to the root directory of this repository and select it.

### 2. Gradle Sync
1. Wait for Android Studio to initialize the project.
2. If prompted, click **Sync Project with Gradle Files**.
3. Ensure all dependencies are downloaded successfully. If you encounter issues with `jcenter()`, you may need to update the repositories in `build.gradle` to `mavenCentral()`.

### 3. Configure Native Build (Optional)
The project includes a C++ engine. If you want to modify the native code:
- Ensure the NDK path is correctly set in your local environment or `local.properties`.
- The `engine` module contains the native implementation using NCNN.

### 4. Build and Run Directly
If you want to run the app directly from Android Studio:
1. Connect your Android device via USB (ensure USB Debugging is enabled).
2. Select the `app` module in the run configurations dropdown.
3. Click the **Run** button (green play icon).
4. The application will be built and installed on your device.

### 5. Generate APK or AAB for Manual Installation
If you prefer to build the package and install it manually:

#### Using Android Studio UI:
1. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Once the build is complete, a notification will appear. Click **locate** to find the generated `.apk` file.
3. Alternatively, for a release version, go to **Build > Generate Signed Bundle / APK...** and follow the wizard using the provided `key.jks` file.

#### Using Terminal (Command Line):
Run the following command in the project root:
- **For Debug APK**: `./gradlew assembleDebug`
- **For Release APK**: `./gradlew assembleRelease`
- **For App Bundle (AAB)**: `./gradlew bundleRelease`

#### Locate the Output:
- **Debug APK**: `app/build/outputs/apk/debug/Mini-FAS_0.2_debug.apk`
- **Release APK**: `app/build/outputs/apk/release/Mini-FAS_0.2_release.apk`
- **App Bundle**: `app/build/outputs/bundle/release/app-release.aab`

#### Manual Installation:
1. **Copy to Phone**: Connect your phone to your computer and copy the `.apk` file to your phone's internal storage.
2. **Enable Unknown Sources**: On your phone, go to **Settings > Security** (or **Apps**) and enable **Install unknown apps** for your file manager.
3. **Install**: Use a file manager app on your phone to find the `.apk` file and tap it to install.

## Project Structure

- **`app`**: The main Android application module, containing the UI and camera logic.
- **`engine`**: The core detection engine, containing native C++ code and NCNN models.
- **`engine/src/main/assets/live`**: Contains the pre-trained anti-spoofing models (`.bin` and `.param`).

## Signing Configuration
The project comes with a pre-configured debug and release signing key (`key.jks`).
- **Key Alias**: `key`
- **Key Password**: `123456`
- **Store Password**: `123456`

## Troubleshooting

- **Camera Permission**: Ensure you grant camera permissions when the app starts.
- **Resolution**: The algorithm works best with standard camera resolutions. If the detection is slow, check the device's CPU performance.
- **NDK Version Mismatch**: If you see an error like "No version of NDK matched the requested version", ensure the `ndkVersion` in `engine/build.gradle` matches a version you have installed in Android Studio (SDK Manager > SDK Tools > NDK (Side by side)).

---
*For more information about the algorithm, visit [Minivision Technology](https://www.minivision.cn/).*
