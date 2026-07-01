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
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class TweenAnimationDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_tween)
        setupDemoToolbar(R.string.anim_tween, R.id.tween_root)

        val target = findViewById<android.view.View>(R.id.tween_target)

        /// 透明度：0 → 1
        findViewById<TextView>(R.id.btn_alpha).setOnClickListener {
            target.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tween_alpha))
        }
        /// 缩放：从中心向外放大
        findViewById<TextView>(R.id.btn_scale).setOnClickListener {
            target.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tween_scale))
        }
        /// 平移：横向往返
        findViewById<TextView>(R.id.btn_translate).setOnClickListener {
            target.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tween_translate))
        }
        /// 旋转：绕中心旋转 360°
        findViewById<TextView>(R.id.btn_rotate).setOnClickListener {
            target.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tween_rotate))
        }
        /// 组合动画：缩放 + 旋转 + 平移同时执行
        findViewById<TextView>(R.id.btn_set).setOnClickListener {
            target.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tween_set))
        }
        findViewById<TextView>(R.id.btn_reset).setOnClickListener {
            target.clearAnimation()
        }
    }
}
