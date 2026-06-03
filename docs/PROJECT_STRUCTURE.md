# Project Structure

**Created At**: 2026-05-04 09:15 AM (GMT+7)  
**Last Modified**: 2026-05-04 11:25 AM (GMT+7)

This document outlines the directory structure and the purpose of key components in the **Silent Face Anti-Spoofing** Android project.

## Directory Overview

```text
Silent-Face-Anti-Spoofing-APK/
├── app/                # Main Android application module
├── engine/             # Core detection and liveness engine (Library module)
├── gradle/             # Gradle wrapper and configuration
├── docs/               # Documentation files
├── build.gradle        # Root build configuration
├── settings.gradle     # Module definitions
└── key.jks             # Signing key for APK generation
```

---

## 1. `app` Module (Application Layer)

The `app` module contains the user interface and coordinates the camera stream with the detection engine.

### Key Files:
- **`MainActivity.kt`**: The entry point of the app, coordinating camera preview and engine calls.
- **`SetThresholdDialogFragment.kt`**: Handles the threshold settings dialog.
- **`BindingAdapters.kt`**: Custom DataBinding logic for backward-compatible UI attributes (e.g., icon tinting).
- **`EngineWrapper.kt`**: Acts as a wrapper for the `engine` module, providing a simplified interface for the UI to interact with the detection logic.
- **`CoverView.kt` & `RectView.kt`**: Custom Android views used to draw overlays on top of the camera preview (e.g., face bounding boxes and status indicators).
- **`DetectionResult.kt`**: Data class representing the result of a single detection pass (score, liveness status, etc.).

---

## 2. `engine` Module (Logic & Native Layer)

The `engine` module is an Android Library that encapsulates the face detection and anti-spoofing algorithm. It uses NCNN for inference and includes native C++ code.

### Java/Kotlin Source (`com.mv.engine`):
- **`FaceDetector.kt`**: Handles the initialization and execution of the face detection model.
- **`Live.kt`**: The core liveness detection component. It processes the detected face and returns a liveness score.
- **`FaceBox.kt`**: Data model for face coordinates and attributes.
- **`ModelConfig.kt`**: Manages the configuration and loading of the model parameters.

### Native Source (`src/main/cpp`):
- **`CMakeLists.txt`**: Build configuration for the C++ code.
- **`ncnn-android-lib`**: Contains the NCNN library dependencies.
- **Native Implementation**: C++ files that implement the algorithm logic using the NCNN framework for high performance.

### Assets (`src/main/assets`):
- **`detection/`**: Contains the face detection model files (`.bin` and `.param`).
- **`live/`**: Contains the anti-spoofing (liveness) model files.

---

## 3. Configuration & Build Files

- **`build.gradle` (Root)**: Defines project-wide dependencies and repository settings.
- **`app/build.gradle`**: Specific configuration for the application, including the signing config and dependencies on the `engine` module.
- **`engine/build.gradle`**: Configuration for the library module, including the `externalNativeBuild` (CMake) setup.
- **`local.properties`**: (Auto-generated) Contains paths to your local Android SDK and NDK.

---

## Data Flow Summary

1. **Camera Stream**: `MainActivity` captures frames from the camera.
2. **Preprocessing**: Frames are passed to `EngineWrapper`.
3. **Face Detection**: The `engine` (via native code) detects faces in the frame.
4. **Liveness Check**: For each detected face, the liveness model evaluates if it is a real person or a spoof (e.g., a photo or screen).
5. **UI Update**: `MainActivity` receives the results and updates `RectView` to show the bounding box and liveness status to the user.
