package com.example.appdemo.demo.graphics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * 手势缩放 —— 双指缩放 + 平移 + 双击复位。
 *
 *   · ScaleGestureDetector 处理双指
 *   · GestureDetector 处理双击 / 单指拖动
 *   · Matrix + setImageMatrix 应用变换
 */
class ZoomImageDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_zoom)
        setupDemoToolbar(R.string.graphics_zoom_title, R.id.graphics_zoom_root)
    }
}
