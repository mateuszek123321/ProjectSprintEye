package pl.pollub.android.sprinteyeapp.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.os.SystemClock
import androidx.annotation.OptIn


class PoseDetectionHelper(private val context: Context, private val listener: DetectionListener) {
    interface DetectionListener {
        fun onPoseDetected(result: PoseLandmarkerResult, inferenceTime: Long)

        fun onPoseLost()

        fun onFinishLineCrossed(timeStamp: Long, crossingData: CrossingData)

        fun onError(error: String, errorCode: Int = ERROR_OTHER)
    }

    data class CrossingData(
        val timeStamp: Long,
        val landmarkIndices: List<Int>,
        val positions: List<PointF>
    )

    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE

    var finishLineX: Float = 0.75f

    @Volatile
    var isDetectionEnabled: Boolean = false

    var detectionDelays: Long = 1500L

    private var lastCrossingTime: Long = 0L

    private var lastChestCenterXPx: Float? = null

    private var poseLandmarker: PoseLandmarker? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var overlay: DetectionOverlay? = null

    fun setupPoseLandmarker() {
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
                .setErrorListener { error ->
                    listener.onError("Pose detection error: ${error.message}", ERROR_DETECTION)
                }

            poseLandmarker = PoseLandmarker.createFromOptions(context, optionsBuilder.build())
            Log.d(TAG, "PoseLandmarker initialized successfully")

        } catch (e: IllegalStateException) {
            listener.onError(
                "Pose Landmarker failed to initialize: ${e.message}",
                ERROR_INITIALIZATION
            )
            Log.e(TAG, "MediaPipe failed to load task", e)
        } catch (e: RuntimeException) {
            listener.onError("Pose Landmarker runtime error: ${e.message}", ERROR_GPU)
            Log.e(TAG, "PoseLandmarker runtime error", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        overlay: DetectionOverlay
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val selector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()

            val preview = Preview.Builder()
                .setResolutionSelector(selector)
                .build().apply { setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(selector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                try {
                    val img = imageProxy.image ?: return@setAnalyzer

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
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                listener.onError("Failed to start camera: ${e.message}", ERROR_CAMERA)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmarker?.detectAsync(mpImage, frameTime)
    }


    private fun onPoseResult(result: PoseLandmarkerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        if (result.landmarks().isEmpty()) {
            lastChestCenterXPx = null
            overlay?.clear()
            listener.onPoseLost()
            return
        }

        // Notify pose detected
        listener.onPoseDetected(result, inferenceTime)
        overlay?.setResults(
            result,
            imageHeight = input.height,
            imageWidth = input.width,
            runningMode = RunningMode.LIVE_STREAM
        )

        checkFinishLineCrossing(result, input.width, input.height)
    }

    private fun checkFinishLineCrossing(
        result: PoseLandmarkerResult,
        imageWidth: Int,
        imageHeight: Int
    ) {
        if (!isDetectionEnabled) return
        if (result.landmarks().isEmpty()) return

        val now = SystemClock.uptimeMillis()

        // delay check
        if (now - lastCrossingTime < detectionDelays) return

        val landmarks = result.landmarks()[0]
        val ls = landmarks[11]
        val rs = landmarks[12]

        val chestCenterXPx = (ls.x() + rs.x()) * 0.5f * imageWidth
        val lineXpx = finishLineX * imageWidth

        val previousChestXPx = lastChestCenterXPx
        lastChestCenterXPx = chestCenterXPx

        // Check which landmarks crossed the line
        // Using hips (23, 24) and ankles (27, 28)
        if (previousChestXPx != null &&
            previousChestXPx < lineXpx &&
            chestCenterXPx >= lineXpx
        ) {
            lastCrossingTime = now
            isDetectionEnabled = false  // jedno przekroczenie na jedno „uzbrojenie”

            val positions = listOf(
                PointF(ls.x() * imageWidth, ls.y() * imageHeight),
                PointF(rs.x() * imageWidth, rs.y() * imageHeight)
            )

            val indices = listOf(11, 12)

            listener.onFinishLineCrossed(
                now,
                CrossingData(now, indices, positions)
            )

            Log.d(TAG, "Finish line crossed! Chest center X: $chestCenterXPx")
        }
    }

    fun close() {
        poseLandmarker?.close()
        executor.shutdown()
    }
// Konwersja YUV_420_888 -> Bitmap (NV21 -> JPEG)
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

    companion object {
        private const val TAG = "PoseDetectionHelper"

        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F

        const val ERROR_OTHER = 0
        const val ERROR_GPU = 1
        const val ERROR_INITIALIZATION = 2
        const val ERROR_DETECTION = 3
        const val ERROR_CAMERA = 4
    }
}
