package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.RippleClickView

/**
 * 水波纹点击 —— 多层圆环扩散 + alpha 衰减。
 *
 * 涵盖：
 *   1) ACTION_DOWN 记录触点，生成 Ripple
 *   2) ValueAnimator 同时驱动 radius 变大 + alpha 衰减
 *   3) 多次点击生成多个 Ripple，绘制时叠加
 *   4) 动画结束自动移除，避免列表无限增长
 */
class RippleClickDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_ripple)
        setupDemoToolbar(R.string.customview_ripple_title, R.id.customview_ripple_root)

        val ripple = findViewById<RippleClickView>(R.id.customview_ripple_view)
        findViewById<Button>(R.id.customview_ripple_reset).setOnClickListener {
            ripple.clear()
        }
    }
}
