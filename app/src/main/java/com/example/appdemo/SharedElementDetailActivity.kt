package com.example.appdemo

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 共享元素动画的目标页面。
 *
 * 接收来自前一页的 transitionName 与背景资源 ID，
 * 将 detail 视图设置为同名 transitionName，使框架自动完成过渡。
 */
class SharedElementDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_shared_detail)
        setupDemoToolbar(R.string.anim_shared_element, R.id.shared_detail_root)

        val name = intent.getStringExtra(EXTRA_NAME) ?: "shared"
        val bgRes = intent.getIntExtra(EXTRA_BG, R.drawable.shape_anim_circle)

        val view = findViewById<View>(R.id.shared_detail_view)
        view.transitionName = name
        view.setBackgroundResource(bgRes)
    }

    companion object {
        const val EXTRA_NAME = "extra_transition_name"
        const val EXTRA_BG = "extra_bg_res"
    }
}
