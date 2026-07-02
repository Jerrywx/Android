package com.example.appdemo.demo.graphics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * Shader 演示 —— 三种渐变对比：
 *
 *   · LinearGradient：横向渐变进度条
 *   · SweepGradient：360° 扫描色环
 *   · RadialGradient：中心向外辐射的圆
 */
class ShaderDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_shader)
        setupDemoToolbar(R.string.graphics_shader_title, R.id.graphics_shader_root)
    }
}
