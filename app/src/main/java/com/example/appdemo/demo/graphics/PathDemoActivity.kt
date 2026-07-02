package com.example.appdemo.demo.graphics

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import com.example.appdemo.demo.graphics.widget.SignatureView

/**
 * Path 演示 —— 签名板，展示：
 *   · moveTo / lineTo / quadTo 平滑曲线
 *   · 手指抬起后 Path 加入 history 列表
 *   · 撤销 / 清空
 */
class PathDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphics_path)
        setupDemoToolbar(R.string.graphics_path_title, R.id.graphics_path_root)

        val board = findViewById<SignatureView>(R.id.signature_board)
        findViewById<TextView>(R.id.btn_path_undo).setOnClickListener { board.undo() }
        findViewById<TextView>(R.id.btn_path_clear).setOnClickListener { board.clear() }
    }
}
