package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 手写签名板 —— Path 演示。
 *
 *   · moveTo → quadTo：用二次贝塞尔平滑连接采样点，避免折线感
 *   · 每一笔完成后加到 history，用于 undo
 *   · 清空 = history.clear() + invalidate
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F2933")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val history = mutableListOf<Path>()
    private var currentPath: Path? = null
    private var lastX = 0f
    private var lastY = 0f

    override fun onDraw(canvas: Canvas) {
        history.forEach { canvas.drawPath(it, strokePaint) }
        currentPath?.let { canvas.drawPath(it, strokePaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply { moveTo(x, y) }
                lastX = x; lastY = y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                /// quadTo(control, end) 用上一点做控制点做二次贝塞尔
                currentPath?.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentPath?.let { history += it }
                currentPath = null
            }
        }
        invalidate()
        return true
    }

    fun undo() {
        if (history.isNotEmpty()) {
            history.removeAt(history.lastIndex)
            invalidate()
        }
    }

    fun clear() {
        history.clear()
        currentPath = null
        invalidate()
    }
}
