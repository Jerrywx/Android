package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lifecycle 演示 —— 让任意组件感知宿主生命周期。
 *
 * 两种写法：
 *   1) DefaultLifecycleObserver：按 onCreate/onStart/... 回调（推荐）
 *   2) LifecycleEventObserver：统一 onStateChanged(event) 回调
 *
 * 场景：Activity 里的相机、定位、Timer 等需要跟随生命周期启停时，
 *   把逻辑封装到一个 Observer，addObserver 后完全自动。
 */
class LifecycleDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /// 用 DefaultLifecycleObserver 感知生命周期（按方法分类）
    private val defaultObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) = append("Default: onCreate")
        override fun onStart(owner: LifecycleOwner) = append("Default: onStart")
        override fun onResume(owner: LifecycleOwner) = append("Default: onResume")
        override fun onPause(owner: LifecycleOwner) = append("Default: onPause")
        override fun onStop(owner: LifecycleOwner) = append("Default: onStop")
        override fun onDestroy(owner: LifecycleOwner) = append("Default: onDestroy")
    }

    /// 用 LifecycleEventObserver 感知（统一 event 回调）
    private val eventObserver = LifecycleEventObserver { _, event ->
        append("Event: $event")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jetpack_lifecycle)
        setupDemoToolbar(R.string.jetpack_lifecycle_title, R.id.jetpack_lifecycle_root)

        log = findViewById(R.id.tv_lc_log)

        /// 把两个观察者挂到当前 Activity 的 Lifecycle 上，之后全自动
        lifecycle.addObserver(defaultObserver)
        lifecycle.addObserver(eventObserver)

        findViewById<TextView>(R.id.btn_lc_state).setOnClickListener {
            val state: Lifecycle.State = lifecycle.currentState
            append("📌 当前 state=$state (isAtLeast STARTED? ${state.isAtLeast(Lifecycle.State.STARTED)})")
        }

        findViewById<TextView>(R.id.btn_lc_clear).setOnClickListener {
            log.text = getString(R.string.storage_log_hint)
        }

        append("💡 尝试按 Home 键 → 回到应用，观察日志变化")
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击") || log.text.startsWith("💡"))
            line
        else
            "${log.text}\n$line"
    }
}
