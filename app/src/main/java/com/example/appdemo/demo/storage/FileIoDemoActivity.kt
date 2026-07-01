package com.example.appdemo.demo.storage

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import java.io.File

/**
 * 文件读写演示 —— Android 三种常用目录对比。
 *
 *   1) filesDir        应用私有目录，卸载即清除
 *   2) cacheDir        应用私有缓存，系统空间不足可能被清理
 *   3) getExternalFilesDir 应用私有外部存储，无需权限，卸载清除
 *
 * 演示要点：
 *   • Kotlin 扩展方法 writeText / readText 简化 IO
 *   • 一定要用 use {} 确保流关闭（这里 writeText/readText 内部已处理）
 */
class FileIoDemoActivity : AppCompatActivity() {

    private companion object {
        const val FILE_NAME = "demo_note.txt"
    }

    private lateinit var log: TextView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_storage_file)
        setupDemoToolbar(R.string.storage_file_title, R.id.storage_file_root)

        log = findViewById(R.id.tv_file_log)
        input = findViewById(R.id.et_file_content)

        appendLog("📂 filesDir  = ${filesDir.absolutePath}")
        appendLog("📂 cacheDir  = ${cacheDir.absolutePath}")
        appendLog("📂 extFiles  = ${getExternalFilesDir(null)?.absolutePath ?: "—"}")

        findViewById<TextView>(R.id.btn_file_write_internal).setOnClickListener {
            File(filesDir, FILE_NAME).writeText(input.text.toString())
            appendLog("✍️ 已写入 filesDir/$FILE_NAME")
        }

        findViewById<TextView>(R.id.btn_file_write_cache).setOnClickListener {
            File(cacheDir, FILE_NAME).writeText(input.text.toString())
            appendLog("✍️ 已写入 cacheDir/$FILE_NAME")
        }

        findViewById<TextView>(R.id.btn_file_write_external).setOnClickListener {
            val dir = getExternalFilesDir(null)
            if (dir == null) {
                appendLog("⚠️ 外部存储不可用")
            } else {
                File(dir, FILE_NAME).writeText(input.text.toString())
                appendLog("✍️ 已写入 extFiles/$FILE_NAME")
            }
        }

        findViewById<TextView>(R.id.btn_file_read_internal).setOnClickListener { read(File(filesDir, FILE_NAME), "filesDir") }
        findViewById<TextView>(R.id.btn_file_read_cache).setOnClickListener { read(File(cacheDir, FILE_NAME), "cacheDir") }
        findViewById<TextView>(R.id.btn_file_read_external).setOnClickListener {
            val dir = getExternalFilesDir(null)
            if (dir == null) appendLog("⚠️ 外部存储不可用") else read(File(dir, FILE_NAME), "extFiles")
        }

        findViewById<TextView>(R.id.btn_file_delete_all).setOnClickListener {
            listOfNotNull(
                File(filesDir, FILE_NAME) to "filesDir",
                File(cacheDir, FILE_NAME) to "cacheDir",
                getExternalFilesDir(null)?.let { File(it, FILE_NAME) to "extFiles" },
            ).forEach { (f, tag) ->
                if (f.exists() && f.delete()) appendLog("🗑️ 删除 $tag/$FILE_NAME")
            }
        }
    }

    private fun read(file: File, tag: String) {
        if (!file.exists()) {
            appendLog("📭 $tag/$FILE_NAME 不存在")
            return
        }
        appendLog("📖 $tag 内容：${file.readText()}")
    }

    private fun appendLog(msg: String) {
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) msg
        else "${log.text}\n$msg"
    }
}
