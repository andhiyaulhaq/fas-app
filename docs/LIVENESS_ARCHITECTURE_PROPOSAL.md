# Elite Liveness Architecture Proposal

**Status**: Proposed  
**Objective**: Expand the single-page Silent Face Anti-Spoofing demo into a comprehensive, multi-module "Elite Anti-Spoofing" application.

## 1. Architectural Changes

To support multiple liveness demonstration modes without cluttering a single screen, we will refactor the app to use a hub-and-spoke navigation model.

### A. The Hub: `WelcomeActivity`
A new entry-point Activity (`WelcomeActivity`) will be created and registered as the `MAIN`/`LAUNCHER` intent in the AndroidManifest.
- **UI Design**: A premium, dark-mode dashboard featuring neon accents (matching the established hacker/elite aesthetic).
- **Features**: Large, tappable cards directing the user to specific liveness tests.

### B. The Spoke 1: `PassiveLivenessActivity` (Existing)
- The current `MainActivity` will be renamed to `PassiveLivenessActivity`.
- This ensures naming conventions accurately reflect its function (Silent/Passive Liveness using the proprietary C++ engine).
- The existing UI and functionality remain entirely intact, acting as the first option from the Welcome Hub.

### C. The Active Liveness Spokes (New)
- **Shared Architecture**: Instead of using the legacy `Camera` API, the active liveness tests will implement modern **AndroidX CameraX** for robust, lifecycle-aware camera streaming, coupled with **Google ML Kit Face Detection** to analyze the stream in real-time.
- **Spoke 2: `BlinkTestActivity`**: Tracks `isRightEyeOpenProbability` and `isLeftEyeOpenProbability`. Requires the user to blink to verify liveness.
- **Spoke 3: `SmileTestActivity`**: Tracks `isSmilingProbability`. Requires the user to smile (transitioning from < 0.3 to > 0.8) to verify liveness.
- **Spoke 4: `HeadTurnTestActivity`**: Tracks the Euler Y (Yaw) angle. Requires the user to turn their head left and right (e.g. angle > 30 and < -30) to verify 3D presence.

---

## 2. Dependency Additions

To support the Active Liveness test, the following dependencies will be added to the project (`libs.versions.toml` and `app/build.gradle.kts`):

*   **Google ML Kit Face Detection**: `com.google.mlkit:face-detection:16.1.6`
*   **AndroidX CameraX**:
    *   `camera-core`
    *   `camera-camera2`
    *   `camera-lifecycle`
    *   `camera-view`

---

## 3. UI/UX Specifications (Welcome Page)

The `activity_welcome.xml` layout will include:
*   **Header**: A sleek title like "LIVENESS DEMO" or "ELITE ANTI-SPOOF".
*   **Card 1**: "Passive Liveness Test"
    *   *Description*: "Silent Anti-Spoofing. Detects printed photos or screen recordings."
    *   *Action*: Launches `PassiveLivenessActivity`.
*   **Card 2**: "Active Liveness: Blink"
    *   *Description*: "Verifies 3D presence via eye movement tracking."
    *   *Action*: Launches `BlinkTestActivity`.
*   **Card 3**: "Active Liveness: Smile"
    *   *Description*: "Verifies liveness by detecting facial expression changes."
    *   *Action*: Launches `SmileTestActivity`.
*   **Card 4**: "Active Liveness: Head Turn"
    *   *Description*: "Verifies 3D geometry by tracking yaw orientation."
    *   *Action*: Launches `HeadTurnTestActivity`.

---

## 4. Phased Implementation Strategy

1.  **Preparation**: Add CameraX and ML Kit dependencies. Sync the project.
2.  **Hub Creation**: Create `WelcomeActivity` and update the AndroidManifest to make it the launcher.
3.  **Refactor**: Rename `MainActivity` and verify it can be launched successfully from the Welcome Page.
4.  **Active Tests Integration**: Build the `BlinkTestActivity`, `SmileTestActivity`, and `HeadTurnTestActivity`. Bind CameraX to a `PreviewView` and pipe the `ImageAnalysis` frames into ML Kit for challenge processing.
