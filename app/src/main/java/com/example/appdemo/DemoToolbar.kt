package com.example.appdemo

import android.app.Activity
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.setupDemoToolbar(@StringRes titleRes: Int, rootId: Int = R.id.demo_root) {
    findViewById<TextView>(R.id.demo_title).setText(titleRes)
    findViewById<ImageButton>(R.id.demo_back).setOnClickListener { finish() }
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(rootId)) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}
