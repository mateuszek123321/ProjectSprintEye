package pl.pollub.android.sprinteyeapp.mediapipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max

class DetectionOverlay (context: Context, attrs: AttributeSet?=null) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = LANDMARK_STROKE_WIDTH
    }

    private var scaleFactor: Float = 1f
    private var imageWidth = 1
    private var imageHeight = 1
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val res = results ?: return

        for(landmark in res.landmarks()){
            // points
            for(point in landmark){
                val x = point.x() * imageWidth * scaleFactor
                val y = point.y() * imageHeight * scaleFactor
                canvas.drawPoint(x, y, pointPaint)
            }
            //leftshoulder
            val ls = landmark[11]
            //rightshoulder
            val rs = landmark[12]
            //lefthip
            val lh = landmark[23]
            //righthip
            val rh = landmark[24]
            //rightelbow
            val re = landmark[14]
            //leftelbow
            val le = landmark[13]
            //shoulder to shoulder
            // canvas.drawLine( leftshoulder.x() * imageWidth * scaleFactor,
            // leftshoulder.y() * imageHeight * scaleFactor,
            // rightshoulder.x() * imageWidth * scaleFactor,
            // rightshoulder.y() * imageHeight * scaleFactor,
            // linePaint )
            fun fx(v: Float) = v * imageWidth * scaleFactor
            fun fy(v: Float) = v * imageHeight * scaleFactor

            //shoulder-shoulder
            canvas.drawLine(fx(ls.x()), fy(ls.y()), fx(rs.x()), fy(rs.y()), linePaint)
            //left shoulder - left hip
            canvas.drawLine(fx(ls.x()), fy(ls.y()), fx(lh.x()), fy(lh.y()), linePaint)
            //right shoulder - right hip
            canvas.drawLine(fx(rs.x()), fy(rs.y()), fx(rh.x()), fy(rh.y()), linePaint)
            //hip-hip
            canvas.drawLine(fx(lh.x()), fy(lh.y()), fx(rh.x()), fy(rh.y()), linePaint)
            //left shoulder - left elbow
            canvas.drawLine(fx(ls.x()), fy(ls.y()), fx(le.x()), fy(le.y()), linePaint)
            //right shoulder - right elbow
            canvas.drawLine(fx(rs.x()), fy(rs.y()), fx(re.x()), fy(re.y()), linePaint)

        }
    }
    fun setResults(
        poseLandmarkerResult: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ){
        results = poseLandmarkerResult

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        //for center
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
        val drawnW = imageWidth * scaleFactor
        val drawnH = imageHeight * scaleFactor

        postInvalidateOnAnimation()
    }
    fun clear() {
        results = null
        postInvalidateOnAnimation()

    }
    companion object {
        private const val LANDMARK_STROKE_WIDTH = 30F
    }
}