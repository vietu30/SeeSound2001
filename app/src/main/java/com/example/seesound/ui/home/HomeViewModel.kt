package com.example.seesound.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seesound.data.repository.NoiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.log10

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NoiseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _dbLevel = MutableStateFlow(0.0)
    val dbLevel: StateFlow<Double> = _dbLevel.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var monitoringJob: Job? = null

    fun checkPermission() {
        _hasPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(granted: Boolean) {
        _hasPermission.value = granted
    }

    fun toggleMonitoring() {
        if (_isMonitoring.value) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        if (!_hasPermission.value) return

        try {
            val outputFile = File(context.cacheDir, "temp_audio.3gp")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            _isMonitoring.value = true

            monitoringJob = viewModelScope.launch {
                while (isActive && _isMonitoring.value) {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    if (amplitude > 0) {
                        // Convert amplitude to dB (approximate)
                        // dB = 20 * log10(amplitude / reference)
                        // Using amplitude directly as reference is 1
                        val db = 20 * log10(amplitude.toDouble())
                        _dbLevel.value = db.coerceIn(0.0, 140.0)
                    }
                    delay(100) // Update every 100ms
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isMonitoring.value = false
        }
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        _isMonitoring.value = false
    }

    fun saveCurrentReading() {
        val currentDb = _dbLevel.value
        if (currentDb > 0) {
            viewModelScope.launch {
                repository.saveReading(currentDb)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
