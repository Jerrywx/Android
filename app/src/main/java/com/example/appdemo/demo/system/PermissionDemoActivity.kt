package com.example.appdemo.demo.system

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 权限动态申请 —— 使用 ActivityResult API（推荐，替代已废弃的 onRequestPermissionsResult）。
 *
 * 三个场景：
 *   1) 单权限：CAMERA
 *   2) 多权限：定位 + 通知
 *   3) 永久拒绝：跳设置页手动开启
 *
 * 关键点：shouldShowRequestPermissionRationale 返回 false 且未授权 → 用户勾了"不再询问"
 */
class PermissionDemoActivity : AppCompatActivity() {

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /// 单权限 launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        append(if (granted) "✅ 相机权限：已授予" else "❌ 相机权限：被拒绝")
    }

    /// 多权限 launcher
    private val multiLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.forEach { (perm, granted) ->
            append("${if (granted) "✅" else "❌"} ${perm.substringAfterLast('.')} → $granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_permission)
        setupDemoToolbar(R.string.system_permission_title, R.id.system_perm_root)

        log = findViewById(R.id.tv_perm_log)

        findViewById<TextView>(R.id.btn_perm_camera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                append("✅ 已有相机权限，无需申请")
            } else {
                append("🔔 请求相机权限")
                cameraLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        findViewById<TextView>(R.id.btn_perm_multi).setOnClickListener {
            append("🔔 请求定位 + 通知权限")
            multiLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            )
        }

        findViewById<TextView>(R.id.btn_perm_settings).setOnClickListener {
            /// 权限被永久拒绝时，只能引导用户去系统设置页开启
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            append("↗️ 已跳转到系统设置页")
        }

        findViewById<TextView>(R.id.btn_perm_clear).setOnClickListener {
            log.text = getString(R.string.storage_log_hint)
        }
    }

    private fun append(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }
}
