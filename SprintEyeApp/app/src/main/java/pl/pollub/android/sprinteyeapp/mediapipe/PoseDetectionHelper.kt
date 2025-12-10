package pl.pollub.android.sprinteyeapp.mediapipe

import android.content.Context
import android.graphics.PointF
import android.media.metrics.PlaybackErrorEvent.ERROR_OTHER
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult


class PoseDetectionHelper(private val context: Context, private val listener: DetectionListener) {


    interface DetectionListener{
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


}