package com.example.appdemo.demo.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.appdemo.MainActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 通知渠道演示 ——
 *
 *   1) 创建 Channel（API 26+ 必须先创建才能发通知）
 *   2) 普通通知
 *   3) BigText 展开样式
 *   4) 带 Action 的通知（PendingIntent）
 *   5) 进度条通知
 *   6) Android 13+ 需要 POST_NOTIFICATIONS 运行时权限
 */
class NotificationDemoActivity : AppCompatActivity() {

    private companion object {
        const val CHANNEL_ID = "demo_channel"
        const val NOTI_ID_BASIC = 1001
        const val NOTI_ID_BIG = 1002
        const val NOTI_ID_ACTION = 1003
        const val NOTI_ID_PROGRESS = 1004
    }

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val notiPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> append(if (granted) "✅ 通知权限已开启" else "❌ 通知权限被拒绝") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_notification)
        setupDemoToolbar(R.string.system_notification_title, R.id.system_noti_root)

        log = findViewById(R.id.tv_noti_log)

        /// 首次进入即创建 Channel，重复创建无副作用
        createChannel()

        findViewById<TextView>(R.id.btn_noti_perm).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notiPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                append("💡 Android 12 及以下无需运行时申请通知权限")
            }
        }

        findViewById<TextView>(R.id.btn_noti_basic).setOnClickListener {
            if (!ensurePermission()) return@setOnClickListener
            val n = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("普通通知")
                .setContentText("点击可打开应用")
                .setContentIntent(buildLaunchPendingIntent())
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(this).notify(NOTI_ID_BASIC, n)
            append("📩 发送普通通知")
        }

        findViewById<TextView>(R.id.btn_noti_big).setOnClickListener {
            if (!ensurePermission()) return@setOnClickListener
            val longText = "这是一条 BigTextStyle 通知，展开后可以看到完整内容。\n" +
                    "适合日志、邮件预览、行程提醒等场景。可换行、支持较多字数。"
            val n = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("大文本通知")
                .setContentText("下滑展开查看完整内容")
                .setStyle(NotificationCompat.BigTextStyle().bigText(longText))
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(this).notify(NOTI_ID_BIG, n)
            append("📩 发送大文本通知")
        }

        findViewById<TextView>(R.id.btn_noti_action).setOnClickListener {
            if (!ensurePermission()) return@setOnClickListener
            val openApp = buildLaunchPendingIntent()
            val n = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("带按钮的通知")
                .setContentText("试试下面的操作按钮")
                .setContentIntent(openApp)
                .addAction(0, "打开", openApp)
                .addAction(0, "忽略", openApp)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(this).notify(NOTI_ID_ACTION, n)
            append("📩 发送带 Action 的通知")
        }

        findViewById<TextView>(R.id.btn_noti_progress).setOnClickListener {
            if (!ensurePermission()) return@setOnClickListener
            simulateProgress()
        }

        findViewById<TextView>(R.id.btn_noti_cancel).setOnClickListener {
            NotificationManagerCompat.from(this).cancelAll()
            append("🛑 已清除全部通知")
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Demo 通知渠道",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "AppDemo 演示用通知渠道" }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }

    private fun ensurePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            append("⚠️ 尚未授予 POST_NOTIFICATIONS 权限")
            return false
        }
        return true
    }

    private fun buildLaunchPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(this, 0, intent, flag)
    }

    /// 通过 handler 模拟一个进度递增的通知
    private fun simulateProgress() {
        val nm = NotificationManagerCompat.from(this)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("下载中")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        append("⏳ 开始模拟下载进度")

        val max = 100
        val handler = android.os.Handler(mainLooper)
        var progress = 0
        val step = object : Runnable {
            override fun run() {
                progress += 10
                if (progress <= max) {
                    builder.setProgress(max, progress, false)
                        .setContentText("已完成 $progress%")
                    nm.notify(NOTI_ID_PROGRESS, builder.build())
                    handler.postDelayed(this, 400)
                } else {
                    builder.setContentText("下载完成 ✅")
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                    nm.notify(NOTI_ID_PROGRESS, builder.build())
                    append("✅ 进度通知完成")
                }
            }
        }
        handler.post(step)
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }
}
