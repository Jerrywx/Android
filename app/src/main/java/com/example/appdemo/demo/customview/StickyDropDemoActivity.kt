package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.StickyDropView

/**
 * 粘性水滴 —— Path + 二阶贝塞尔进阶。
 *
 * 涵盖：
 *   1) 两圆之间用 quadTo 连出粘连"腰身"
 *   2) 距离超过阈值判定断裂，此后只画拖拽圆
 *   3) 抬手弹性回位 vs 断裂消失两种交互
 */
class StickyDropDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_sticky_drop)
        setupDemoToolbar(R.string.customview_sticky_title, R.id.customview_sticky_root)

        val drop = findViewById<StickyDropView>(R.id.customview_sticky_view)
        drop.onBurst = {
            Toast.makeText(this, R.string.customview_sticky_burst, Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.customview_sticky_reset).setOnClickListener {
            drop.reset()
        }
    }
}
