# Camera2 API for Android

## Overview

The Camera2 API was introduced in Android 5.0 (Lollipop) as a replacement for the older Camera API. It provides a more sophisticated and flexible way to interact with the camera hardware, offering fine-grained control over camera settings and capabilities.

## Key Features

### 1. Architecture

Camera2 API follows a pipeline-based architecture:

- **CameraManager**: System service to discover and open camera devices
- **CameraDevice**: Represents a camera device
- **CameraCaptureSession**: Manages capture requests for the camera
- **CaptureRequest**: Contains parameters for a single capture operation
- **CaptureResult**: Contains output metadata from a capture operation
- **CameraCharacteristics**: Contains static information about a camera device

### 2. Camera Control Capabilities

- Manual control over focus, exposure, ISO, and white balance
- RAW image capture support
- High-resolution image capture
- Multi-camera support
- High-speed video recording
- Depth sensing (on supported devices)

### 3. Performance Benefits

- Zero-shutter lag capture
- Burst mode shooting
- Frame-by-frame camera control
- Efficient image processing pipeline

## Implementation

### 1. Basic Setup

```kotlin
class Camera2Activity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private lateinit var previewSurface: Surface
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        
        // Get the camera manager service
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    
    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }
    
    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }
    
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }
    
    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    private fun openCamera() {
        try {
            // Get the back camera ID
            val cameraId = cameraManager.cameraIdList.first { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_BACK
            }
            
            // Check for camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            
            // Open the camera
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            this@Camera2Activity.cameraDevice = camera
            createCameraPreviewSession()
        }
        
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }
        
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            this@Camera2Activity.finish()
        }
    }
    
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)
            
            previewSurface = Surface(texture)
            
            // Set up capture request for preview
            captureRequestBuilder = 
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(previewSurface)
            
            // Create a capture session
            cameraDevice.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        updatePreview()
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@Camera2Activity, 
                            "Failed to configure camera", Toast.LENGTH_SHORT).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    private fun updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, 
            CaptureRequest.CONTROL_MODE_AUTO)
        
        try {
            cameraCaptureSession.setRepeatingRequest(
                captureRequestBuilder.build(),
                null,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    private fun closeCamera() {
        if (::cameraCaptureSession.isInitialized) {
            cameraCaptureSession.close()
        }
        
        if (::cameraDevice.isInitialized) {
            cameraDevice.close()
        }
    }
}
```

### 2. Capturing Still Images

```kotlin
private fun takePicture() {
    try {
        // Set up image reader for high-res photos
        imageReader = ImageReader.newInstance(
            imageDimension.width, 
            imageDimension.height, 
            ImageFormat.JPEG, 
            1
        )
        
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            saveImage(bytes)
            image.close()
        }, backgroundHandler)
        
        // Create a capture request for still image
        val captureBuilder = 
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(imageReader.surface)
        
        // Set auto-focus mode
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, 
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        
        // Set orientation
        val rotation = windowManager.defaultDisplay.rotation
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))
        
        // Capture the image
        cameraCaptureSession.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                Toast.makeText(this@Camera2Activity, "Picture taken", Toast.LENGTH_SHORT).show()
                createCameraPreviewSession()
            }
        }, null)
        
    } catch (e: CameraAccessException) {
        e.printStackTrace()
    }
}
```

## Advanced Features

### 1. Processing Camera Frames for Computer Vision

```kotlin
// Set up ImageReader for processing frames
private lateinit var frameReader: ImageReader
private val yuvBytes = Array(3) { ByteArray(0) }
private lateinit var rgbBytes: IntArray
private lateinit var rgbFrameBitmap: Bitmap

private fun setupFrameProcessing() {
    // Create ImageReader for YUV format
    frameReader = ImageReader.newInstance(
        previewSize.width,
        previewSize.height,
        ImageFormat.YUV_420_888,
        2
    )
    
    rgbBytes = IntArray(previewSize.width * previewSize.height)
    rgbFrameBitmap = Bitmap.createBitmap(
        previewSize.width,
        previewSize.height,
        Bitmap.Config.ARGB_8888
    )
    
    frameReader.setOnImageAvailableListener({ reader ->
        val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
        
        // Process the frame
        processImageForVision(image)
        
        image.close()
    }, backgroundHandler)
    
    // Add the frameReader surface to the capture session
    createCaptureSessionWithFrameProcessing()
}

private fun createCaptureSessionWithFrameProcessing() {
    val surfaces = listOf(previewSurface, frameReader.surface)
    
    cameraDevice.createCaptureSession(
        surfaces,
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                
                // Set up the preview request
                captureRequestBuilder = 
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequestBuilder.addTarget(previewSurface)
                captureRequestBuilder.addTarget(frameReader.surface)
                
                // Start the preview
                cameraCaptureSession.setRepeatingRequest(
                    captureRequestBuilder.build(), null, backgroundHandler
                )
            }
            
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@Camera2Activity, 
                    "Failed to configure camera", Toast.LENGTH_SHORT).show()
            }
        },
        null
    )
}

private fun processImageForVision(image: Image) {
    // Convert YUV to RGB for easier processing
    val yuvToRgbConverter = YuvToRgbConverter(this)
    yuvToRgbConverter.yuvToRgb(image, rgbFrameBitmap)
    
    // Now rgbFrameBitmap contains the camera frame in RGB format
    // You can now pass this bitmap to ML Kit or other vision libraries
    runPoseDetection(rgbFrameBitmap)
}

private fun runPoseDetection(bitmap: Bitmap) {
    // This is where you would send the bitmap to ML Kit for pose detection
    // Example implementation will be covered in the ML Kit document
    
    // Convert to InputImage for ML Kit
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    
    // Process with pose detector (placeholder for actual implementation)
    // poseDetector.process(inputImage)
    //     .addOnSuccessListener { pose ->
    //         // Process the detected pose
    //     }
    //     .addOnFailureListener { e ->
    //         // Handle any errors
    //     }
}
```

### 2. Integration with Skeletal Tracking

To integrate Camera2 API with skeletal tracking libraries like ML Kit, follow these steps:

1. Set up a processing pipeline for camera frames as shown above
2. Convert the camera frames to a format suitable for the pose detection library
3. Process frames through the pose detection library
4. Visualize or react to the detected poses

The key challenge is efficiently processing the frames without causing performance issues. Consider these optimization strategies:

- Process only every Nth frame (e.g., every 2nd or 3rd frame)
- Resize the image to a smaller resolution before processing
- Run the detection on a background thread
- Consider using hardware acceleration if available

## Resources

- [Official Android Camera2 Documentation](https://developer.android.com/reference/android/hardware/camera2/package-summary)
- [Camera2 API Samples](https://github.com/android/camera-samples)
- [Understanding the Camera2 API](https://medium.com/androiddevelopers/understanding-the-camera2-api-3460e02a5cc2)
