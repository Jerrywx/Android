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

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity 转场动画演示。
 *
 * 两种方式：
 *   1) overridePendingTransition(enterAnim, exitAnim) —— 经典补间动画
 *   2) ActivityOptions —— 系统级转场（ScaleUp / ClipReveal / Custom）
 */
class ActivityTransitionDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_activity_transition)
        setupDemoToolbar(R.string.anim_activity_transition, R.id.at_root)

        /// 左右滑入
        findViewById<TextView>(R.id.btn_slide).setOnClickListener {
            startActivity(Intent(this, ActivityTransitionTargetActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        /// 淡入淡出
        findViewById<TextView>(R.id.btn_fade).setOnClickListener {
            startActivity(Intent(this, ActivityTransitionTargetActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        /// 从按钮位置放大
        findViewById<TextView>(R.id.btn_scale_up).setOnClickListener { v ->
            val opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.width, v.height)
            startActivity(Intent(this, ActivityTransitionTargetActivity::class.java), opts.toBundle())
        }

        /// 圆形展开（ClipReveal，API 23+）
        findViewById<TextView>(R.id.btn_clip_reveal).setOnClickListener { v ->
            val opts = ActivityOptions.makeClipRevealAnimation(v, 0, 0, v.width, v.height)
            startActivity(Intent(this, ActivityTransitionTargetActivity::class.java), opts.toBundle())
        }
    }

    /// 返回时也使用淡入淡出
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
