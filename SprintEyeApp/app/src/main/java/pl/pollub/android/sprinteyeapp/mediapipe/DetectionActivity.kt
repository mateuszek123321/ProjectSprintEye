package pl.pollub.android.sprinteyeapp.mediapipe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import pl.pollub.android.sprinteyeapp.R
import java.util.concurrent.ExecutorService

import android.graphics.*
import android.media.Image
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.View
import androidx.annotation.OptIn
import androidx.annotation.VisibleForTesting
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class DetectionActivity : AppCompatActivity() {
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE

    val detectionHelperListener: LandmarkerListener? = null

    private lateinit var previewView: PreviewView
    private lateinit var overlay: DetectionOverlay
    private lateinit var tvStatus: TextView
    private lateinit var executor: ExecutorService
    private var poseLandmarker: PoseLandmarker? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        // ?
        // setContentView(R.layout.activity_detection)

        previewView = findViewById(R.id.prevView)
        overlay = findViewById(R.id.overlay)
        tvStatus = findViewById(androidx.camera.core.R.id.tvStatus)

        executor = Executors.newSingleThreadExecutor()

        setupPoseLandmarker()
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        poseLandmarker?.close()
        executor.shutdown()
    }

    private fun setupPoseLandmarker() {
        // Konfiguracja base options
        val baseOptionBuilder = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .setDelegate(Delegate.CPU)

        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                .setMinTrackingConfidence(minPoseTrackingConfidence)
                .setMinPosePresenceConfidence(minPosePresenceConfidence)
                .setResultListener { result, input -> onPoseResult(result, input) }

            poseLandmarker = PoseLandmarker.createFromOptions(this, optionsBuilder.build())

        } catch (e: IllegalStateException) {
            detectionHelperListener?.onError(
                "Pose Landmarker failed to initialize. See logs for details"
            )
            Log.e(TAG, "MediaPipe failed to load task: ${e.message}", e)
        } catch (e: RuntimeException) {
            detectionHelperListener?.onError(
                "Pose Landmarker failed to initialize. See logs for details", GPU_ERROR
            )
            Log.e(TAG, "PoseLandmarker runtime error: ${e.message}", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            //from android developers
            //use ResolutionSelector with AspectRatioStrategy to
            // specify the preferred aspect ratio settings instead.
            val selector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(ResolutionStrategy(Size(1280, 720),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                .build()

            val preview = Preview.Builder()
                .setResolutionSelector(selector)
                .build().apply { setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(selector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { imageProxy ->
                try {
                    val img = imageProxy.image ?: return@Analyzer
                    val bmp = img.toBitmapFast()
                    val rotated = bmp.rotate(imageProxy.imageInfo.rotationDegrees)
                    val mpImage = BitmapImageBuilder(rotated).build()
                    val frameTime = SystemClock.uptimeMillis()
                    //LIVE_STREAM - monotoniczny timestamp
                    detectAsync(mpImage, frameTime)
                } catch (e: Exception) {
                    Log.e(TAG, "Analyzer error: ${e.message}", e)
                } finally {
                    imageProxy.close()
                }
            })

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmarker?.detectAsync(mpImage, frameTime)
    }

    // wynik z PoseLandmarker (LIVE_STREAM)
    private fun onPoseResult(result: PoseLandmarkerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        // Jeśli ktoś podłączy listener z zewnątrz —  ResultBundle
        detectionHelperListener?.onResults(
            ResultBundle(
                results = listOf(result),
                inferenceTime = inferenceTime,
                inputImageHeight = input.height,
                inputImageWidth = input.width
            )
        )

        // UI: status + overlay
        runOnUiThread {
            val hasPose = result.landmarks().isNotEmpty()
            tvStatus.visibility = if (hasPose) View.VISIBLE else View.GONE
            if (hasPose) tvStatus.text = "WYKRYTO SYLWETKĘ (${inferenceTime} ms)"
            (if (hasPose) result else null)?.let {
                overlay.setResults(it, input.height, input.width)
            } ?: overlay.clear()
        }
    }

    // -----------------------------------------
    // Konwersja YUV_420_888 -> Bitmap (NV21 -> JPEG), kompatybilna i prosta
    // -----------------------------------------
    private fun Image.toBitmapFast(): Bitmap {
        val y = planes[0].buffer
        val u = planes[1].buffer
        val v = planes[2].buffer

        val ySize = y.remaining()
        val uSize = u.remaining()
        val vSize = v.remaining()

        // NV21 = Y + V + U
        val nv21 = ByteArray(ySize + uSize + vSize)
        y.get(nv21, 0, ySize)
        v.get(nv21, ySize, vSize)
        u.get(nv21, ySize + vSize, uSize)

        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, width, height), 90, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun Bitmap.rotate(deg: Int): Bitmap {
        if (deg == 0) return this
        val m = Matrix().apply { postRotate(deg.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
    }

    // ---- lokalne typy/konstanty przeniesione z Helpera ----
    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }

    companion object {
        const val TAG = "PoseLandmarkerHelper"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_POSES = 1
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        const val MODEL_POSE_LANDMARKER_FULL = 0
        const val MODEL_POSE_LANDMARKER_LITE = 1
        const val MODEL_POSE_LANDMARKER_HEAVY = 2
    }
}