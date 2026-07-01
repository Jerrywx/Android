package com.example.appdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity 转场目标页 —— 用于演示进入/退出动画。
 *
 * 通过 ActivityTransitionDemoActivity 启动，传入 EXTRA_EXIT_ANIM 决定
 * 返回时使用的退出动画。重写 finish() 以应用 overridePendingTransition。
 */
class ActivityTransitionTargetActivity : AppCompatActivity() {

    private var enterAnim: Int = 0
    private var exitAnim: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anim_activity_transition_target)
        setupDemoToolbar(R.string.anim_activity_transition, R.id.at_target_root)

        enterAnim = intent.getIntExtra(EXTRA_ENTER_ANIM, 0)
        exitAnim = intent.getIntExtra(EXTRA_EXIT_ANIM, 0)
    }

    override fun finish() {
        super.finish()
        if (enterAnim != 0 && exitAnim != 0) {
            overridePendingTransition(enterAnim, exitAnim)
        }
    }

    companion object {
        const val EXTRA_ENTER_ANIM = "enter_anim"
        const val EXTRA_EXIT_ANIM = "exit_anim"
    }
}
