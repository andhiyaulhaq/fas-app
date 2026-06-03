# Project Modernization Log

**Created At**: 2026-05-04 09:15 AM (GMT+7)  
**Last Modified**: 2026-05-04 11:25 AM (GMT+7)

This document outlines the changes made to modernize the Silent Face Anti-Spoofing project from its original legacy state to a version compatible with modern Android Studio, Gradle, and target SDKs.

## 1. Environment & Build System Comparison

| Feature | Original Project | Modernized Project | Rationale |
| :--- | :--- | :--- | :--- |
| **Android Gradle Plugin** | 3.6.3 | 7.2.2 | Required for compatibility with modern Android Studio versions. |
| **Gradle Wrapper** | 5.6.4 / 6.x | 7.3.3 | Matches AGP 7.2.2 and enables Java 11 support. |
| **Java JDK** | 8 | 11 | AGP 7.x+ requires Java 11 minimum to run. |
| **Kotlin** | 1.3.x | 1.7.10 | Required for compatibility with newer Gradle and Coroutines. |
| **AppCompat** | 1.1.0 | 1.6.1 | Resolves resource merging glitches and adds modern UI components. |
| **Core-KTX** | 1.3.0 | 1.7.0 | Matches Kotlin 1.7 requirement for stability. |
| **Target SDK** | 28 (Android 9) | 33 (Android 13) | Compliance with Google Play requirements and modern security. |
| **Repositories** | `jcenter()` (Deprecated) | `mavenCentral()` | JCenter is sunsetted; `mavenCentral()` is the modern standard. |

---

## 2. Key Technical Fixes

### A. Manifest Compliance (Android 12+)
*   **Problem**: In SDK 31+, activities with intent filters MUST explicitly declare `android:exported`.
*   **Fix**: Added `android:exported="true"` to `MainActivity` in the `app` module.
*   **Test Fix**: Upgraded `androidx.test` dependencies (Espresso 3.5.1, Test Core 1.5.0) to resolve similar issues in the automatically generated Test Manifests.

### B. Native Build (NDK & CMake)
*   **ABI Filtering**: Added explicit `abiFilters "armeabi-v7a", "arm64-v8a"` to the `engine` module to avoid warnings about invalid metadata (like `riscv64`) found in newer NDKs.
*   **Output Path Fix**: Commented out `CMAKE_LIBRARY_OUTPUT_DIRECTORY` in `CMakeLists.txt`. In modern AGP, Gradle expects to manage the library placement to correctly pack them into the APK.
*   **NDK Versioning**: Explicitly set `ndkVersion "26.1.10909125"` to ensure build reproducibility.

### C. Native Library Merging
*   **Problem**: `DuplicateRelativeFileException` occurred because OpenCV `.so` files were being found in both the `jniLibs` folder and the CMake build output.
*   **Fix**: Added `packagingOptions` with a `pickFirst 'lib/*/libopencv_*.so'` rule in `engine/build.gradle` to allow the build to proceed when duplicates are detected.

### D. Kotlin Signature Strictness
*   **Problem**: Upgrading to Kotlin 1.7 caused compilation errors in `MainActivity.kt` because `SurfaceHolder.Callback` and `Camera.PreviewCallback` methods used nullable types (`?`) that were no longer compatible with the platform interface.
*   **Fix**: Updated method signatures to use non-nullable types (e.g., `SurfaceHolder`, `ByteArray`) to match the expected overrides.

### E. Data Binding Syntax
*   **Problem**: The old `dataBinding { enabled = true }` syntax is deprecated.
*   **Fix**: Migrated to the modern block:
    ```gradle
    buildFeatures {
        dataBinding true
    }
    ```

### F. Responsive Camera Scaling & Immersive Mode
*   **Problem**: Fixed 640x480 resolution caused "stretched" faces, and the status bar caused a vertical shift in detection boxes.
*   **Fix**: 
    *   Implemented `getOptimalPreviewSize()` for dynamic resolution matching.
    *   Moved `SurfaceView` into a `FrameLayout` for "Center-Crop" support.
    *   Enabled **Immersive Sticky Mode** to hide system bars and resolve coordinate mapping offsets.

### G. Neon HUD UI Design
*   **Problem**: Legacy Cyan/Orange colors had low contrast and felt outdated.
*   **Fix**: 
    *   Adopted a **Neon Palette**: Neon Green (`#00FF00`) and Electric Red (`#FF3030`).
    *   Implemented **High-Contrast Badges**: Semi-transparent dark backgrounds for labels.
    *   Inversed Confidence Display: Neon text on a black background for maximum readability.

### H. DataBinding & Build Stability
*   **Problem**: `android:drawableTint` caused KAPT failures on API 21.
*   **Fix**: 
    *   Implemented a custom **`BindingAdapters.kt`** using `TextViewCompat`.
    *   Upgraded all Test dependencies to Android 12+ compatible versions (`Espresso 3.5.1`).

---

## 3. Usage Warning
The original project provided pre-built AI libraries only for **ARM** architectures.
*   **Physical Devices**: Works perfectly on ARM-based phones.
*   **Emulators**: Will likely crash with `UnsatisfiedLinkError` on x86/x86_64 emulators unless using an ARM-translation layer.
