package com.example.appdemo.demo.graphics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * Canvas 基础 —— 一个走时的表盘，展示：
 *   · Paint 描边 / 抗锯齿
 *   · Canvas 变换栈 save/restore + rotate
 *   · drawCircle / drawLine / drawText
 *   · 定时 invalidate 驱动重绘
 */
class CanvasBasicDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_canvas)
        setupDemoToolbar(R.string.graphics_canvas_title, R.id.graphics_canvas_root)
    }
}
