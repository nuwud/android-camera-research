package com.example.androidcameraresearch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.androidcameraresearch.databinding.ActivityMainBinding
import com.example.androidcameraresearch.feature.camera.CameraViewModel
import com.example.androidcameraresearch.feature.mlkit.PoseDetectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraViewModel: CameraViewModel by viewModels()
    private val poseDetectionViewModel: PoseDetectionViewModel by viewModels()
    
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check permissions
        if (hasRequiredPermissions()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up UI
        setupUI()
        
        // Observe pose detection results
        observePoseDetection()
    }

    private fun setupUI() {
        binding.controlsContainer.apply {
            // Toggle pose detection
            togglePoseDetection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    poseDetectionViewModel.startPoseDetection()
                } else {
                    poseDetectionViewModel.stopPoseDetection()
                }
            }
            
            // Toggle AR overlay
            toggleAr.setOnCheckedChangeListener { _, isChecked ->
                binding.arOverlay.visibility = if (isChecked) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }
    }

    private fun observePoseDetection() {
        lifecycleScope.launch {
            poseDetectionViewModel.poseResults.collectLatest { poseResult ->
                binding.poseOverlay.updatePose(poseResult)
            }
        }
        
        // Observe state (processing, errors, etc)
        lifecycleScope.launch {
            poseDetectionViewModel.state.collectLatest { state ->
                when (state) {
                    is PoseDetectionViewModel.State.Processing -> {
                        binding.processingIndicator.visibility = android.view.View.VISIBLE
                    }
                    is PoseDetectionViewModel.State.Idle -> {
                        binding.processingIndicator.visibility = android.view.View.GONE
                    }
                    is PoseDetectionViewModel.State.Error -> {
                        Toast.makeText(this@MainActivity, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun startCamera() {
        // Get the surface from the TextureView
        val surfaceProvider = binding.viewFinder.surfaceProvider
        
        // Start camera
        cameraViewModel.startCamera(surfaceProvider)
        
        // Connect camera to pose detection
        cameraViewModel.frameFlow.let { frameFlow ->
            poseDetectionViewModel.setFrameSource(frameFlow)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (hasRequiredPermissions()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewModel.stopCamera()
        poseDetectionViewModel.stopPoseDetection()
    }
}
