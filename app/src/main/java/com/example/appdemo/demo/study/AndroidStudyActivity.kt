package com.example.appdemo.demo.study

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar

class AndroidStudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_floating_ball_study)
        setupDemoToolbar(R.string.android_study_label, R.id.floating_ball_study_root)

        findViewById<Button>(R.id.btn_start_floating_ball).setOnClickListener { requestAndStart() }
        findViewById<Button>(R.id.btn_stop_floating_ball).setOnClickListener {
            FloatingBallService.stop(this)
            Toast.makeText(this, "悬浮球已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从设置页返回后自动检查权限并启动
        if (hasOverlayPermission()) {
            try {
                FloatingBallService.start(this)
            } catch (e: Exception) {
                Toast.makeText(this, "启动悬浮球失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestAndStart() {
        if (!hasOverlayPermission()) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            Toast.makeText(this, "请开启「显示在其他应用上层」权限", Toast.LENGTH_LONG).show()
            startActivity(intent)
        } else {
            FloatingBallService.start(this)
            Toast.makeText(this, "悬浮球已启动，可在屏幕边缘找到它", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
    }
}
