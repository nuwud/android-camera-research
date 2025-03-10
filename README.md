# Android Camera Research

A comprehensive research repository on Android camera development, with a focus on skeletal tracking and augmented reality using clean architecture patterns to avoid common Gradle and native code conflicts.

## Overview

This project explores various approaches for building Android camera applications that incorporate skeletal tracking and AR capabilities. By following clean architecture patterns, we aim to minimize the common issues that arise when integrating Camera2 API, ML Kit, ARCore, and other libraries that tend to conflict with each other.

## 🚀 Quick Start for Beginners

### Want to skip the research and just implement camera features?

Use our easy setup tools designed for beginners:

1. **One-click setup script**:
   - 💻 [Bash Script](easy-setup/setup.sh) for Mac/Linux users
   - 🖥️ [PowerShell Script](easy-setup/setup.ps1) for Windows users

2. **IDE Integration**:
   - 🛠️ [Android Studio Guide](easy-setup/android-studio-plugin/README.md)
   - 📝 [VSCode Setup](easy-setup/vscode/README.md)

3. **Starter Templates**:
   - 📦 [Ready-to-use project templates](easy-setup/starter-templates/README.md)

Check the [Easy Setup Guide](easy-setup/README.md) for more details!

## Repository Structure

### Core Documentation

- **[Camera2 API](camera2-api.md)**: Detailed overview of the Camera2 API implementation
- **[ML Kit Pose Detection](ml-kit-pose-detection.md)**: Guide to implementing skeletal tracking with ML Kit
- **[ARCore Augmented Reality](arcore-augmented-reality.md)**: ARCore implementation for AR experiences
- **[SceneView 3D/AR](sceneview-3d-ar.md)**: Using SceneView for 3D and AR development
- **[Clean Architecture Guide](clean-architecture-guide.md)**: Architecture patterns to avoid dependency conflicts
- **[React Native Vision Camera Analysis](react-native-vision-camera-analysis.md)**: Hybrid approach analysis
- **[Vuforia Guide](vuforia-guide.md)**: Comprehensive guide to Vuforia implementation
- **[Vuforia Integration](vuforia-integration.md)**: Integrating Vuforia with our camera research project

### Code Samples

- **[Camera2 + ML Kit Integration](code-samples/camera2-mlkit-integration.kt)**: Integration between Camera2 and ML Kit for pose detection
- **[ML Kit + ARCore Integration](code-samples/mlkit-arcore-integration.kt)**: Combining pose detection with AR experiences
- **[Multi-Subject Tracker](code-samples/multi-subject-tracker.kt)**: Implementation for tracking multiple humans, cats, and dogs simultaneously
- **[Skeletal Visualization](code-samples/skeletal-visualization-renderer.kt)**: Rendering skeletal overlays for different subject types

### Project Structure

- **[Build Setup](project-structure/build.gradle.kts)**: Root build configuration to avoid conflicts
- **[Dependency Management](project-structure/buildSrc/src/main/kotlin/Dependencies.kt)**: Centralized dependency management
- **[Module Configuration](project-structure/feature-camera/build.gradle.kts)**: Feature module build configuration

### Sample Project

- **[Sample Application](sample-project/)**: Demonstration of clean architecture principles with camera, ML Kit, and ARCore

### Performance and Best Practices

- **[Performance Benchmarks](benchmarks/camera-ml-ar-benchmarks.md)**: Comparative benchmarks of different approaches
- **[Multi-Skeletal Tracking Benchmarks](benchmarks/multi-skeletal-tracking-benchmarks.md)**: Benchmarking methodologies for multi-subject tracking systems
- **[Threading & Memory Management](guides/threading-memory-management.md)**: Best practices for resource management

### Advanced Research

- **[Multi-Skeletal Tracking](research/multi-skeletal-tracking.md)**: Research on tracking multiple subjects (humans, cats, dogs) simultaneously
- **[Animal Pose Detection Model](research/animal-pose-detection-model.md)**: Architecture for custom TensorFlow Lite models for animal pose detection
- **[React Native Camera Integration](research/react-native-camera-integration.md)**: Guide for integrating camera functionality with multi-subject tracking in React Native
- **[Future Research Directions](research/future-research-directions.md)**: Planned next steps for advancing multi-skeletal tracking research
- **[Latest Research Findings (2025)](research/latest-research-findings-2025.md)**: Current state-of-the-art in animal motion tracking and multi-subject pose estimation
- **[npm/Node.js Build Issues](research/npm-node-advanced-troubleshooting.md)**: In-depth troubleshooting for React Native integrations
- **[React Native Integration Guide](research/react-native-integration-guide.md)**: Detailed guide for integrating React Native with camera functionality
- **[CI/CD Build Solutions](research/ci-cd-build-solutions.md)**: Solutions for CI/CD build issues with camera projects

### CI/CD

- **[GitHub Actions Workflow](ci-cd/.github/workflows/android.yml)**: Continuous integration configuration

## Key Findings

### Integration Challenges

1. **Camera Access Conflicts**: Camera2 API, ARCore, and Vuforia often compete for camera access, requiring careful resource management and camera sharing configurations.

2. **Dependency Versioning**: ML Kit, ARCore, Vuforia, and camera libraries frequently have conflicting dependency requirements, necessitating centralized dependency management.

3. **Native Code Issues**: NDK and ABI configurations are critical when combining libraries with native components.

4. **Threading Complexity**: Frame processing, ML inference, and AR rendering each require proper threading strategies to maintain performance.

5. **npm/Node.js Build Issues**: React Native camera integrations face unique challenges with npm scripts, patch-package, buffer handling, and module compilation.

6. **Multi-Subject Attribution**: Maintaining correct subject-skeleton associations during interactions between multiple subjects poses significant challenges.

### Recommended Approach

Based on our research, the most effective approach for integrating these technologies involves:

1. **Clean Architecture**: Separation of concerns through well-defined modules
2. **Centralized Dependency Management**: Using Gradle Kotlin DSL and version catalogs
3. **Explicit Thread Management**: Dedicated threads for camera, processing, and rendering
4. **Shared Camera Configuration**: Proper configuration of Camera2 API for shared use with ARCore or Vuforia
5. **Optimized Frame Processing**: Selective frame processing and resolution reduction for ML Kit
6. **Strategic Camera Provider Selection**: Choosing between Camera2, ARCore, and Vuforia based on specific use cases
7. **Two-Stage Detection**: First detect subjects, then apply appropriate pose detection models
8. **Tracking Algorithms**: Implement IoU-based tracking to maintain subject identity across frames

## Technology Comparison

### Camera APIs

| Feature | Camera2 API | CameraX |
|---------|------------|--------|
| **Lifecycle Management** | Manual | Automatic |
| **Control Level** | Fine-grained | Simplified |
| **Configuration** | Complex | Easy |
| **Use Cases** | Advanced camera apps | Most camera apps |

### AR Technologies

| Feature | ARCore | Vuforia |
|---------|--------|--------|
| **Focus** | Environmental tracking | Image/object recognition |
| **Tracking Type** | Markerless | Target-based |
| **Licensing** | Free | Commercial |
| **Camera Integration** | Easier with Google APIs | More isolated |

### Pose Detection

| Feature | ML Kit | DeepLabCut | SLEAP | YOLOv8 Pose | Custom TFLite |
|---------|--------|------------|-------|-------------|---------------|
| **Multi-subject** | No | Yes | Yes | Yes | Yes |
| **Animal Support** | No | Yes | Yes | Yes | Yes |
| **Mobile Optimized** | Yes | No | No | Partial | Yes |
| **Accuracy** | High | Very High | Very High | High | Customizable |
| **Integration Complexity** | Low | High | High | Medium | Medium |
| **Social Behavior Analysis** | No | Limited | Yes | No | Possible |

## Getting Started

To explore this research repository:

1. Browse the core documentation files to understand the fundamental concepts
2. Examine the code samples for practical implementation examples
3. Review the project structure for clean architecture patterns
4. Check the sample project for a complete integration example
5. For beginners, start with the easy setup tools
6. Review the latest research findings to see the current state-of-the-art
7. Check the future research directions to understand our roadmap

## Contributing

Contributions to this research are welcome. Areas that would benefit from additional exploration include:

- Performance optimization techniques for lower-end devices
- Integration with additional ML models beyond pose detection
- Advanced AR features and interactions based on skeletal tracking
- Improved camera resource management strategies
- Hybrid approaches using multiple AR technologies
- Datasets and training methods for custom animal pose detection models
- Improved multi-subject tracking algorithms for close interactions

## License

MIT
