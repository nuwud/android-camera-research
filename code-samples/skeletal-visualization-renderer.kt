package com.example.androidcameraresearch.visualization

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.example.androidcameraresearch.multitracking.AnimalPoseLandmark
import com.example.androidcameraresearch.multitracking.SubjectType
import com.example.androidcameraresearch.multitracking.TrackedSubject
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * SkeletalVisualizationRenderer - Renders skeletal overlays for tracked subjects
 * Visualizes humans, cats, and dogs with appropriate skeletal structures
 */
class SkeletalVisualizationRenderer(private val context: Context) {

    // Paint objects for different visual elements
    private val humanSkeletonPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    
    private val catSkeletonPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    
    private val dogSkeletonPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    
    private val landmarkPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }
    
    private val boundingBoxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isFakeBoldText = true
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }
    
    /**
     * Draw all tracked subjects on the canvas
     */
    fun drawTrackedSubjects(canvas: Canvas, subjects: List<TrackedSubject>, imageWidth: Int, imageHeight: Int) {
        subjects.forEach { subject ->
            when (subject.type) {
                SubjectType.HUMAN -> drawHumanSkeleton(canvas, subject, imageWidth, imageHeight)
                SubjectType.CAT -> drawCatSkeleton(canvas, subject, imageWidth, imageHeight)
                SubjectType.DOG -> drawDogSkeleton(canvas, subject, imageWidth, imageHeight)
            }
            
            // Draw bounding box and label for all subjects
            drawBoundingBox(canvas, subject, imageWidth, imageHeight)
        }
    }
    
    /**
     * Draw human skeletal structure
     */
    private fun drawHumanSkeleton(canvas: Canvas, subject: TrackedSubject, imageWidth: Int, imageHeight: Int) {
        val landmarks = subject.humanPoseLandmarks ?: return
        
        // Draw the landmarks
        landmarks.forEach { landmark ->
            val position = landmark.position
            val normalizedX = position.x / imageWidth
            val normalizedY = position.y / imageHeight
            val x = normalizedX * canvas.width
            val y = normalizedY * canvas.height
            val radius = 8f + (landmark.inFrameLikelihood * 5f)
            
            canvas.drawCircle(x, y, radius, landmarkPaint)
        }
        
        // Draw the connections between landmarks
        val connections = getHumanConnections()
        connections.forEach { connection ->
            val startLandmark = landmarks.find { it.landmarkType == connection.first }
            val endLandmark = landmarks.find { it.landmarkType == connection.second }
            
            if (startLandmark != null && endLandmark != null &&
                startLandmark.inFrameLikelihood > 0.5f && endLandmark.inFrameLikelihood > 0.5f) {
                val startX = (startLandmark.position.x / imageWidth) * canvas.width
                val startY = (startLandmark.position.y / imageHeight) * canvas.height
                val endX = (endLandmark.position.x / imageWidth) * canvas.width
                val endY = (endLandmark.position.y / imageHeight) * canvas.height
                
                canvas.drawLine(startX, startY, endX, endY, humanSkeletonPaint)
            }
        }
    }
    
    /**
     * Draw cat skeletal structure
     */
    private fun drawCatSkeleton(canvas: Canvas, subject: TrackedSubject, imageWidth: Int, imageHeight: Int) {
        val landmarks = subject.animalPoseLandmarks ?: return
        
        // Draw the landmarks
        landmarks.forEach { landmark ->
            val position = landmark.position
            val normalizedX = position.x / imageWidth
            val normalizedY = position.y / imageHeight
            val x = normalizedX * canvas.width
            val y = normalizedY * canvas.height
            val radius = 8f + (landmark.confidence * 5f)
            
            canvas.drawCircle(x, y, radius, landmarkPaint)
        }
        
        // Draw the connections between landmarks
        val connections = getCatConnections()
        connections.forEach { connection ->
            val startLandmark = landmarks.find { it.type == connection.first }
            val endLandmark = landmarks.find { it.type == connection.second }
            
            if (startLandmark != null && endLandmark != null &&
                startLandmark.confidence > 0.5f && endLandmark.confidence > 0.5f) {
                val startX = (startLandmark.position.x / imageWidth) * canvas.width
                val startY = (startLandmark.position.y / imageHeight) * canvas.height
                val endX = (endLandmark.position.x / imageWidth) * canvas.width
                val endY = (endLandmark.position.y / imageHeight) * canvas.height
                
                canvas.drawLine(startX, startY, endX, endY, catSkeletonPaint)
            }
        }
    }
    
    /**
     * Draw dog skeletal structure
     */
    private fun drawDogSkeleton(canvas: Canvas, subject: TrackedSubject, imageWidth: Int, imageHeight: Int) {
        val landmarks = subject.animalPoseLandmarks ?: return
        
        // Draw the landmarks
        landmarks.forEach { landmark ->
            val position = landmark.position
            val normalizedX = position.x / imageWidth
            val normalizedY = position.y / imageHeight
            val x = normalizedX * canvas.width
            val y = normalizedY * canvas.height
            val radius = 8f + (landmark.confidence * 5f)
            
            canvas.drawCircle(x, y, radius, landmarkPaint)
        }
        
        // Draw the connections between landmarks
        val connections = getDogConnections()
        connections.forEach { connection ->
            val startLandmark = landmarks.find { it.type == connection.first }
            val endLandmark = landmarks.find { it.type == connection.second }
            
            if (startLandmark != null && endLandmark != null &&
                startLandmark.confidence > 0.5f && endLandmark.confidence > 0.5f) {
                val startX = (startLandmark.position.x / imageWidth) * canvas.width
                val startY = (startLandmark.position.y / imageHeight) * canvas.height
                val endX = (endLandmark.position.x / imageWidth) * canvas.width
                val endY = (endLandmark.position.y / imageHeight) * canvas.height
                
                canvas.drawLine(startX, startY, endX, endY, dogSkeletonPaint)
            }
        }
    }
    
    /**
     * Draw bounding box and identifying label for a subject
     */
    private fun drawBoundingBox(canvas: Canvas, subject: TrackedSubject, imageWidth: Int, imageHeight: Int) {
        val box = subject.boundingBox
        val left = (box.left / imageWidth) * canvas.width
        val top = (box.top / imageHeight) * canvas.height
        val right = (box.right / imageWidth) * canvas.width
        val bottom = (box.bottom / imageHeight) * canvas.height
        
        // Set color based on subject type
        boundingBoxPaint.color = when (subject.type) {
            SubjectType.HUMAN -> Color.GREEN
            SubjectType.CAT -> Color.BLUE
            SubjectType.DOG -> Color.RED
        }
        
        // Draw bounding box
        canvas.drawRect(left, top, right, bottom, boundingBoxPaint)
        
        // Draw label
        val label = "${subject.type.name} #${subject.id}"
        canvas.drawText(label, left, top - 10, textPaint)
    }
    
    /**
     * Get the connections for human skeletal structure
     * Each pair represents a connection between two landmarks
     */
    private fun getHumanConnections(): List<Pair<Int, Int>> {
        return listOf(
            // Face
            Pair(PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER),
            Pair(PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE),
            Pair(PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER),
            Pair(PoseLandmark.LEFT_EYE_OUTER, PoseLandmark.LEFT_EAR),
            Pair(PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE_INNER),
            Pair(PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EYE),
            Pair(PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_OUTER),
            Pair(PoseLandmark.RIGHT_EYE_OUTER, PoseLandmark.RIGHT_EAR),
            Pair(PoseLandmark.MOUTH_LEFT, PoseLandmark.MOUTH_RIGHT),
            
            // Torso
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
            
            // Left arm
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_THUMB),
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_PINKY),
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_INDEX),
            Pair(PoseLandmark.LEFT_PINKY, PoseLandmark.LEFT_INDEX),
            
            // Right arm
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_THUMB),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_PINKY),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_INDEX),
            Pair(PoseLandmark.RIGHT_PINKY, PoseLandmark.RIGHT_INDEX),
            
            // Left leg
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            Pair(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL),
            Pair(PoseLandmark.LEFT_HEEL, PoseLandmark.LEFT_FOOT_INDEX),
            Pair(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_FOOT_INDEX),
            
            // Right leg
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
            Pair(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL),
            Pair(PoseLandmark.RIGHT_HEEL, PoseLandmark.RIGHT_FOOT_INDEX),
            Pair(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_FOOT_INDEX)
        )
    }
    
    /**
     * Get the connections for cat skeletal structure
     * Each pair represents a connection between two landmark types
     */
    private fun getCatConnections(): List<Pair<Int, Int>> {
        // Cat skeletal keypoint definitions
        val NOSE = 0
        val LEFT_EYE = 1
        val RIGHT_EYE = 2
        val LEFT_EAR = 3
        val RIGHT_EAR = 4
        val NECK = 5
        val WITHERS = 6
        val MID_BACK = 7
        val ROOT_TAIL = 8
        val TAIL_TIP = 9
        val LEFT_FRONT_PAW = 10
        val RIGHT_FRONT_PAW = 11
        val LEFT_FRONT_ELBOW = 12
        val RIGHT_FRONT_ELBOW = 13
        val LEFT_BACK_PAW = 14
        val RIGHT_BACK_PAW = 15
        val LEFT_BACK_KNEE = 16
        val RIGHT_BACK_KNEE = 17
        
        return listOf(
            // Head
            Pair(NOSE, LEFT_EYE),
            Pair(NOSE, RIGHT_EYE),
            Pair(LEFT_EYE, LEFT_EAR),
            Pair(RIGHT_EYE, RIGHT_EAR),
            Pair(NOSE, NECK),
            
            // Spine
            Pair(NECK, WITHERS),
            Pair(WITHERS, MID_BACK),
            Pair(MID_BACK, ROOT_TAIL),
            Pair(ROOT_TAIL, TAIL_TIP),
            
            // Front legs
            Pair(WITHERS, LEFT_FRONT_ELBOW),
            Pair(WITHERS, RIGHT_FRONT_ELBOW),
            Pair(LEFT_FRONT_ELBOW, LEFT_FRONT_PAW),
            Pair(RIGHT_FRONT_ELBOW, RIGHT_FRONT_PAW),
            
            // Back legs
            Pair(ROOT_TAIL, LEFT_BACK_KNEE),
            Pair(ROOT_TAIL, RIGHT_BACK_KNEE),
            Pair(LEFT_BACK_KNEE, LEFT_BACK_PAW),
            Pair(RIGHT_BACK_KNEE, RIGHT_BACK_PAW)
        )
    }
    
    /**
     * Get the connections for dog skeletal structure
     * Each pair represents a connection between two landmark types
     */
    private fun getDogConnections(): List<Pair<Int, Int>> {
        // Dog skeletal keypoint definitions
        val NOSE = 0
        val LEFT_EYE = 1
        val RIGHT_EYE = 2
        val LEFT_EAR = 3
        val RIGHT_EAR = 4
        val THROAT = 5
        val WITHERS = 6
        val MID_BACK = 7
        val ROOT_TAIL = 8
        val MID_TAIL = 9
        val TAIL_TIP = 10
        val LEFT_FRONT_PAW = 11
        val RIGHT_FRONT_PAW = 12
        val LEFT_FRONT_ELBOW = 13
        val RIGHT_FRONT_ELBOW = 14
        val LEFT_BACK_PAW = 15
        val RIGHT_BACK_PAW = 16
        val LEFT_BACK_KNEE = 17
        val RIGHT_BACK_KNEE = 18
        val CHEST = 19
        
        return listOf(
            // Head
            Pair(NOSE, LEFT_EYE),
            Pair(NOSE, RIGHT_EYE),
            Pair(LEFT_EYE, LEFT_EAR),
            Pair(RIGHT_EYE, RIGHT_EAR),
            Pair(NOSE, THROAT),
            
            // Spine
            Pair(THROAT, WITHERS),
            Pair(WITHERS, MID_BACK),
            Pair(MID_BACK, ROOT_TAIL),
            Pair(ROOT_TAIL, MID_TAIL),
            Pair(MID_TAIL, TAIL_TIP),
            
            // Chest
            Pair(THROAT, CHEST),
            Pair(CHEST, WITHERS),
            
            // Front legs
            Pair(WITHERS, LEFT_FRONT_ELBOW),
            Pair(WITHERS, RIGHT_FRONT_ELBOW),
            Pair(LEFT_FRONT_ELBOW, LEFT_FRONT_PAW),
            Pair(RIGHT_FRONT_ELBOW, RIGHT_FRONT_PAW),
            
            // Back legs
            Pair(ROOT_TAIL, LEFT_BACK_KNEE),
            Pair(ROOT_TAIL, RIGHT_BACK_KNEE),
            Pair(LEFT_BACK_KNEE, LEFT_BACK_PAW),
            Pair(RIGHT_BACK_KNEE, RIGHT_BACK_PAW)
        )
    }
    
    /**
     * Create a bitmap with visualizations drawn on it
     */
    fun createVisualizationOverlay(width: Int, height: Int, subjects: List<TrackedSubject>, imageWidth: Int, imageHeight: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        drawTrackedSubjects(canvas, subjects, imageWidth, imageHeight)
        
        return bitmap
    }
}

/**
 * Usage example:
 * 
 * val renderer = SkeletalVisualizationRenderer(context)
 * val overlayBitmap = renderer.createVisualizationOverlay(
 *     width = previewView.width,
 *     height = previewView.height,
 *     subjects = trackedSubjects,
 *     imageWidth = frameMetadata.width,
 *     imageHeight = frameMetadata.height
 * )
 * overlayImageView.setImageBitmap(overlayBitmap)
 */