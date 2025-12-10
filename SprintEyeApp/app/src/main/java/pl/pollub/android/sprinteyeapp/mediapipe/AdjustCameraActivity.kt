package pl.pollub.android.sprinteyeapp.mediapipe

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.Range
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import pl.pollub.android.sprinteyeapp.databinding.AdjustCameraActivityBinding
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity.Companion.EXTRA_SESSION_MODE
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity.Companion.SESSION_MODE_ADVANCED
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity.Companion.SESSION_MODE_BASIC
import pl.pollub.android.sprinteyeapp.view.AdvancedSessionCamera
import pl.pollub.android.sprinteyeapp.view.BasicSessionCamera

class AdjustCameraActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView

    private lateinit var binding: AdjustCameraActivityBinding
    private var sessionMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdjustCameraActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionMode = intent.getStringExtra(EXTRA_SESSION_MODE)

        previewView = binding.previewView
        startCameraPreview()
        confirmCameraSetting()
    }

    private fun startCameraPreview(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            //Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(ResolutionStrategy(
                    Size(1280, 720),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                ).build()

            // Preview
            val preview = Preview.Builder()
                .setTargetFrameRate(Range(30, 60))
                .setResolutionSelector(resolutionSelector)
                .build().also{
                    it.surfaceProvider = previewView.surfaceProvider
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun confirmCameraSetting(){
        binding.confirmCamerPositon.setOnClickListener{
            when(sessionMode){
                SESSION_MODE_BASIC -> {
                    val intent = Intent(this, BasicSessionCamera::class.java)
                    startActivity(intent)
                    finish()
                }
                SESSION_MODE_ADVANCED -> {
                    val intent = Intent(this, AdvancedSessionCamera::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Błąd trybu sesji", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

}