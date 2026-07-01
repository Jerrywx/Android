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

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 帧动画演示 —— AnimationDrawable 逐帧播放。
 *
 * 在 res/drawable/ 下使用 <animation-list> 定义帧序列，
 * setImageResource 或在 XML 中 src 加载后，通过 (drawable as AnimationDrawable).start() 播放。
 */
class FrameAnimationDemoActivity : AppCompatActivity() {

    private lateinit var anim: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_frame)
        setupDemoToolbar(R.string.anim_frame, R.id.frame_root)

        val iv = findViewById<ImageView>(R.id.iv_loading)
        anim = iv.drawable as AnimationDrawable

        findViewById<TextView>(R.id.btn_start).setOnClickListener {
            if (!anim.isRunning) anim.start()
        }
        findViewById<TextView>(R.id.btn_stop).setOnClickListener {
            if (anim.isRunning) anim.stop()
        }
    }

    /// Activity 出现在前台后自动播放
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !anim.isRunning) anim.start()
    }
}
