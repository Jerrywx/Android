package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Flow 演示 —— cold Flow / StateFlow / SharedFlow 对比。
 *
 *   1) cold Flow：每次 collect 都会重新执行 flow{ } 里的代码
 *   2) StateFlow：热流，持有当前值，新订阅者立刻收到最新值
 *   3) SharedFlow：热流，无初始值，可配置重放数量
 *   4) 常用操作符：map / filter
 */
class FlowDemoActivity : AppCompatActivity() {

    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /// StateFlow 演示用：持有一个整数计数
    private val counter = MutableStateFlow(0)

    /// SharedFlow 演示用：广播事件流，replay = 0 表示不重放历史
    private val events = MutableSharedFlow<String>(replay = 0)

    private var coldJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jetpack_flow)
        setupDemoToolbar(R.string.jp_flow_title, R.id.jp_flow_root)

        val coldLog = findViewById<TextView>(R.id.tv_flow_cold_log)
        val stateLog = findViewById<TextView>(R.id.tv_flow_state_log)
        val sharedLog = findViewById<TextView>(R.id.tv_flow_shared_log)

        /// ─── cold Flow ───
        findViewById<TextView>(R.id.btn_flow_cold_run).setOnClickListener {
            coldJob?.cancel()
            append(coldLog, "▶️ 开始 collect cold Flow", true)
            coldJob = lifecycleScope.launch {
                coldNumbers()
                    .map { it * it }
                    .filter { it % 2 == 0 }
                    .collect { append(coldLog, "🔹 收到 $it") }
                append(coldLog, "✅ Flow 完成")
            }
        }
        findViewById<TextView>(R.id.btn_flow_cold_cancel).setOnClickListener {
            coldJob?.cancel()
            append(coldLog, "⏹ 已取消 collect")
        }

        /// ─── StateFlow ───
        lifecycleScope.launch {
            counter.collect { append(stateLog, "📥 StateFlow = $it") }
        }
        findViewById<TextView>(R.id.btn_flow_state_inc).setOnClickListener {
            counter.value = counter.value + 1
        }
        findViewById<TextView>(R.id.btn_flow_state_reset).setOnClickListener {
            counter.value = 0
        }

        /// ─── SharedFlow ───
        lifecycleScope.launch {
            events.collect { append(sharedLog, "📣 收到事件：$it") }
        }
        findViewById<TextView>(R.id.btn_flow_shared_emit).setOnClickListener {
            lifecycleScope.launch { events.emit("click@${timeFmt.format(Date())}") }
        }
    }

    /// cold Flow：flow{ } 是惰性的，每次 collect 都会重新走一遍
    private fun coldNumbers() = flow {
        for (i in 1..5) {
            kotlinx.coroutines.delay(300)
            emit(i)
        }
    }

    private fun append(tv: TextView, msg: String, reset: Boolean = false) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        tv.text = if (reset || tv.text.isNullOrBlank() || tv.text.startsWith("点击")) line
        else "${tv.text}\n$line"
    }
}
