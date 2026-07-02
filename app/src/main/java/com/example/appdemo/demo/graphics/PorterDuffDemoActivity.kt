package com.example.appdemo.demo.graphics

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.graphics.widget.ScratchCardView

/**
 * PorterDuff 演示 —— 刮刮卡效果。
 *
 *   · 底层：奖品文字
 *   · 上层：一层灰色画布
 *   · 手指划过用 CLEAR 模式擦除，显示底部内容
 */
class PorterDuffDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_porterduff)
        setupDemoToolbar(R.string.graphics_porterduff_title, R.id.graphics_porterduff_root)

        val card = findViewById<ScratchCardView>(R.id.scratch_card)
        findViewById<TextView>(R.id.btn_scratch_reset).setOnClickListener { card.reset() }
    }
}
