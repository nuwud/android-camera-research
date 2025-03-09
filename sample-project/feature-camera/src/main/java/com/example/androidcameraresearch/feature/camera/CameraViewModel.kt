package com.example.androidcameraresearch.feature.camera

import android.util.Size
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidcameraresearch.core.common.DispatcherProvider
import com.example.androidcameraresearch.core.domain.model.Frame
import com.example.androidcameraresearch.feature.camera.domain.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    // Camera state
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState
    
    // Camera frames
    val frameFlow: SharedFlow<Frame> = cameraManager.frameFlow

    fun startCamera(surfaceProvider: SurfaceRequest.SurfaceProvider) {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                _cameraState.value = CameraState.Starting
                
                // Configure camera
                val config = CameraConfig(
                    targetResolution = Size(1280, 720),
                    fps = 30
                )
                
                // Start camera with the surface provider
                cameraManager.startCamera(surfaceProvider, config)
                
                _cameraState.value = CameraState.Running
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun stopCamera() {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                cameraManager.stopCamera()
                _cameraState.value = CameraState.Idle
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCamera()
    }

    // Camera states
    sealed class CameraState {
        object Idle : CameraState()
        object Starting : CameraState()
        object Running : CameraState()
        data class Error(val message: String) : CameraState()
    }
}

// Camera configuration data class
data class CameraConfig(
    val targetResolution: Size,
    val fps: Int
)
