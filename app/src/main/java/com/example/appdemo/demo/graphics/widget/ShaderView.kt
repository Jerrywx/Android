package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

/**
 * Shader 三种渐变对比 —— 一次画在同一 View 上：
 *
 *   · 上：LinearGradient 横向渐变胶囊
 *   · 中：SweepGradient 扫描色环
 *   · 下：RadialGradient 中心辐射圆
 */
class ShaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#607D8B")
        textSize = 32f
    }
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val cellH = height / 3f

        /// ─── LinearGradient
        rect.set(40f, 40f, w - 40f, cellH - 40f)
        paint.shader = LinearGradient(
            rect.left, 0f, rect.right, 0f,
            intArrayOf(Color.parseColor("#FF6E40"), Color.parseColor("#FFC400"), Color.parseColor("#00B0FF")),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(rect, cellH / 2, cellH / 2, paint)
        drawLabel(canvas, "LinearGradient", rect)

        /// ─── SweepGradient
        val cx = w / 2f
        val cy = cellH * 1.5f
        val r = cellH / 2f - 40f
        paint.shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.RED, Color.parseColor("#FFEB3B"), Color.GREEN,
                Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED,
            ),
            null,
        )
        canvas.drawCircle(cx, cy, r, paint)
        rect.set(cx - r, cellH + 40f, cx + r, cellH * 2 - 40f)
        drawLabel(canvas, "SweepGradient", rect)

        /// ─── RadialGradient
        val cy2 = cellH * 2.5f
        paint.shader = RadialGradient(
            cx, cy2, r,
            Color.parseColor("#FFFFFF"), Color.parseColor("#3F51B5"),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy2, r, paint)
        rect.set(cx - r, cellH * 2 + 40f, cx + r, cellH * 3 - 40f)
        drawLabel(canvas, "RadialGradient", rect)
    }

    private fun drawLabel(canvas: Canvas, text: String, rect: RectF) {
        canvas.drawText(text, rect.left, rect.bottom + 44f, labelPaint)
    }
}
