package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 刮刮卡 —— PorterDuff 演示。
 *
 * 关键：
 *   · 用一张 mask Bitmap 作为遮罩，初始整块灰色
 *   · 手指划过用 CLEAR 模式擦除 mask 上的像素
 *   · onDraw 先画底层文字，再把 mask 盖上，露出被擦部分
 */
class ScratchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 60f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    private val prizePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D84315")
        textSize = 72f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var maskBitmap: Bitmap? = null
    private var maskCanvas: Canvas? = null
    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        maskCanvas = Canvas(maskBitmap!!).apply {
            drawRect(0f, 0f, w.toFloat(), h.toFloat(), coverPaint)
            drawText("刮开有惊喜", w / 2f, h / 2f + 18f, hintPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        /// 底层：奖品文字
        canvas.drawText("🎉 iPhone 一部", width / 2f, height / 2f + 24f, prizePaint)
        /// 上层：mask bitmap（被 CLEAR 擦出的地方透明）
        maskBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(x, y)
                lastX = x; lastY = y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                maskCanvas?.drawPath(path, maskPaint)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> path.reset()
        }
        invalidate()
        return true
    }

    fun reset() {
        val w = width; val h = height
        if (w <= 0 || h <= 0) return
        maskCanvas?.apply {
            /// SRC 模式重新画一层不透明覆盖
            val clearPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                color = Color.parseColor("#B0BEC5")
            }
            drawRect(0f, 0f, w.toFloat(), h.toFloat(), clearPaint)
            drawText("刮开有惊喜", w / 2f, h / 2f + 18f, hintPaint)
        }
        invalidate()
    }
}
