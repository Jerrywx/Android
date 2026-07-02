package com.example.appdemo.demo.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BroadcastReceiver 演示 —— 三种常见姿势：
 *
 *   1) 动态注册 + 显式 sendBroadcast：应用内自定义 action
 *   2) 静态注册（Manifest）+ 系统广播：如 BOOT_COMPLETED（这里演示 API，不真触发）
 *   3) LocalBroadcastManager：进程内广播，效率高、安全
 *
 * 从 Android 8 开始，隐式广播的静态注册大多不再工作，推荐显式或本地广播。
 */
class BroadcastDemoActivity : AppCompatActivity() {

    private companion object {
        const val ACTION_APP = "com.example.appdemo.ACTION_DEMO"
        const val ACTION_LOCAL = "com.example.appdemo.ACTION_LOCAL"
    }

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /// 动态注册的应用内广播接收者
    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val msg = intent.getStringExtra("msg") ?: "—"
            append("📡 应用内广播：$msg")
        }
    }

    /// LocalBroadcastManager 广播接收者
    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val msg = intent.getStringExtra("msg") ?: "—"
            append("🏠 本地广播：$msg")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_broadcast)
        setupDemoToolbar(R.string.system_broadcast_title, R.id.system_broadcast_root)

        log = findViewById(R.id.tv_bc_log)

        /// 注册两个接收者
        ContextCompat.registerReceiver(
            this,
            appReceiver,
            IntentFilter(ACTION_APP),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localReceiver, IntentFilter(ACTION_LOCAL))

        findViewById<TextView>(R.id.btn_bc_app).setOnClickListener {
            val intent = Intent(ACTION_APP).apply {
                setPackage(packageName)
                putExtra("msg", "hello@${timeFmt.format(Date())}")
            }
            sendBroadcast(intent)
            append("✉️ 发送应用内广播 $ACTION_APP")
        }

        findViewById<TextView>(R.id.btn_bc_local).setOnClickListener {
            val intent = Intent(ACTION_LOCAL).apply {
                putExtra("msg", "local@${timeFmt.format(Date())}")
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            append("✉️ 发送本地广播 $ACTION_LOCAL")
        }

        findViewById<TextView>(R.id.btn_bc_clear).setOnClickListener {
            log.text = getString(R.string.storage_log_hint)
        }
    }

    override fun onDestroy() {
        /// 一定要反注册，否则内存泄漏
        unregisterReceiver(appReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver)
        super.onDestroy()
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }
}
