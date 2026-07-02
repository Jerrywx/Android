package com.example.appdemo.demo.graphics.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * SurfaceView 粒子系统 —— 独立线程渲染演示。
 *
 * 要点：
 *   · SurfaceView 有自己的 Surface（Window 之外的独立层）
 *   · lockCanvas / unlockCanvasAndPost 驱动一帧
 *   · surfaceCreated/Destroyed 时启停渲染线程
 *   · burst() 一次爆出一堆粒子；粒子有速度、重力、寿命
 */
class ParticleSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private class Particle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Int,
        val color: Int,
        val radius: Float,
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val palette = intArrayOf(
        Color.parseColor("#FF5252"),
        Color.parseColor("#FFEB3B"),
        Color.parseColor("#40C4FF"),
        Color.parseColor("#69F0AE"),
        Color.parseColor("#B388FF"),
    )

    private var renderThread: Thread? = null
    @Volatile private var running = false

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        renderThread = Thread(::renderLoop).also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        renderThread?.join()
        renderThread = null
    }

    fun burst() {
        val cx = width / 2f
        val cy = height / 2f
        synchronized(particles) {
            repeat(80) {
                val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
                val speed = Random.nextFloat() * 12f + 4f
                particles += Particle(
                    x = cx, y = cy,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = Random.nextInt(60, 120),
                    color = palette.random(),
                    radius = Random.nextFloat() * 6f + 4f,
                )
            }
        }
    }

    fun clearAll() {
        synchronized(particles) { particles.clear() }
    }

    private fun renderLoop() {
        while (running) {
            val canvas: Canvas? = try { holder.lockCanvas() } catch (_: Throwable) { null }
            if (canvas != null) {
                try {
                    canvas.drawColor(Color.parseColor("#0D1B2A"), PorterDuff.Mode.SRC)
                    synchronized(particles) {
                        val it = particles.iterator()
                        while (it.hasNext()) {
                            val p = it.next()
                            p.vy += 0.35f /// 重力
                            p.x += p.vx
                            p.y += p.vy
                            p.life -= 1
                            if (p.life <= 0) { it.remove(); continue }
                            paint.color = p.color
                            paint.alpha = (255 * (p.life / 120f).coerceIn(0f, 1f)).toInt()
                            canvas.drawCircle(p.x, p.y, p.radius, paint)
                        }
                    }
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            try { Thread.sleep(16) } catch (_: InterruptedException) { break } /// ~60fps
        }
    }
}
