package com.example.appdemo.demo.customview

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.FlowLayout

/**
 * 流式标签布局 —— ViewGroup 测量 / 布局练习。
 *
 * 涵盖：
 *   1) 自定义 ViewGroup 的 onMeasure / onLayout
 *   2) 按行摆放子 View 并自动换行
 *   3) 通过代码给 FlowLayout 动态添加 / 删除标签
 */
class FlowLayoutDemoActivity : AppCompatActivity() {

    private val presetTags = listOf(
        "Android", "Kotlin", "Jetpack", "Compose", "自定义 View",
        "Coroutine", "Flow", "RecyclerView", "ViewPager2", "Navigation",
        "Room", "DataStore", "Retrofit", "OkHttp", "Hilt",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_flow_layout)
        setupDemoToolbar(R.string.customview_flow_title, R.id.customview_flow_root)

        val flow = findViewById<FlowLayout>(R.id.customview_flow_container)
        val input = findViewById<EditText>(R.id.customview_flow_input)
        val addBtn = findViewById<Button>(R.id.customview_flow_add)
        val resetBtn = findViewById<Button>(R.id.customview_flow_reset)

        fun rebuildPresets() {
            flow.removeAllViews()
            presetTags.forEach { flow.addView(createTag(it, flow)) }
        }
        rebuildPresets()

        addBtn.setOnClickListener {
            val text = input.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return@setOnClickListener
            flow.addView(createTag(text, flow))
            input.setText("")
        }
        resetBtn.setOnClickListener { rebuildPresets() }
    }

    private fun createTag(text: String, parent: FlowLayout): TextView {
        val density = resources.displayMetrics.density
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.parseColor("#1E88E5"))
            textSize = 13f
            setPadding((12 * density).toInt(), (6 * density).toInt(), (12 * density).toInt(), (6 * density).toInt())
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = 999f
                setColor(Color.parseColor("#E3F2FD"))
                setStroke((density).toInt(), Color.parseColor("#90CAF9"))
            }
            isClickable = true
            /// 点击标签自身，从 FlowLayout 中移除，演示动态增删
            setOnClickListener { parent.removeView(this) }
        }
    }
}
