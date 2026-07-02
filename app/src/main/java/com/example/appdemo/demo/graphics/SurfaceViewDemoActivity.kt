package com.example.appdemo.demo.graphics

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.graphics.widget.ParticleSurfaceView

/**
 * SurfaceView 演示 —— 独立渲染线程绘制粒子系统。
 *
 *   · SurfaceView 有自己的 Surface，绘制不阻塞主线程
 *   · lockCanvas / unlockCanvasAndPost 驱动帧
 *   · 生命周期通过 SurfaceHolder.Callback 管理
 */
class SurfaceViewDemoActivity : AppCompatActivity() {

    private lateinit var surface: ParticleSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_surface)
        setupDemoToolbar(R.string.graphics_surface_title, R.id.graphics_surface_root)

        surface = findViewById(R.id.particle_surface)
        findViewById<TextView>(R.id.btn_surface_burst).setOnClickListener { surface.burst() }
        findViewById<TextView>(R.id.btn_surface_clear).setOnClickListener { surface.clearAll() }
    }
}
