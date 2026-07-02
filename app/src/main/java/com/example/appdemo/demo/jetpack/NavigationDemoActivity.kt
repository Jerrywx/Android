package com.example.appdemo.demo.jetpack

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * Navigation Component 演示 —— NavHostFragment + NavGraph XML。
 *
 * 演示要点：
 *   1) NavHostFragment 作为 Fragment 容器
 *   2) NavGraph XML 声明式定义目的地与跳转
 *   3) Bundle 传参 + argType 声明
 *   4) 回退栈自动管理
 *
 * 具体跳转在 nav_jetpack_graph 中定义，起始 destination 是 NavHomeFragment。
 */
class NavigationDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jetpack_nav)
        setupDemoToolbar(R.string.jp_nav_title, R.id.jp_nav_root)
    }
}
