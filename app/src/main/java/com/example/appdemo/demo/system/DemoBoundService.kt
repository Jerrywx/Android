package com.example.appdemo.demo.system

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * bindService 演示 —— Activity 通过 ServiceConnection 拿到 LocalBinder，
 * 直接调用 Service 提供的方法，用于同进程内 Activity ↔ Service 交互。
 */
class DemoBoundService : Service() {

    private val binder = LocalBinder()
    private var counter = 0

    inner class LocalBinder : Binder() {
        fun getService(): DemoBoundService = this@DemoBoundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /// 对外暴露的方法，Activity 拿到 Service 引用后可以直接调
    fun currentCount(): Int {
        counter += 1
        return counter
    }
}
