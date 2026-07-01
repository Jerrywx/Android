package com.example.appdemo

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 过渡动画演示 —— TransitionManager 自动补间布局变化。
 *
 * 工作原理：
 *   1) beginDelayedTransition 记录当前布局状态
 *   2) 你修改任意子 View 的属性 / 布局参数
 *   3) 下一帧绘制时，自动以指定 Transition 过渡到新状态
 */
class TransitionDemoActivity : AppCompatActivity() {

    private lateinit var sceneRoot: FrameLayout
    private lateinit var card: View
    private var expanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_transition)
        setupDemoToolbar(R.string.anim_transition, R.id.trans_root)

        sceneRoot = findViewById(R.id.trans_scene_root)
        card = findViewById(R.id.trans_card)

        /// ChangeBounds —— 大小 / 位置变化
        findViewById<TextView>(R.id.btn_change_bounds).setOnClickListener {
            TransitionManager.beginDelayedTransition(sceneRoot, ChangeBounds().setDuration(600))
            val lp = card.layoutParams as FrameLayout.LayoutParams
            if (expanded) {
                lp.width = dp(120); lp.height = dp(120); lp.gravity = Gravity.CENTER
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = dp(220)
                lp.gravity = Gravity.CENTER
            }
            card.layoutParams = lp
            expanded = !expanded
        }

        /// Fade —— 淡入淡出
        findViewById<TextView>(R.id.btn_fade).setOnClickListener {
            TransitionManager.beginDelayedTransition(sceneRoot, Fade().setDuration(500))
            card.visibility = if (card.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }

        /// Slide —— 滑入滑出
        findViewById<TextView>(R.id.btn_slide).setOnClickListener {
            val slide = Slide(Gravity.END).setDuration(500)
            TransitionManager.beginDelayedTransition(sceneRoot, slide)
            card.visibility = if (card.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }

        /// AutoTransition —— Fade + ChangeBounds 组合
        findViewById<TextView>(R.id.btn_auto).setOnClickListener {
            TransitionManager.beginDelayedTransition(sceneRoot, AutoTransition().setDuration(600))
            val lp = card.layoutParams as FrameLayout.LayoutParams
            lp.width = dp(if (expanded) 120 else 200)
            lp.height = dp(if (expanded) 120 else 200)
            card.layoutParams = lp
            card.alpha = if (expanded) 1f else 0.5f
            expanded = !expanded
        }

        /// 复位
        findViewById<TextView>(R.id.btn_explode).setOnClickListener {
            TransitionManager.beginDelayedTransition(sceneRoot, AutoTransition().setDuration(400))
            val lp = card.layoutParams as FrameLayout.LayoutParams
            lp.width = dp(120); lp.height = dp(120); lp.gravity = Gravity.CENTER
            card.layoutParams = lp
            card.alpha = 1f
            card.visibility = View.VISIBLE
            expanded = false
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
