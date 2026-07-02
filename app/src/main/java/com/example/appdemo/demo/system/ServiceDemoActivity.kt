package com.example.appdemo.demo.system

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service 演示 —— 后台任务的三种典型玩法。
 *
 *   1) startService：单向启动
 *   2) bindService：拿到 IBinder，可回调
 *   3) 前台 Service：常驻通知栏（音乐/位置类必须用）
 */
class ServiceDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private var binder: DemoBoundService.LocalBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as? DemoBoundService.LocalBinder
            append("🔗 已连接到 BoundService")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            append("🔌 与 BoundService 断开")
        }
    }

    private val notiPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startForegroundInternal()
        else append("❌ 通知权限拒绝，前台 Service 无常驻通知")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_service)
        setupDemoToolbar(R.string.system_service_title, R.id.system_service_root)

        log = findViewById(R.id.tv_service_log)

        findViewById<TextView>(R.id.btn_service_start).setOnClickListener {
            startService(Intent(this, DemoStartedService::class.java))
            append("🚀 startService")
        }
        findViewById<TextView>(R.id.btn_service_stop).setOnClickListener {
            stopService(Intent(this, DemoStartedService::class.java))
            append("🛑 stopService")
        }

        findViewById<TextView>(R.id.btn_service_bind).setOnClickListener {
            bindService(
                Intent(this, DemoBoundService::class.java),
                connection,
                Context.BIND_AUTO_CREATE,
            )
        }
        findViewById<TextView>(R.id.btn_service_ask).setOnClickListener {
            val ans = binder?.getService()?.currentCount()
            append(if (ans == null) "⚠️ 尚未 bind" else "🎯 BoundService 当前计数 = $ans")
        }
        findViewById<TextView>(R.id.btn_service_unbind).setOnClickListener {
            if (binder != null) {
                unbindService(connection)
                binder = null
                append("🔌 unbindService")
            } else append("⚠️ 未处于 bind 状态")
        }

        findViewById<TextView>(R.id.btn_service_foreground).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notiPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startForegroundInternal()
            }
        }
        findViewById<TextView>(R.id.btn_service_foreground_stop).setOnClickListener {
            stopService(Intent(this, DemoForegroundService::class.java))
            append("🛑 停止前台 Service")
        }
    }

    private fun startForegroundInternal() {
        val intent = Intent(this, DemoForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
        append("🚀 启动前台 Service，请下拉通知栏查看")
    }

    override fun onDestroy() {
        if (binder != null) runCatching { unbindService(connection) }
        super.onDestroy()
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }
}
