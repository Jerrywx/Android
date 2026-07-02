package com.example.appdemo.demo.study

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.appdemo.R

class FloatingBallService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var ballView: TextView
    private var layoutParams: WindowManager.LayoutParams? = null

    private var downRawX = 0f
    private var downRawY = 0f
    private var downParamsX = 0
    private var downParamsY = 0
    private var isDragging = false
    private var screenWidth = 0
    private var added = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        screenWidth = resources.displayMetrics.widthPixels
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()
        ensureBallAdded()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeBall()
        super.onDestroy()
    }

    // ── 前台通知 ──────────────────────────────────────────────

    private var foregroundStarted = false

    private fun ensureForeground() {
        if (foregroundStarted) return
        foregroundStarted = true

        try {
            val channelId = "floating_ball"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(channelId, "悬浮球", NotificationManager.IMPORTANCE_MIN).apply {
                        description = "悬浮球运行中"
                        setShowBadge(false)
                    }
                )
            }

            val pi = PendingIntent.getActivity(
                this, 0,
                Intent(this, AndroidStudyActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val n = NotificationCompat.Builder(this, channelId)
                .setContentTitle("悬浮球运行中")
                .setContentText("点击悬浮球进入学习页面")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1001, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1001, n)
            }
        } catch (_: Exception) {
            foregroundStarted = false
        }
    }

    // ── 悬浮球 View ──────────────────────────────────────────

    private fun ensureBallAdded() {
        if (added) return
        added = true

        try {
            val dp = resources.displayMetrics.density
            val size = (56 * dp).toInt()

            ballView = TextView(this).apply {
                text = "学"
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundResource(R.drawable.bg_floating_ball)
                setOnTouchListener { _, event -> handleTouch(event); true }
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            layoutParams = WindowManager.LayoutParams(
                size, size, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = screenWidth - size
                y = 300
            }

            windowManager.addView(ballView, layoutParams)
        } catch (e: Exception) {
            added = false
        }
    }

    private fun removeBall() {
        if (added) {
            added = false
            try { windowManager.removeView(ballView) } catch (_: Exception) {}
        }
    }

    // ── 触摸 ─────────────────────────────────────────────────

    private fun handleTouch(event: MotionEvent) {
        val lp = layoutParams ?: return
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX; downRawY = event.rawY
                downParamsX = lp.x; downParamsY = lp.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) isDragging = true
                if (isDragging) {
                    lp.x = (downParamsX + dx).toInt()
                    lp.y = (downParamsY + dy).toInt()
                    try { windowManager.updateViewLayout(ballView, lp) } catch (_: Exception) {}
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) snapToEdge(lp) else openStudyActivity()
            }
        }
    }

    private fun snapToEdge(lp: WindowManager.LayoutParams) {
        val bw = ballView.width
        if (bw == 0) return
        lp.x = if (lp.x + bw / 2 < screenWidth / 2) 0 else screenWidth - bw
        lp.y = lp.y.coerceIn(0, resources.displayMetrics.heightPixels - bw)
        try { windowManager.updateViewLayout(ballView, lp) } catch (_: Exception) {}
    }

    private fun openStudyActivity() {
        startActivity(Intent(this, AndroidStudyActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ── 静态入口 ─────────────────────────────────────────────

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FloatingBallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingBallService::class.java))
        }
    }
}
