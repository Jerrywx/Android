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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.demo.animation.widget.CircleProgressView
import com.example.appdemo.demo.animation.widget.RippleView

/**
 * 自定义绘制动画演示 —— Canvas + ValueAnimator。
 *
 * 模式：
 *   1) ValueAnimator 提供时间驱动 (0 → 1)
 *   2) 每帧回调里更新状态字段，调用 invalidate()
 *   3) onDraw 根据当前状态绘制
 */
class CanvasAnimationDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_canvas)
        setupDemoToolbar(R.string.anim_canvas, R.id.canvas_root)

        val progress = findViewById<CircleProgressView>(R.id.circle_progress)
        findViewById<TextView>(R.id.btn_progress_run).setOnClickListener {
            progress.animateTo(1f, 2000)
        }

        val ripple = findViewById<RippleView>(R.id.ripple_view)
        findViewById<TextView>(R.id.btn_ripple_start).setOnClickListener { ripple.start() }
        findViewById<TextView>(R.id.btn_ripple_stop).setOnClickListener { ripple.stop() }
    }
}
