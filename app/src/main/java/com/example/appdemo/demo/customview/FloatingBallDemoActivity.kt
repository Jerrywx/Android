package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.FloatingBallView

/**
 * 可拖拽悬浮球 —— 事件处理 + Scroller 惯性 + 贴边吸附。
 *
 * 涵盖：
 *   1) onTouchEvent + TouchSlop 判定拖动
 *   2) VelocityTracker 追踪抛掷速度
 *   3) Scroller + computeScroll 做惯性滑行
 *   4) 边界约束 + 贴边吸附到最近一侧
 */
class FloatingBallDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_floating_ball)
        setupDemoToolbar(R.string.customview_drag_ball_title, R.id.customview_ball_root)

        val ball = findViewById<FloatingBallView>(R.id.customview_ball)
        findViewById<Button>(R.id.customview_ball_center).setOnClickListener {
            ball.moveToCenter()
        }
    }
}
