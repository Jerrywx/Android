package com.example.appdemo.demo.system

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * startService 演示用 —— 生命周期：onCreate → onStartCommand(N 次) → onDestroy。
 * 不返回 Binder，属于"发射后不管"的任务，需要外部 stopService 才结束。
 */
class DemoStartedService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand id=$startId")
        /// START_STICKY：进程被杀后系统尝试重新拉起 Service（intent 为 null）
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    private companion object {
        const val TAG = "DemoStartedService"
    }
}
