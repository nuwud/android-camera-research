# Android Camera Research

A comprehensive research repository on Android camera development, with focus on skeletal tracking and augmented reality.

## Overview

This repository documents research on various approaches to implementing camera functionality in Android applications without relying on Expo, focusing on native Android development using Camera2 API, ML Kit for pose detection, and ARCore for augmented reality.

## Detailed Documentation

This repository contains several in-depth documents on different aspects of Android camera implementation:

1. [Camera2 API](camera2-api.md) - Low-level camera access with full control over camera hardware
2. [ML Kit Pose Detection](ml-kit-pose-detection.md) - Using Google's ML Kit for skeletal tracking
3. [ARCore for Augmented Reality](arcore-augmented-reality.md) - Building AR experiences with Google's ARCore
4. [SceneView for 3D and AR](sceneview-3d-ar.md) - Modern library for simplified 3D and AR development
5. [Clean Architecture Guide](clean-architecture-guide.md) - Building conflict-free Android apps with camera features
6. [React Native Vision Camera Analysis](react-native-vision-camera-analysis.md) - Evaluation of Vision Camera library for React Native integration

## Key Technologies

### 1. Camera2 API

The Camera2 API provides fine-grained control over the camera hardware in Android devices. It offers features such as:

- Manual control over camera settings (exposure, focus, etc.)
- Access to raw camera data
- Support for multiple cameras
- Better performance for real-time processing

**Resources:**
- [Official Camera2 API Guide](https://developer.android.com/guide/topics/media/camera)
- [Camera2 Samples Repository](https://github.com/android/camera-samples)

### 2. ML Kit for Pose Detection

Google ML Kit provides an easy-to-use API for pose detection that can track up to 33 body landmarks in real-time. Key features include:

- Full-body pose detection (including face, torso, arms, legs)
- Support for both static images and video streams
- Options for accuracy vs. performance tradeoffs
- Classification capabilities to identify specific poses (e.g., squats, push-ups)

**Resources:**
- [ML Kit Pose Detection Documentation](https://developers.google.com/ml-kit/vision/pose-detection)
- [ML Kit Samples Repository](https://github.com/googlesamples/mlkit)
- [PoseDetection-MLKit Example](https://github.com/icanerdogan/PoseDetection-MLKit)
- [Pose Estimation Android App](https://github.com/nevinbaiju/pose_estimation_android_app)

### 3. ARCore for Augmented Reality

ARCore is Google's platform for building augmented reality experiences on Android. It provides:

- Motion tracking: Understanding the phone's position relative to the world
- Environmental understanding: Detecting surfaces and their size/location
- Light estimation: Estimating current lighting conditions for realistic rendering

**Resources:**
- [ARCore Developer Guide](https://developers.google.com/ar/develop/java/quickstart)
- [ARCore Android SDK](https://github.com/google-ar/arcore-android-sdk)
- [Awesome ARCore Collection](https://github.com/olucurious/Awesome-ARCore)

### 4. SceneView for 3D and AR

SceneView is a modern library that simplifies working with 3D content and AR in Android. It is built on top of Google's Filament rendering engine and ARCore, providing:

- Both Jetpack Compose and traditional View implementations
- Simplified 3D model loading and rendering
- Easy integration with ARCore for AR experiences
- Physics and collision detection

**Resources:**
- [SceneView Android](https://github.com/SceneView/sceneview-android)
- [SceneView Documentation](https://sceneview.github.io/)
- [AR Model Viewer Sample](https://github.com/SceneView/sceneview-android/tree/main/samples/ar-model-viewer)

### 5. React Native Vision Camera

React Native Vision Camera is a powerful library for creating camera applications in React Native with native performance:

- High-performance native camera implementation with React Native UI
- Frame processor API for ML and AR integration
- Direct access to Camera2 API features
- Optimized frame processing with worklets

**Resources:**
- [React Native Vision Camera](https://github.com/mrousavy/react-native-vision-camera)
- [Vision Camera Documentation](https://react-native-vision-camera.com/)
- [Frame Processors Guide](https://react-native-vision-camera.com/docs/guides/frame-processors)

## Implementation Approaches

### Approach 1: Native Android with Clean Architecture

This approach uses pure native Android development with a clean architecture pattern:

1. Implement camera functionality using Camera2 API or CameraX
2. Process frames with ML Kit for pose detection
3. Integrate ARCore for augmented reality features
4. Use SceneView for simplified 3D and AR rendering
5. Organize code with clean architecture principles

**Advantages:**
- Maximum performance and control
- Direct access to all native APIs
- Clean, modular code structure
- Avoids cross-platform complications

### Approach 2: React Native with Vision Camera

This approach leverages React Native for UI while maintaining native performance for camera operations:

1. Use React Native for application UI
2. Implement camera functionality with Vision Camera
3. Create custom frame processors for ML Kit and ARCore integration
4. Bridge between React Native and native components

**Advantages:**
- Cross-platform UI development
- Near-native camera performance
- Faster UI iteration
- Access to React Native ecosystem

## Avoiding Common Issues

Our research has identified several key practices to avoid common issues:

1. **Dependency Management**: Centralize dependency versions and use version catalogs
2. **Gradle Configuration**: Properly configure Gradle with consistent SDK versions
3. **Clean Architecture**: Separate business logic from framework dependencies
4. **Module Organization**: Structure your app into feature modules with clear boundaries
5. **Camera Lifecycle Management**: Properly handle camera lifecycle events

For detailed guidelines on building conflict-free Android applications, see our [Clean Architecture Guide](clean-architecture-guide.md).

## Conclusion

Android offers multiple robust approaches for implementing camera-based applications with skeletal tracking and AR capabilities. The choice between a pure native approach versus a React Native hybrid depends on specific requirements such as device compatibility, AR needs, performance considerations, and development team expertise.

Both approaches are well-documented and have active communities, making them viable options for professional Android development in 2025.
