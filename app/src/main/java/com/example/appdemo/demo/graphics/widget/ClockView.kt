package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Canvas 基础演示 —— 一个走时的表盘。
 *
 * 涵盖：
 *   1) Paint 的 style / strokeWidth / antiAlias
 *   2) Canvas.save / rotate / restore 变换栈
 *   3) drawCircle / drawLine / drawText
 *   4) postInvalidateDelayed 定时重绘
 */
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEEEEE")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#777777")
        strokeWidth = 3f
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }
    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
    }
    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935")
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935")
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 20f

        /// 表盘外圈
        canvas.drawCircle(cx, cy, radius, dialPaint)

        /// 12 个刻度 + 数字
        canvas.save()
        canvas.translate(cx, cy)
        for (i in 0 until 12) {
            canvas.save()
            canvas.rotate(i * 30f)
            canvas.drawLine(0f, -radius + 8, 0f, -radius + 28, tickPaint)
            canvas.restore()
        }
        /// 12 个数字（不跟随旋转，另算坐标）
        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val nx = ((radius - 55) * cos(angle)).toFloat()
            val ny = ((radius - 55) * sin(angle)).toFloat() + 10f
            canvas.drawText(i.toString(), nx, ny, numberPaint)
        }

        val cal = Calendar.getInstance()
        val second = cal.get(Calendar.SECOND)
        val minute = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR) + minute / 60f

        /// 时针
        canvas.save()
        canvas.rotate(hour * 30)
        canvas.drawLine(0f, 20f, 0f, -radius * 0.5f, hourPaint)
        canvas.restore()

        /// 分针
        canvas.save()
        canvas.rotate(minute * 6f)
        canvas.drawLine(0f, 20f, 0f, -radius * 0.7f, minutePaint)
        canvas.restore()

        /// 秒针
        canvas.save()
        canvas.rotate(second * 6f)
        canvas.drawLine(0f, 30f, 0f, -radius * 0.85f, secondPaint)
        canvas.restore()

        canvas.drawCircle(0f, 0f, 10f, centerPaint)
        canvas.restore()

        /// 每秒重绘一次
        postInvalidateDelayed(1000L - (System.currentTimeMillis() % 1000))
    }
}
