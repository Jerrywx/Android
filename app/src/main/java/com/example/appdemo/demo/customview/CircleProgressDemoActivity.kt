package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.CircleProgressView

/**
 * 圆形进度条 —— 自定义 View 入门。
 *
 * 涵盖：
 *   1) drawArc 画环形轨道 / 进度弧
 *   2) SweepGradient 给进度弧上色
 *   3) drawText 居中百分比
 *   4) ValueAnimator 平滑过渡到目标值
 */
class CircleProgressDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_circle_progress)
        setupDemoToolbar(R.string.customview_circle_progress_title, R.id.customview_circle_root)

        val progress = findViewById<CircleProgressView>(R.id.customview_circle_view)
        progress.animateTo(72)

        findViewById<Button>(R.id.customview_circle_run).setOnClickListener {
            progress.animateTo(72)
        }
        findViewById<Button>(R.id.customview_circle_random).setOnClickListener {
            progress.animateTo((0..100).random())
        }
    }
}
