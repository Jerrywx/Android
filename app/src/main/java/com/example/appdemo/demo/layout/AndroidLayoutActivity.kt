package com.example.appdemo.demo.layout

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
