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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 共享元素动画演示 —— Activity 之间元素无缝过渡。
 *
 * 要求：
 *   1) 两个页面的 View 设置相同 transitionName
 *   2) startActivity 时附带 ActivityOptions.makeSceneTransitionAnimation
 *   3) Activity 主题需开启 windowContentTransitions（默认主题已开启）
 */
class SharedElementDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_shared_element)
        setupDemoToolbar(R.string.anim_shared_element, R.id.shared_root)

        val avatars = mapOf(
            R.id.shared_avatar_1 to R.drawable.shape_anim_circle,
            R.id.shared_avatar_2 to R.drawable.shape_anim_star,
            R.id.shared_avatar_3 to R.drawable.shape_anim_ball,
        )
        avatars.forEach { (id, bgRes) ->
            findViewById<View>(id).setOnClickListener { v ->
                val intent = Intent(this, SharedElementDetailActivity::class.java).apply {
                    putExtra(SharedElementDetailActivity.EXTRA_NAME, v.transitionName)
                    putExtra(SharedElementDetailActivity.EXTRA_BG, bgRes)
                }
                /// 通过 ActivityOptions 指定共享元素 + transitionName
                val opts = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName)
                startActivity(intent, opts.toBundle())
            }
        }
    }
}
