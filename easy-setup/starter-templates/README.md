# Camera Integration Starter Templates

These starter templates provide ready-to-use project configurations with camera, ML Kit, and AR capabilities already integrated.

## How to Use

1. Download the template that matches your needs
2. Extract the zip file
3. Open the project in Android Studio or VSCode
4. Update the package name if needed
5. Run the app on a device that supports ARCore

## Available Templates

### Full Integration Template

`camera-ml-ar-full-template.zip` - Includes Camera2 API, ML Kit pose detection, and ARCore integration.

### Camera + ML Kit Template

`camera-ml-template.zip` - Includes Camera2 API and ML Kit pose detection.

### Camera + AR Template

`camera-ar-template.zip` - Includes Camera2 API and ARCore integration.

### Basic Camera Template

`camera-basic-template.zip` - Includes only Camera2 API implementation.

## Template Features

- Clean architecture with separation of concerns
- Proper thread and memory management
- Complete Gradle configuration to avoid dependency conflicts
- Sample activities to demonstrate each feature
- Comprehensive comments to explain key implementation details

## Customizing the Templates

1. **Change package name**:
   - Open `app/build.gradle`
   - Update the `applicationId`
   - Refactor package names in the source files

2. **Adjust camera resolution**:
   - Open `CameraViewModel.kt`
   - Modify the `targetResolution` value

3. **Change ML Kit model**:
   - Open `PoseDetectionProcessor.kt`
   - Switch between `AccuratePoseDetectorOptions` and `PoseDetectorOptions`

## Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer, or VSCode with Android extensions
- Android SDK 31 or higher (compileSdkVersion)
- Device with ARCore support (for AR templates)

## Need Help?

Refer to the main documentation in the repository for detailed explanations of each component.
