package com.example.appdemo.demo.system

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handler / Looper / HandlerThread 演示 —— Android 消息机制。
 *
 * 涵盖：
 *   1) 主线程 Handler 延迟任务（postDelayed）
 *   2) HandlerThread：后台线程 + 私有 Looper，处理串行任务
 *   3) sendMessage(what/arg1/obj) 与主线程通信
 *   4) removeCallbacksAndMessages(null) 停止全部
 *
 * 与协程对比：协程简单直观，但 Handler 在 View 系统、Choreographer 中仍随处可见。
 */
class HandlerDemoActivity : AppCompatActivity() {

    private companion object {
        const val MSG_FROM_BG = 1
    }

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /// 主线程 Handler，用来接收后台线程发来的 message
    private val mainHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_FROM_BG -> append("💌 主线程收到 message obj=${msg.obj}, arg1=${msg.arg1}")
        }
        true
    }

    /// 后台 HandlerThread + 它的 Handler
    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_handler)
        setupDemoToolbar(R.string.system_handler_title, R.id.system_handler_root)

        log = findViewById(R.id.tv_hd_log)

        /// 主线程 Handler 演示：延迟任务
        findViewById<TextView>(R.id.btn_hd_delay).setOnClickListener {
            append("🕐 提交 postDelayed 2s")
            mainHandler.postDelayed({ append("⏰ 2s 后回调触发") }, 2000)
        }

        /// HandlerThread 演示：启动后台线程，从后台发消息回主线程
        findViewById<TextView>(R.id.btn_hd_bg).setOnClickListener {
            ensureBg()
            bgHandler?.post {
                val threadName = Thread.currentThread().name
                Thread.sleep(500)
                mainHandler.obtainMessage(MSG_FROM_BG, 42, 0, "from $threadName").sendToTarget()
            }
            append("🚀 已投递任务到后台线程")
        }

        findViewById<TextView>(R.id.btn_hd_cancel).setOnClickListener {
            /// null 表示移除所有消息与 Runnable
            mainHandler.removeCallbacksAndMessages(null)
            bgHandler?.removeCallbacksAndMessages(null)
            append("🛑 取消所有 pending 消息")
        }

        findViewById<TextView>(R.id.btn_hd_clear).setOnClickListener {
            log.text = getString(R.string.storage_log_hint)
        }
    }

    private fun ensureBg() {
        if (bgThread == null) {
            bgThread = HandlerThread("demo-handler-thread").also { it.start() }
            bgHandler = Handler(bgThread!!.looper)
            append("🧵 已创建 HandlerThread")
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        bgHandler?.removeCallbacksAndMessages(null)
        /// quitSafely 会跑完队列里已入队的任务再退出
        bgThread?.quitSafely()
        bgThread = null
        bgHandler = null
        super.onDestroy()
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }
}
