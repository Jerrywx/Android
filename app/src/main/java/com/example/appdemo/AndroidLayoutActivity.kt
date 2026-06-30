package com.example.appdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 安卓布局演示 —— 展示 Android 五大布局的核心用法与对比。
 *
 * 内容直接通过 ScrollView 排版在 XML 中完成，每个布局类型
 * 都配有文字说明和可运行的 UI 实例。
 */
class AndroidLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_layout)
        setupDemoToolbar(R.string.android_layout_title, R.id.android_layout_root)
    }
}
