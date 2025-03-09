# ML Kit Pose Detection

## Overview

ML Kit's Pose Detection API allows Android developers to detect the pose of a human body in real-time from either a static image or a continuous video feed. The API identifies up to 33 skeletal landmarks on the body, including the face, torso, arms, and legs.

## Features

- **Full-body pose detection**: Tracks 33 landmarks across the entire body
- **Real-time processing**: Optimized for live video processing
- **Accuracy vs. performance options**: Choose between faster or more accurate detection
- **Pose classification**: Can be extended to recognize specific poses
- **Z-coordinate support**: Get depth information for 3D pose tracking

## Implementation

### Dependencies

```gradle
dependencies {
    // ML Kit Pose Detection with accurate model
    implementation 'com.google.mlkit:pose-detection-accurate:18.0.0-beta3'
    // OR ML Kit Pose Detection with fast model
    implementation 'com.google.mlkit:pose-detection:18.0.0-beta3'
}
```

### Basic Setup

```kotlin
// Create pose detector options
val options = AccuratePoseDetectorOptions.Builder()
    .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
    .build()

// Create pose detector
val poseDetector = PoseDetection.getClient(options)

// Process image
poseDetector.process(inputImage)
    .addOnSuccessListener { pose ->
        // Process detected pose
        processPose(pose)
    }
    .addOnFailureListener { e ->
        // Handle any errors
        Log.e("PoseDetection", "Error detecting pose", e)
    }
```

### Processing Detected Poses

```kotlin
private fun processPose(pose: Pose) {
    // Get all landmarks
    val allLandmarks = pose.getAllPoseLandmarks()
    
    if (allLandmarks.isEmpty()) {
        // No pose detected
        return
    }
    
    // Access specific landmarks
    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    
    // Calculate angles or distances between landmarks
    // For example, calculate angle at elbow
    val leftElbowAngle = calculateAngle(
        leftShoulder.position, 
        leftElbow.position, 
        leftWrist.position
    )
    
    // Use the pose information for your application logic
    // E.g., detect if a specific pose is being performed
    if (leftElbowAngle < 30.0 && rightElbowAngle < 30.0) {
        // Arms are raised straight up
    }
}
```

### Visualizing the Pose

To visualize the detected pose, you'll need to create a custom View that draws the skeleton on top of the camera preview:

```kotlin
class PoseGraphic(overlay: GraphicOverlay, private val pose: Pose) : GraphicOverlay.Graphic(overlay) {

    private val leftPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4.0f
    }
    
    private val rightPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 4.0f
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.getAllPoseLandmarks()
        if (landmarks.isEmpty()) return

        // Draw all points
        for (landmark in landmarks) {
            canvas.drawCircle(
                translateX(landmark.position.x),
                translateY(landmark.position.y),
                8.0f,
                if (landmark.landmarkType.ordinal % 2 == 0) leftPaint else rightPaint
            )
        }

        // Draw lines connecting landmarks
        // Left side body connections
        drawLineIfLandmarksExist(canvas, 
            PoseLandmark.LEFT_SHOULDER, 
            PoseLandmark.LEFT_ELBOW, 
            leftPaint
        )
        
        drawLineIfLandmarksExist(canvas, 
            PoseLandmark.LEFT_ELBOW, 
            PoseLandmark.LEFT_WRIST, 
            leftPaint
        )
        
        // Add more connections as needed
    }

    private fun drawLineIfLandmarksExist(
        canvas: Canvas,
        startLandmarkType: Int,
        endLandmarkType: Int,
        paint: Paint
    ) {
        val startLandmark = pose.getPoseLandmark(startLandmarkType)
        val endLandmark = pose.getPoseLandmark(endLandmarkType)

        if (startLandmark != null && endLandmark != null) {
            canvas.drawLine(
                translateX(startLandmark.position.x),
                translateY(startLandmark.position.y),
                translateX(endLandmark.position.x),
                translateY(endLandmark.position.y),
                paint
            )
        }
    }
}
```

### Integration with Camera2 API

Here's how to integrate ML Kit pose detection with the Camera2 API:

```kotlin
class PoseDetectionActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var poseDetector: PoseDetector
    private lateinit var graphicOverlay: GraphicOverlay
    
    // Other necessary variables...
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose_detection)
        
        // Initialize pose detector
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)
        
        graphicOverlay = findViewById(R.id.graphic_overlay)
        
        // Set up camera...
    }
    
    private fun processImage(image: Image, rotationDegrees: Int) {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vSize.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        // Copy Y
        yBuffer.get(nv21, 0, ySize)
        
        // Copy UV data - this is an approximation that works but not optimal
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        // Convert to InputImage
        val inputImage = InputImage.fromByteArray(
            nv21, 
            image.width,
            image.height,
            rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21
        )
        
        // Process with pose detector
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                graphicOverlay.clear()
                graphicOverlay.add(PoseGraphic(graphicOverlay, pose))
                graphicOverlay.postInvalidate()
                
                // Add any custom pose processing here
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Pose detection failed", e)
            }
            .addOnCompleteListener {
                image.close()
            }
    }
}
```

## Advanced Features

### Pose Classification

ML Kit doesn't provide built-in pose classification, but you can build it on top of the detected landmarks. A common approach is to:

1. Calculate angles between key joints
2. Compare these angles with reference poses
3. Use a threshold to determine if the current pose matches a reference pose

```kotlin
class PoseClassifier {
    private val squatPose = mapOf(
        "kneeAngle" to 45.0f,
        "hipAngle" to 60.0f
        // Other relevant angles
    )
    
    fun classifyPose(pose: Pose): String {
        // Extract current angles from pose
        val currentKneeAngle = calculateKneeAngle(pose)
        val currentHipAngle = calculateHipAngle(pose)
        
        // Compare with reference poses
        val squatDifference = abs(currentKneeAngle - squatPose["kneeAngle"]!!) +
                              abs(currentHipAngle - squatPose["hipAngle"]!!)
        
        // Check if the difference is below threshold
        return if (squatDifference < 30.0f) "squat" else "unknown"
    }
    
    private fun calculateKneeAngle(pose: Pose): Float {
        // Get relevant landmarks
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        
        // Calculate angle
        return calculateAngle(
            hip.position,
            knee.position,
            ankle.position
        )
    }
    
    // Utility function to calculate the angle between three points
    private fun calculateAngle(
        point1: PointF,
        point2: PointF,
        point3: PointF
    ): Float {
        val angle1 = atan2(point1.y - point2.y, point1.x - point2.x)
        val angle2 = atan2(point3.y - point2.y, point3.x - point2.x)
        
        var result = Math.toDegrees((angle1 - angle2).toDouble()).toFloat()
        result = abs(result)
        
        if (result > 180) {
            result = 360 - result
        }
        
        return result
    }
}
```

### Smoothing Pose Detection

To reduce jitter and make pose detection more stable:

```kotlin
class PoseSmoother(private val smoothingWindow: Int = 5) {
    private val landmarkHistory = mutableMapOf<Int, ArrayDeque<PointF>>()
    
    fun smoothLandmark(landmark: PoseLandmark): PointF {
        val landmarkType = landmark.landmarkType
        
        // Initialize history for this landmark type if needed
        if (!landmarkHistory.containsKey(landmarkType)) {
            landmarkHistory[landmarkType] = ArrayDeque()
        }
        
        val history = landmarkHistory[landmarkType]!!
        
        // Add current position to history
        history.addLast(landmark.position)
        
        // Keep only the most recent positions
        while (history.size > smoothingWindow) {
            history.removeFirst()
        }
        
        // Calculate average position
        var sumX = 0f
        var sumY = 0f
        
        for (point in history) {
            sumX += point.x
            sumY += point.y
        }
        
        return PointF(sumX / history.size, sumY / history.size)
    }
    
    fun smoothPose(pose: Pose): Pose {
        // This is a simplified approach - in a real implementation,
        // you would create a new Pose object with smoothed landmarks
        for (landmark in pose.allPoseLandmarks) {
            val smoothedPosition = smoothLandmark(landmark)
            // Replace the landmark's position with the smoothed one
            // (This would require reflection or a custom Pose implementation)
        }
        
        return pose
    }
}
```

### Performance Optimization

For real-time applications, consider these optimizations:

1. Use `STREAM_MODE` for video processing
2. Consider using the fast model for higher frame rates if accuracy is less critical
3. Process frames at a lower resolution
4. Skip frames if processing can't keep up with camera feed
5. Run post-processing operations on a background thread

```kotlin
// Process every Nth frame
private var frameCounter = 0
private val processEveryNthFrame = 3

private fun processFrameWithSkipping(image: Image, rotationDegrees: Int) {
    frameCounter++
    
    if (frameCounter % processEveryNthFrame != 0) {
        image.close()
        return
    }
    
    // Process the frame as usual
    processImage(image, rotationDegrees)
}
```

## Best Practices

1. **Lighting**: Ensure good lighting conditions for better pose detection accuracy
2. **Full body visibility**: The subject should be fully visible in the frame
3. **Distance**: The subject should be at an appropriate distance (not too close or far)
4. **Background**: A contrasting background can improve detection accuracy
5. **Smoothing**: Apply smoothing to detected landmarks to reduce jitter

## Working with 3D Information

ML Kit's pose detection provides Z-coordinates for depth information:

```kotlin
fun processPositionIn3D(pose: Pose) {
    for (landmark in pose.allPoseLandmarks) {
        val position3D = landmark.position3D
        
        // X and Y are in pixels relative to the image
        val x = position3D.x
        val y = position3D.y
        
        // Z is the depth in "image pixels equivalent"
        // Negative Z means the point is closer to the camera
        val z = position3D.z
        
        // Use the 3D information for advanced applications
        // such as AR overlays or 3D pose analysis
    }
}
```

## Resources

- [ML Kit Pose Detection Documentation](https://developers.google.com/ml-kit/vision/pose-detection)
- [ML Kit Sample Code](https://github.com/googlesamples/mlkit)
- [Third-party Example: PoseDetection-MLKit](https://github.com/icanerdogan/PoseDetection-MLKit)
