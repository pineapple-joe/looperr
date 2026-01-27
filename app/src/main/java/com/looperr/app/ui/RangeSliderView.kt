package com.looperr.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.looperr.app.R

class RangeSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnRangeChangeListener {
        fun onRangeChanged(startPercent: Float, endPercent: Float)
        fun onRangeChangeFinished(startPercent: Float, endPercent: Float)
    }

    var listener: OnRangeChangeListener? = null

    private var startPercent = 0f
    private var endPercent = 1f
    private var currentPositionPercent = 0f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.track_inactive)
        style = Paint.Style.FILL
    }

    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.spotify_green)
        style = Paint.Style.FILL
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 2f, 0x40000000)
    }

    private val positionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
    }

    private val trackRect = RectF()
    private val selectedRect = RectF()

    private val trackHeight = 16f.dp
    private val handleRadius = 14f.dp
    private val positionIndicatorRadius = 6f.dp

    private var activeHandle: Handle? = null

    private enum class Handle { START, END }

    private val Float.dp: Float
        get() = this * context.resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = handleRadius
        val trackTop = (height - trackHeight) / 2
        val trackBottom = trackTop + trackHeight

        // Draw inactive track
        trackRect.set(padding, trackTop, width - padding, trackBottom)
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, trackPaint)

        // Draw selected range
        val startX = padding + (width - 2 * padding) * startPercent
        val endX = padding + (width - 2 * padding) * endPercent
        selectedRect.set(startX, trackTop, endX, trackBottom)
        canvas.drawRoundRect(selectedRect, trackHeight / 2, trackHeight / 2, selectedPaint)

        // Draw current position indicator
        val posX = padding + (width - 2 * padding) * currentPositionPercent
        canvas.drawCircle(posX, height / 2f, positionIndicatorRadius, positionPaint)

        // Draw handles
        canvas.drawCircle(startX, height / 2f, handleRadius, handlePaint)
        canvas.drawCircle(endX, height / 2f, handleRadius, handlePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val padding = handleRadius
        val touchX = event.x.coerceIn(padding, width - padding)
        val percent = (touchX - padding) / (width - 2 * padding)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val startX = padding + (width - 2 * padding) * startPercent
                val endX = padding + (width - 2 * padding) * endPercent

                val distToStart = kotlin.math.abs(event.x - startX)
                val distToEnd = kotlin.math.abs(event.x - endX)

                activeHandle = when {
                    distToStart < handleRadius * 2 && distToStart <= distToEnd -> Handle.START
                    distToEnd < handleRadius * 2 -> Handle.END
                    else -> null
                }
                return activeHandle != null
            }

            MotionEvent.ACTION_MOVE -> {
                when (activeHandle) {
                    Handle.START -> {
                        startPercent = percent.coerceIn(0f, endPercent - 0.02f)
                        listener?.onRangeChanged(startPercent, endPercent)
                    }
                    Handle.END -> {
                        endPercent = percent.coerceIn(startPercent + 0.02f, 1f)
                        listener?.onRangeChanged(startPercent, endPercent)
                    }
                    null -> {}
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (activeHandle != null) {
                    listener?.onRangeChangeFinished(startPercent, endPercent)
                    activeHandle = null
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRange(start: Float, end: Float) {
        startPercent = start.coerceIn(0f, 1f)
        endPercent = end.coerceIn(0f, 1f)
        invalidate()
    }

    fun setCurrentPosition(percent: Float) {
        currentPositionPercent = percent.coerceIn(0f, 1f)
        invalidate()
    }

    fun getStartPercent() = startPercent
    fun getEndPercent() = endPercent
}
