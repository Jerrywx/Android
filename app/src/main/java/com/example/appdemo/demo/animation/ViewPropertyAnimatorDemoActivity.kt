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
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * ViewPropertyAnimator 演示 —— 通过 view.animate() 链式调用。
 *
 * 优点：
 *   1) 一次性合并多个属性更新，性能好
 *   2) API 简洁，可读性高
 *   3) 支持 withStartAction / withEndAction 回调
 */
class ViewPropertyAnimatorDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_view_property)
        setupDemoToolbar(R.string.anim_view_property, R.id.vpa_root)

        val target = findViewById<View>(R.id.vpa_target)

        /// 链式·基础 —— 单属性动画
        findViewById<TextView>(R.id.btn_chain_simple).setOnClickListener {
            target.animate()
                .translationX(200f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        /// 链式·组合 —— 多属性同时变化
        findViewById<TextView>(R.id.btn_chain_combo).setOnClickListener {
            target.animate()
                .translationX(0f).translationY(-100f)
                .rotation(360f)
                .scaleX(1.4f).scaleY(1.4f)
                .alpha(0.6f)
                .setDuration(900)
                .setInterpolator(OvershootInterpolator())
                .withEndAction {
                    /// 链式回退
                    target.animate()
                        .translationY(0f).scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(500).start()
                }
                .start()
        }

        /// 3D 翻转
        findViewById<TextView>(R.id.btn_chain_flip).setOnClickListener {
            target.animate()
                .rotationY(360f)
                .setDuration(1000)
                .start()
        }

        /// 跳跃 —— 上跳再下落
        findViewById<TextView>(R.id.btn_chain_jump).setOnClickListener {
            target.animate()
                .translationY(-200f)
                .setDuration(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    target.animate()
                        .translationY(0f)
                        .setDuration(700)
                        .setInterpolator(BounceInterpolator())
                        .start()
                }
                .start()
        }

        /// 抖动效果
        findViewById<TextView>(R.id.btn_chain_shake).setOnClickListener {
            shake(target)
        }

        /// 重置
        findViewById<TextView>(R.id.btn_reset).setOnClickListener {
            target.animate()
                .translationX(0f).translationY(0f)
                .scaleX(1f).scaleY(1f)
                .rotation(0f).rotationX(0f).rotationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    /// 经典抖动：左右快速来回
    private fun shake(view: View) {
        val offsets = floatArrayOf(0f, -25f, 25f, -20f, 20f, -10f, 10f, 0f)
        var i = 0
        fun next() {
            if (i >= offsets.size) return
            view.animate().translationX(offsets[i]).setDuration(60).withEndAction {
                i++
                next()
            }.start()
        }
        next()
    }
}
