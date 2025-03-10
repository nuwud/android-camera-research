# Multi-Skeletal Tracking Research Summary

## Overview

This document summarizes the multi-skeletal tracking research we've added to the repository, focusing on the challenges, solutions, and implementation examples for tracking multiple subjects (humans, cats, and dogs) simultaneously.

## Research Contributions

### 1. Multi-Subject Tracking Research

We've identified and documented the key challenges in tracking multiple subjects simultaneously:

- **ML Kit Limitations**: Google's ML Kit can only detect one person per frame
- **Subject Attribution**: Maintaining correct identity across frames is challenging
- **Cross-Species Detection**: Different anatomical structures require specialized models
- **Performance Constraints**: Real-time tracking of multiple subjects is computationally intensive

We've proposed three potential solutions:

1. **ML Kit Extension with Custom TensorFlow Models**: Using ML Kit for humans and custom TFLite models for animals
2. **DeepLabCut-Inspired Custom Implementation**: Creating a unified detection model optimized for mobile
3. **Hybrid Approach with Server-Side Processing**: Distributing workload between device and server

### 2. Animal Pose Detection Models

We've designed an architecture for custom TensorFlow Lite models for animal pose detection:

- **Cat Model**: 17 keypoints covering head, body, and limbs
- **Dog Model**: 20 keypoints with additional points for the throat, mid-tail, and chest
- **Multi-Stage Architecture**: Feature extraction → keypoint heatmaps → part affinity fields → post-processing
- **Optimization Techniques**: Quantization, pruning, and hardware acceleration support

### 3. React Native Integration

We've created a comprehensive guide for integrating our multi-skeletal tracking with React Native applications:

- **Camera Dependencies**: Analysis of deprecated libraries and recommendation of `react-native-vision-camera`
- **Architecture**: Frame processor design for native bridge communication
- **Implementation Steps**: Detailed implementation for frame processing and subject tracking
- **Common Issues**: Solutions for performance, memory leaks, and permissions issues

### 4. Benchmarking Framework

We've designed a benchmarking methodology for multi-skeletal tracking systems:

- **Performance Metrics**: FPS, processing time, model size
- **Accuracy Metrics**: Detection rates, PCK scores, tracking consistency
- **Resource Utilization**: Memory, CPU, power consumption
- **Robustness Tests**: Environmental variations, subject variations, edge cases

## Implementation Examples

### 1. Multi-Subject Tracker

We've implemented a `MultiSubjectTracker` class that:

- Uses ML Kit's object detection to identify humans, cats, and dogs
- Applies ML Kit's pose detection for humans
- Provides a placeholder for custom animal pose detection models
- Maintains subject identity across frames using IoU tracking
- Handles multiple subjects simultaneously with proper attribution

### 2. Skeletal Visualization

We've implemented a `SkeletalVisualizationRenderer` that:

- Renders different skeletal structures for humans, cats, and dogs
- Draws accurate connections between detected keypoints
- Visualizes bounding boxes and identity labels
- Provides an easy-to-use API for overlay generation

## Future Directions

1. **Model Training**: Collect and annotate datasets for cat and dog pose estimation
2. **Performance Optimization**: Optimize for better FPS on mid to low-end devices
3. **Interaction Detection**: Detect interactions between subjects for advanced applications
4. **Multi-View Integration**: Combine data from multiple cameras for improved accuracy
5. **On-Device Learning**: Implement adaptive models that improve with user data

## Resources

- [Multi-Skeletal Tracking Research](research/multi-skeletal-tracking.md)
- [Animal Pose Detection Model Architecture](research/animal-pose-detection-model.md)
- [React Native Camera Integration](research/react-native-camera-integration.md)
- [Multi-Skeletal Tracking Benchmarks](benchmarks/multi-skeletal-tracking-benchmarks.md)
- [Multi-Subject Tracker Implementation](code-samples/multi-subject-tracker.kt)
- [Skeletal Visualization Renderer](code-samples/skeletal-visualization-renderer.kt)

## References

1. Google ML Kit Pose Detection: [https://developers.google.com/ml-kit/vision/pose-detection](https://developers.google.com/ml-kit/vision/pose-detection)
2. DeepLabCut: [https://github.com/DeepLabCut/DeepLabCut](https://github.com/DeepLabCut/DeepLabCut)
3. Multi-animal pose estimation with DeepLabCut: [https://www.nature.com/articles/s41592-022-01443-0](https://www.nature.com/articles/s41592-022-01443-0)
4. Multiple Skeleton Tracking Techniques: [https://www.youtube.com/watch?v=ym8Tnmiz5N8](https://www.youtube.com/watch?v=ym8Tnmiz5N8)
5. VVVV Forum on Multiple Skeleton Tracking: [https://forum.vvvv.org/t/multiple-skeleton-tracking-question/22186](https://forum.vvvv.org/t/multiple-skeleton-tracking-question/22186)