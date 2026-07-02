package com.example.appdemo.demo.customview

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.customview.widget.RulerView

/**
 * 刻度尺 View —— 滚动 / 惯性 / 吸附一条龙。
 *
 * 涵盖：
 *   1) VelocityTracker + Scroller 手感三件套
 *   2) ACTION_UP 后吸附到最近整格
 *   3) pixel ↔ value 的双向换算
 */
class RulerDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_ruler)
        setupDemoToolbar(R.string.customview_ruler_title, R.id.customview_ruler_root)

        val ruler = findViewById<RulerView>(R.id.customview_ruler_view)
        val label = findViewById<TextView>(R.id.customview_ruler_label)

        ruler.setOnValueChangeListener { value ->
            label.text = getString(R.string.customview_ruler_current, value)
        }
        /// 触发一次初始文案
        label.text = getString(R.string.customview_ruler_current, ruler.getValue())

        findViewById<Button>(R.id.customview_ruler_reset).setOnClickListener {
            ruler.setValue(170)
        }
        findViewById<Button>(R.id.customview_ruler_random).setOnClickListener {
            ruler.setValue((60..200).random())
        }
    }
}
