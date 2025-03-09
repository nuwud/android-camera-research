# SceneView for 3D and AR Development

## Overview

SceneView is a modern Android library that simplifies 3D and AR development. It's built on top of Google's Filament rendering engine and ARCore, providing a high-level API for creating immersive experiences with minimal boilerplate code. SceneView is the spiritual successor to Sceneform, which was deprecated by Google but maintained and enhanced by the SceneView team.

## Key Features

### 1. Unified API for 3D and AR

- Single, consistent API for both 3D-only and AR applications
- Supports both traditional View-based UI and Jetpack Compose

### 2. Filament-Powered Rendering

- High-performance PBR (Physically Based Rendering)
- Advanced lighting and materials
- Efficient rendering optimized for mobile devices

### 3. Model Loading and Management

- Supports glTF and GLB file formats
- Easy model loading from assets or remote URLs
- Animation support

### 4. AR Integration

- Seamless ARCore integration
- Plane detection and tracking
- Anchor management
- Light estimation
- ARCore Extensions support (Cloud Anchors, Geospatial API, etc.)

### 5. Physics and Interaction

- Collision detection system
- Gesture handling (touch, drag, rotate, etc.)
- Object transformation

## Implementation

### Dependencies

```gradle
dependencies {
    // For 3D without AR
    implementation 'io.github.sceneview:sceneview:2.2.1'
    
    // For AR applications
    implementation 'io.github.sceneview:arsceneview:2.2.1'
}
```

### Basic 3D Scene Setup (View-based)

```kotlin
class ModelViewerActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_viewer)
        
        sceneView = findViewById(R.id.scene_view)
        
        // Load 3D model
        ModelNode.create(context = this) { modelNode ->
            modelNode.loadModel(
                context = this,
                lifecycle = lifecycle,
                glbFileLocation = "models/model.glb",
                autoAnimate = true,
                scaleToUnits = 1.0f
            )
            // Add the model to the scene
            sceneView.addChild(modelNode)
            
            // Center the model in the scene
            sceneView.camera.lookAt(modelNode.position, modelNode.boundingBoxCenter)
        }
    }
}
```

### 3D Scene with Jetpack Compose

```kotlin
@Composable
fun ModelViewerScreen() {
    val engine = rememberEngine()
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine)
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val collisionSystem = rememberCollisionSystem(view)
    
    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        view = view,
        renderer = renderer,
        scene = scene,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        environmentLoader = environmentLoader,
        collisionSystem = collisionSystem,
        mainLightNode = rememberMainLightNode(engine) {
            intensity = 100_000.0f
        },
        environment = rememberEnvironment(environmentLoader) {
            environmentLoader.createHDREnvironment(
                assetFileLocation = "environments/sky_2k.hdr"
            )!!
        },
        cameraNode = rememberCameraNode(engine) {
            position = Position(z = 4.0f)
        },
        cameraManipulator = rememberCameraManipulator(),
        childNodes = rememberNodes {
            // Add a glTF model
            add(
                ModelNode(
                    modelInstance = modelLoader.createModelInstance(
                        assetFileLocation = "models/model.glb"
                    ),
                    scaleToUnits = 1.0f
                )
            )
            // Add a basic shape (cylinder)
            add(CylinderNode(
                engine = engine,
                radius = 0.2f,
                height = 2.0f,
                materialInstance = materialLoader.createColorInstance(
                    color = Color.Blue,
                    metallic = 0.5f,
                    roughness = 0.2f,
                    reflectance = 0.4f
                )
            ).apply {
                transform(
                    position = Position(y = 1.0f),
                    rotation = Rotation(x = 90.0f)
                )
            })
        }
    )
}
```

### AR with SceneView (View-based)

```kotlin
class ArModelViewerActivity : AppCompatActivity() {
    private lateinit var arSceneView: ArSceneView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_model_viewer)
        
        arSceneView = findViewById(R.id.ar_scene_view)
        
        // Enable plane detection
        arSceneView.planeRenderer = true
        
        // Handle taps on detected planes
        arSceneView.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            // Create an anchor at the tap location
            val anchor = hitResult.createAnchor()
            
            // Load and place a 3D model at the anchor
            ModelNode.create(context = this) { modelNode ->
                modelNode.loadModel(
                    context = this,
                    lifecycle = lifecycle,
                    glbFileLocation = "models/chair.glb",
                    autoAnimate = true,
                    scaleToUnits = 1.0f
                )
                // Add the model to the scene and attach to anchor
                arSceneView.addChild(modelNode)
                modelNode.anchor = anchor
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        arSceneView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }
}
```

### AR with Jetpack Compose

```kotlin
@Composable
fun ARModelViewerScreen() {
    val engine = rememberEngine()
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine)
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val collisionSystem = rememberCollisionSystem(view)
    
    ARScene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        view = view,
        renderer = renderer,
        scene = scene,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        environmentLoader = environmentLoader,
        collisionSystem = collisionSystem,
        planeRenderer = true, // Enable plane detection
        cameraStream = rememberARCameraStream(materialLoader),
        sessionFeatures = setOf(),
        sessionConfiguration = { session, config ->
            config.depthMode =
                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            config.lightEstimationMode =
                Config.LightEstimationMode.ENVIRONMENTAL_HDR
        },
        onSessionCreated = { session ->
            // Session is ready
        },
        onSessionUpdated = { session, frame ->
            // Frame update
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { motionEvent, node ->
                // Handle tap on existing node
            }
        ),
        onTapArPlaneListener = { hitResult, plane, motionEvent ->
            // Create an anchor and add a model
            val anchor = hitResult.createAnchor()
            
            ModelNode(
                modelInstance = modelLoader.createModelInstance(
                    assetFileLocation = "models/chair.glb"
                ),
                scaleToUnits = 1.0f
            ).apply {
                this.anchor = anchor
                // Add to scene
                scene.addChild(this)
            }
        }
    )
}
```

## Advanced Features

### 1. Custom Materials and Shaders

SceneView allows creating custom materials beyond the standard PBR material:

```kotlin
// Create a material from a custom Filament material (.filamat file)
val customMaterial = materialLoader.createMaterial(
    assetFileLocation = "materials/custom_shader.filamat"
)

// Create an instance of the custom material with parameters
val customMaterialInstance = customMaterial.createInstance().apply {
    setFloat3Parameter("baseColor", 1.0f, 0.0f, 0.0f) // Red
    setFloatParameter("roughness", 0.8f)
    setFloatParameter("metallic", 0.0f)
}

// Use with a 3D node
val sphereNode = SphereNode(
    engine = engine,
    radius = 1.0f,
    materialInstance = customMaterialInstance
)
```

### 2. Physics and Collisions

SceneView provides a collision system for detecting interactions between nodes:

```kotlin
// Enable collision shape for a node
modelNode.collisionShape = CollisionShape(
    type = CollisionShape.Type.MESH,
    size = modelNode.extents,
    center = modelNode.boundingBoxCenter
)

// Handle collisions
collisionSystem.addCollisionListener { nodeA, nodeB ->
    // Handle collision between nodeA and nodeB
    nodeA.materialInstance = highlightMaterial
    nodeB.materialInstance = highlightMaterial
    
    // Play collision sound or trigger other effects
    playCollisionSound()
}

// Ray casting for object picking
val ray = collisionSystem.screenPointToRay(motionEvent.x, motionEvent.y)
val rayHit = collisionSystem.raycast(ray)
rayHit?.node?.let { node ->
    // User tapped on this node
    selectNode(node)
}
```

### 3. Animation Control

SceneView supports animation from glTF models:

```kotlin
// Load a model with animations
val modelNode = ModelNode(
    modelInstance = modelLoader.createModelInstance(
        assetFileLocation = "models/animated_character.glb"
    ),
    autoAnimate = false // Don't start animation automatically
)

// Access model animations
val animationCount = modelNode.getAnimationCount()
for (i in 0 until animationCount) {
    val animationName = modelNode.getAnimationName(i)
    Log.d("Animations", "Found animation: $animationName")
}

// Play a specific animation
modelNode.playAnimation(
    animationName = "Walk",
    loop = true
)

// Blend between animations
modelNode.playAnimation(
    animationName = "Run",
    loop = true,
    transitionDuration = 0.5f // Blend over 0.5 seconds
)

// Control animation speed
modelNode.animationSpeed = 1.5f // 50% faster than normal

// Pause animation
modelNode.pauseAnimation()

// Resume animation
modelNode.resumeAnimation()

// Stop animation
modelNode.stopAnimation()
```

### 4. Environment and Lighting

SceneView provides control over scene lighting and environment:

```kotlin
// Create environment from HDR image
val environment = environmentLoader.createHDREnvironment(
    assetFileLocation = "environments/sunset.hdr"
)

// Set environment for the scene
scene.setEnvironment(environment)

// Create and customize the main light
val mainLight = Node()
mainLight.light = Light.Builder(Light.Type.DIRECTIONAL)
    .color(Color(1.0f, 0.9f, 0.8f)) // Warm sunlight color
    .intensity(80_000.0f)
    .direction(0.0f, -1.0f, -1.0f) // Direction vector
    .castShadows(true)
    .build()

// Add light to scene
scene.addChild(mainLight)

// Add additional point light
val pointLight = Node()
pointLight.light = Light.Builder(Light.Type.POINT)
    .color(Color(0.0f, 0.5f, 1.0f)) // Blue light
    .intensity(10_000.0f)
    .falloff(10.0f) // Light range
    .build()
pointLight.position = Position(0.0f, 2.0f, 0.0f) // Position above the scene

// Add to scene
scene.addChild(pointLight)
```

### 5. AR Cloud Anchors

SceneView can work with ARCore Cloud Anchors for shared AR experiences:

```kotlin
// Host a Cloud Anchor
private fun hostCloudAnchor(anchor: Anchor) {
    val session = arSceneView.arSession
    val hostedAnchor = session.hostCloudAnchor(anchor)
    
    // Track the hosting status
    arSceneView.scene.addOnUpdateListener {
        val state = hostedAnchor.cloudAnchorState
        when (state) {
            CloudAnchorState.SUCCESS -> {
                // Get the cloud anchor ID to share with other users
                val cloudAnchorId = hostedAnchor.cloudAnchorId
                saveAndShareCloudAnchorId(cloudAnchorId)
                // Remove the update listener to avoid repeated success callbacks
                arSceneView.scene.removeOnUpdateListener(this)
            }
            CloudAnchorState.ERROR_HOSTING_DATASET_PROCESSING_FAILED,
            CloudAnchorState.ERROR_CLOUD_SERVICE_ERROR,
            CloudAnchorState.ERROR_HOSTING_SERVICE_UNAVAILABLE,
            CloudAnchorState.ERROR_INTERNAL -> {
                // Handle error states
                showError("Error hosting cloud anchor: $state")
                arSceneView.scene.removeOnUpdateListener(this)
            }
            else -> {
                // Still in progress
            }
        }
    }
}

// Resolve a Cloud Anchor
private fun resolveCloudAnchor(cloudAnchorId: String) {
    val session = arSceneView.arSession
    val resolvedAnchor = session.resolveCloudAnchor(cloudAnchorId)
    
    // Track the resolving status
    arSceneView.scene.addOnUpdateListener {
        val state = resolvedAnchor.cloudAnchorState
        when (state) {
            CloudAnchorState.SUCCESS -> {
                // Cloud anchor resolved successfully
                // Place content at the resolved anchor
                placeContentAtAnchor(resolvedAnchor)
                arSceneView.scene.removeOnUpdateListener(this)
            }
            CloudAnchorState.ERROR_RESOLVING_LOCALIZATION_NO_MATCH,
            CloudAnchorState.ERROR_RESOLVING_DATASET_PROCESSING_FAILED,
            CloudAnchorState.ERROR_RESOLVING_SDK_VERSION_TOO_OLD,
            CloudAnchorState.ERROR_CLOUD_SERVICE_ERROR -> {
                // Handle error states
                showError("Error resolving cloud anchor: $state")
                arSceneView.scene.removeOnUpdateListener(this)
            }
            else -> {
                // Still in progress
            }
        }
    }
}
```

### 6. Geospatial API Integration

SceneView works with ARCore's Geospatial API for location-based AR:

```kotlin
// Enable Geospatial mode
val config = Config(arSceneView.arSession)
config.geospatialMode = Config.GeospatialMode.ENABLED
arSceneView.arSession.configure(config)

// Check if Geospatial API is ready
arSceneView.scene.addOnUpdateListener {
    val earth = arSceneView.arSession.earth
    if (earth?.trackingState == TrackingState.TRACKING) {
        val cameraGeospatialPose = earth.cameraGeospatialPose
        
        // Get the current location
        val latitude = cameraGeospatialPose.latitude
        val longitude = cameraGeospatialPose.longitude
        val altitude = cameraGeospatialPose.altitude
        val heading = cameraGeospatialPose.heading
        
        // Update UI with current position
        updateLocationUI(latitude, longitude, altitude, heading)
        
        // Ready to place geospatial anchors
        enableGeospatialUI()
        
        // Stop checking for tracking state
        arSceneView.scene.removeOnUpdateListener(this)
    }
}

// Create an anchor at a specific geographic location
private fun createGeospatialAnchor(
    latitude: Double,
    longitude: Double,
    altitude: Double,
    heading: Double
) {
    val earth = arSceneView.arSession.earth
    
    // Create the anchor
    val anchor = earth.createAnchor(
        latitude,
        longitude,
        altitude,
        0.0f, // rotation quaternion x
        0.0f, // rotation quaternion y
        0.0f, // rotation quaternion z
        1.0f  // rotation quaternion w
    )
    
    // Place content at the anchor
    placeGeospatialContent(anchor)
}

private fun placeGeospatialContent(anchor: Anchor) {
    // Create anchor node
    val anchorNode = AnchorNode(anchor)
    anchorNode.setParent(arSceneView.scene)
    
    // Add a 3D model at this location
    ModelNode.create(context = this) { modelNode ->
        modelNode.loadModel(
            context = this,
            lifecycle = lifecycle,
            glbFileLocation = "models/geospatial_marker.glb",
            autoAnimate = true,
            scaleToUnits = 2.0f // Larger to see from a distance
        )
        modelNode.setParent(anchorNode)
    }
}
```

### 7. Integration with ML Kit for Pose Detection

SceneView can be combined with ML Kit for AR experiences that respond to body poses:

```kotlin
class ArPoseActivity : AppCompatActivity() {
    private lateinit var arSceneView: ArSceneView
    private lateinit var poseDetector: PoseDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_pose)
        
        // Initialize AR Scene View
        arSceneView = findViewById(R.id.ar_scene_view)
        
        // Initialize pose detector
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)
        
        // Set up frame processor
        arSceneView.setOnFrameUpdateListener { frameTime ->
            val frame = arSceneView.arSession.update()
            processFrameForPose(frame)
        }
    }
    
    private fun processFrameForPose(frame: Frame) {
        try {
            // Get camera image from ARCore
            val image = frame.acquireCameraImage()
            
            // Convert to ML Kit input format
            val rotation = frame.imageDisplayRotation
            val inputImage = InputImage.fromMediaImage(image, rotation)
            
            // Process with pose detector
            poseDetector.process(inputImage)
                .addOnSuccessListener { pose ->
                    if (pose.allPoseLandmarks.isNotEmpty()) {
                        // Use pose for AR experience
                        updateArWithPose(pose, frame)
                    }
                }
                .addOnCompleteListener {
                    // Always close the image when done
                    image.close()
                }
        } catch (e: Exception) {
            Log.e("ArPoseActivity", "Error processing frame", e)
        }
    }
    
    private fun updateArWithPose(pose: Pose, frame: Frame) {
        // Example: Track hand positions and place objects 
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        
        leftWrist?.let {
            // Convert 2D screen position to 3D world space
            val screenPoint = android.graphics.Point(
                it.position.x.toInt(),
                it.position.y.toInt()
            )
            val hitResult = frame.hitTest(screenPoint.x.toFloat(), screenPoint.y.toFloat())
            
            hitResult.firstOrNull()?.let { hit ->
                // Place or update an object at the left hand
                updateOrCreateHandObject(hit.createAnchor(), isLeftHand = true)
            }
        }
        
        // Similar processing for right wrist...
    }
}
```

## Optimization Tips

### 1. Model Optimization

- Reduce polygon count in 3D models
- Use efficient UV layouts and optimize textures
- Minimize the number of materials per model
- Use level-of-detail (LOD) variants for complex models

### 2. Memory Management

```kotlin
// Properly dispose of resources when done
override fun onDestroy() {
    super.onDestroy()
    
    // Dispose of nodes
    sceneView.scene.children.forEach { node ->
        if (node is ModelNode) {
            node.destroy()
        }
    }
    
    // Clear the scene
    sceneView.scene.children.clear()
    
    // Destroy the SceneView
    sceneView.destroy()
}

// Pre-load models in the background
private fun preloadModels() {
    lifecycleScope.launch(Dispatchers.IO) {
        // Load models in background thread
        val modelInstances = modelFilePaths.map { path ->
            modelLoader.createModelInstance(assetFileLocation = path)
        }
        
        // Switch to main thread to update UI
        withContext(Dispatchers.Main) {
            // Models are ready to use
            onModelsReady(modelInstances)
        }
    }
}
```

### 3. Rendering Optimizations

```kotlin
// Reduce frame rate for battery saving
sceneView.renderer.filamentRenderer.let { renderer ->
    // Limit to 30 FPS when battery is low
    if (isBatteryLow()) {
        renderer.setFrameRateOptions(
            30, // desired frame rate
            30 // interval
        )
    } else {
        // Use default (typically 60 FPS)
        renderer.setFrameRateOptions(
            0, // no limit (use system default)
            1  // every frame
        )
    }
}

// Optimize view frustum
sceneView.camera.apply {
    // Adjust near and far planes to match your scene scale
    setNearPlane(0.1f)  // Close enough for small objects
    setFarPlane(100.0f) // Far enough for your environment
}
```

### 4. AR-Specific Optimizations

```kotlin
// Configure ARCore session for performance
val config = session.config
config.updateMode = Config.UpdateMode.BLOCKING
config.focusMode = Config.FocusMode.AUTO
config.planeFindingMode = when {
    highPerformanceMode -> Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
    batteryOptimizedMode -> Config.PlaneFindingMode.HORIZONTAL
    else -> Config.PlaneFindingMode.DISABLED
}
session.configure(config)
```

## Resources

- [SceneView Documentation](https://sceneview.github.io/)
- [SceneView GitHub Repository](https://github.com/SceneView/sceneview-android)
- [SceneView Sample Apps](https://github.com/SceneView/sceneview-android/tree/main/samples)
- [Filament Documentation](https://google.github.io/filament/Filament.html)
- [ARCore Documentation](https://developers.google.com/ar/develop/java)
