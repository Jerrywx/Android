package com.example.appdemo.demo.animation

import com.example.appdemo.R
import com.example.appdemo.common.*
import com.example.appdemo.tabs.*
import com.example.appdemo.demo.layout.*
import com.example.appdemo.demo.concurrent.*
import com.example.appdemo.demo.network.*
import com.example.appdemo.demo.animation.*
import com.example.appdemo.demo.animation.widget.*
import com.example.appdemo.demo.viewpager.*
import com.example.appdemo.demo.recyclerview.*
import com.example.appdemo.demo.recyclerview.chat.*
import com.example.appdemo.demo.fragment.*

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 插值器与求值器演示。
 *
 * Interpolator 决定动画进度（0 → 1）的变化曲线；
 * TypeEvaluator 决定属性值如何从起点过渡到终点。
 */
class InterpolatorDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_interpolator)
        setupDemoToolbar(R.string.anim_interpolator, R.id.interp_root)

        val target = findViewById<View>(R.id.interp_target)
        val current = findViewById<TextView>(R.id.tv_current)

        /// 同一段动画，切换不同 Interpolator
        fun play(name: String, interp: Interpolator) {
            current.text = "当前：$name"
            target.translationX = 0f
            ObjectAnimator.ofFloat(target, "translationX", 0f, 700f).apply {
                duration = 1500
                interpolator = interp
                start()
            }
        }

        findViewById<TextView>(R.id.btn_linear).setOnClickListener {
            play("Linear 匀速", LinearInterpolator())
        }
        findViewById<TextView>(R.id.btn_accelerate).setOnClickListener {
            play("Accelerate 加速", AccelerateInterpolator())
        }
        findViewById<TextView>(R.id.btn_decelerate).setOnClickListener {
            play("Decelerate 减速", DecelerateInterpolator())
        }
        findViewById<TextView>(R.id.btn_ad).setOnClickListener {
            play("AccelerateDecelerate 先加后减", AccelerateDecelerateInterpolator())
        }
        findViewById<TextView>(R.id.btn_overshoot).setOnClickListener {
            play("Overshoot 过冲回弹", OvershootInterpolator(2.5f))
        }
        findViewById<TextView>(R.id.btn_anticipate).setOnClickListener {
            play("Anticipate 起步回拉", AnticipateInterpolator(2f))
        }
        findViewById<TextView>(R.id.btn_bounce).setOnClickListener {
            play("Bounce 末段弹跳", BounceInterpolator())
        }
        findViewById<TextView>(R.id.btn_aod).setOnClickListener {
            play("AnticipateOvershoot 双向", AnticipateOvershootInterpolator())
        }
        /// PathInterpolator 通过贝塞尔曲线自定义任意缓动
        findViewById<TextView>(R.id.btn_path).setOnClickListener {
            val path = Path().apply {
                moveTo(0f, 0f)
                cubicTo(0.2f, 0f, 0.1f, 1.2f, 0.5f, 1f)
                cubicTo(0.7f, 0.9f, 0.9f, 1f, 1f, 1f)
            }
            play("PathInterpolator 自定义", PathInterpolator(path))
        }

        /// TypeEvaluator —— 自定义 Point 求值器，让小球走抛物线
        val ball = findViewById<View>(R.id.parabola_target)
        findViewById<TextView>(R.id.btn_parabola).setOnClickListener {
            val start = PointF(0f, 0f)
            val end = PointF(800f, 0f)
            ValueAnimator.ofObject(ParabolaEvaluator(peakHeight = -300f), start, end).apply {
                duration = 1500
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    val p = it.animatedValue as PointF
                    ball.translationX = p.x
                    ball.translationY = p.y
                }
                start()
            }
        }
    }

    /// 抛物线插值器：在两点之间走 y = 4h·t·(1-t) 的曲线
    private class ParabolaEvaluator(private val peakHeight: Float) : TypeEvaluator<PointF> {
        private val out = PointF()
        override fun evaluate(t: Float, start: PointF, end: PointF): PointF {
            out.x = start.x + (end.x - start.x) * t
            val baseY = start.y + (end.y - start.y) * t
            out.y = baseY + 4f * peakHeight * t * (1f - t)
            return out
        }
    }
}
