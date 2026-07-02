package com.example.appdemo.demo.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.appdemo.R

/**
 * 前台 Service —— 必须在 5 秒内调用 startForeground 提交常驻通知，否则 ANR/异常。
 * 场景：音乐播放、位置追踪、下载器等用户可感知的长时任务。
 */
class DemoForegroundService : Service() {

    private companion object {
        const val CHANNEL_ID = "demo_foreground_channel"
        const val NOTI_ID = 2001
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val noti = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Demo 前台 Service")
            .setContentText("常驻通知栏，点停止按钮可关闭")
            .setOngoing(true)
            .build()
        /// startForeground 必须在 5 秒内调用
        startForeground(NOTI_ID, noti)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Demo 前台 Service",
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }
}
