package com.example.appdemo.demo.customview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

/**
 * 自定义 EditText —— 清除按钮 + 密码切换。
 *
 * 涵盖：
 *   1) declare-styleable 自定义 XML 属性
 *   2) setCompoundDrawablesRelativeWithIntrinsicBounds 挂图标
 *   3) onTouchEvent 判定命中右侧图标区域
 *   4) inputType 切换实现密码可见 / 不可见
 */
class ClearableEditTextDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customview_clearable_edit)
        setupDemoToolbar(R.string.customview_clear_edit_title, R.id.customview_clear_root)
    }
}
