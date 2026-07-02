package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.DashboardView

/**
 * 仪表盘 —— 极坐标 + 变换栈实战。
 *
 * 涵盖：
 *   1) drawArc 270° 轨道 + SweepGradient 分段配色
 *   2) sin/cos 沿弧计算刻度端点与文字位置
 *   3) 单独一条线做指针，save/rotate 可替代但这里用极坐标更直观
 *   4) ValueAnimator 数值动画驱动重绘
 */
class DashboardDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_dashboard)
        setupDemoToolbar(R.string.customview_dashboard_title, R.id.customview_dashboard_root)

        val dash = findViewById<DashboardView>(R.id.customview_dashboard_view)
        dash.animateTo(65)

        findViewById<Button>(R.id.customview_dashboard_random).setOnClickListener {
            dash.animateTo((0..100).random())
        }
        findViewById<Button>(R.id.customview_dashboard_zero).setOnClickListener {
            dash.animateTo(0)
        }
    }
}
