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

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * 物理动画演示 —— SpringAnimation / FlingAnimation。
 *
 * 与传统动画的区别：
 *   - 传统动画：固定时长 + 插值器，曲线是预定义的
 *   - 物理动画：基于物理参数（刚度 / 阻尼 / 摩擦力），曲线由速度自然演化
 */
class PhysicsAnimationDemoActivity : AppCompatActivity() {

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_physics)
        setupDemoToolbar(R.string.anim_physics, R.id.physics_root)

        setupSpring()
        setupFling()
    }

    /// SpringAnimation —— 弹簧动画：拖动小球，松手回弹到原位
    private fun setupSpring() {
        val ball = findViewById<View>(R.id.spring_ball)

        val springX = SpringAnimation(ball, DynamicAnimation.TRANSLATION_X).apply {
            spring = SpringForce(0f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        }
        val springY = SpringAnimation(ball, DynamicAnimation.TRANSLATION_Y).apply {
            spring = SpringForce(0f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        }

        /// 拖动监听
        ball.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.rawX - v.translationX
                    downY = ev.rawY - v.translationY
                    springX.cancel(); springY.cancel()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.translationX = ev.rawX - downX
                    v.translationY = ev.rawY - downY
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    /// 松手 → 弹回 0
                    springX.start()
                    springY.start()
                    true
                }
                else -> false
            }
        }

        /// 不同阻尼对比
        fun bounce(damping: Float) {
            ball.translationX = 0f; ball.translationY = -300f
            springY.spring.dampingRatio = damping
            springY.start()
        }
        findViewById<TextView>(R.id.btn_spring_low).setOnClickListener {
            bounce(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        }
        findViewById<TextView>(R.id.btn_spring_medium).setOnClickListener {
            bounce(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
        }
        findViewById<TextView>(R.id.btn_spring_high).setOnClickListener {
            bounce(SpringForce.DAMPING_RATIO_NO_BOUNCY)
        }
    }

    /// FlingAnimation —— 模拟惯性甩动
    private fun setupFling() {
        val box = findViewById<View>(R.id.fling_box)
        val fling = FlingAnimation(box, DynamicAnimation.TRANSLATION_X).apply {
            setStartVelocity(2500f)
            friction = 1.1f
            setMinValue(0f)
            setMaxValue(700f)
        }
        findViewById<TextView>(R.id.btn_fling).setOnClickListener {
            box.translationX = 0f
            fling.start()
        }
    }
}
