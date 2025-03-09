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

## Implementation Approaches

### Approach 1: Camera2 API with ML Kit

This approach involves using the Camera2 API to capture frames and ML Kit to process them for pose detection:

1. Set up a camera preview using Camera2 API
2. Process frames using ML Kit's pose detection API
3. Visualize detected poses by drawing skeletal landmarks
4. Implement custom logic based on detected poses

**Advantages:**
- Fine-grained control over camera
- Efficient processing pipeline
- No dependency on AR features (works on more devices)

### Approach 2: ARCore with SceneView

This approach leverages ARCore and SceneView for more advanced AR experiences:

1. Set up an AR scene using SceneView
2. Use ARCore for plane detection and tracking
3. Integrate ML Kit for pose detection if needed
4. Place virtual content in the real world based on detected poses or planes

**Advantages:**
- Rich AR capabilities (plane detection, light estimation, etc.)
- Simplified 3D content handling
- Modern API with Jetpack Compose support

## Sample Projects

The following samples demonstrate different aspects of camera usage:

1. [CameraX with ML Kit](https://github.com/android/camera-samples/tree/main/CameraX-MLKit) - Basic QR code scanning with CameraX and ML Kit
2. [ML Kit Vision Quickstart](https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart) - Comprehensive examples of ML Kit vision features including pose detection
3. [AR Model Viewer](https://github.com/SceneView/sceneview-android/tree/main/samples/ar-model-viewer) - ARCore and SceneView for AR experiences

## Conclusion

Android offers multiple robust approaches for implementing camera-based applications with skeletal tracking and AR capabilities. The choice between Camera2 API with ML Kit versus ARCore with SceneView depends on specific requirements such as device compatibility, AR needs, and performance considerations.

For applications requiring only pose detection without AR, the Camera2 API with ML Kit provides a more lightweight solution. For full AR experiences with 3D content, ARCore with SceneView offers a more comprehensive framework.

Both approaches are well-documented and have active communities, making them viable options for professional Android development in 2025.
