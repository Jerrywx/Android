package com.example.appdemo.demo.network

import com.example.appdemo.R
import com.example.appdemo.common.*
import com.example.appdemo.tabs.*
import com.example.appdemo.demo.layout.*
import com.example.appdemo.demo.concurrent.*
import com.example.appdemo.demo.network.*
import com.example.appdemo.demo.animation.*
import com.example.appdemo.demo.animation.widget.*
import com.example.appdemo.demo.viewpager.*
import com.example.appdemo.demo.recyclerview.*
import com.example.appdemo.demo.recyclerview.chat.*
import com.example.appdemo.demo.fragment.*

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 网络请求 Demo —— Retrofit + OkHttp + 协程的完整实战示例。
 *
 * 涵盖：
 *   1) GET 请求获取列表
 *   2) GET 请求带路径参数
 *   3) POST 请求带 JSON 请求体
 *   4) 异常处理（HttpException / IOException）
 *
 * 所有网络调用都在协程中进行，UI 自动回到主线程更新。
 */
class AndroidNetworkActivity : AppCompatActivity() {

    /// 与 Activity 生命周期绑定的协程作用域
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /// Retrofit 接口实例
    private val api = RetrofitClient.api

    /// 日志时间戳格式
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_network)
        setupDemoToolbar(R.string.network_title, R.id.network_root)

        bindGetListDemo()
        bindGetSingleDemo()
        bindPostDemo()
        bindErrorDemo()
    }

    // ───────────────────────────── 辅助方法 ─────────────────────────────

    /** 向 TextView 追加一行日志 */
    private fun log(tv: TextView, msg: String) {
        val time = timeFmt.format(Date())
        val existing = tv.text.toString()
        val line = "[$time] $msg"
        tv.text = if (existing.isBlank() ||
            existing.startsWith("点击") ||
            existing.startsWith("输入")
        ) {
            line
        } else {
            "$existing\n$line"
        }
    }

    /** 清空一组 TextView */
    private fun clear(tv: TextView, hint: String = "点击上方按钮发起请求") {
        tv.text = hint
    }

    // ───────────────────────────── Demo 1: GET 列表 ─────────────────────────────

    private fun bindGetListDemo() {
        val result = findViewById<TextView>(R.id.tv_get_list_result)

        findViewById<TextView>(R.id.btn_get_list).setOnClickListener {
            log(result, "🚀 发起 GET /users 请求")

            scope.launch {
                try {
                    /// suspend 函数直接调用，Retrofit 会在 IO 线程执行
                    val users = api.getUsers()
                    log(result, "✅ 请求成功，共返回 ${users.size} 条数据")
                    users.take(5).forEachIndexed { i, u ->
                        log(result, "   ${i + 1}. ${u.name} (@${u.username}) - ${u.email}")
                    }
                    if (users.size > 5) {
                        log(result, "   …（其余 ${users.size - 5} 条省略）")
                    }
                } catch (e: HttpException) {
                    log(result, "❌ HTTP 错误：${e.code()} ${e.message()}")
                } catch (e: IOException) {
                    log(result, "❌ 网络错误：${e.message}")
                } catch (e: Exception) {
                    log(result, "❌ 未知异常：${e.message}")
                }
            }
        }

        findViewById<TextView>(R.id.btn_get_list_clear).setOnClickListener { clear(result) }
    }

    // ───────────────────────────── Demo 2: GET 单个 ─────────────────────────────

    private fun bindGetSingleDemo() {
        val result = findViewById<TextView>(R.id.tv_get_single_result)
        val input = findViewById<EditText>(R.id.et_user_id)

        findViewById<TextView>(R.id.btn_get_single).setOnClickListener {
            val id = input.text.toString().toIntOrNull() ?: 1
            log(result, "🚀 发起 GET /users/$id 请求")

            scope.launch {
                try {
                    val user = api.getUser(id)
                    log(result, "✅ 请求成功")
                    log(result, "   👤 ${user.name} (@${user.username})")
                    log(result, "   📧 ${user.email}")
                    log(result, "   📞 ${user.phone ?: "—"}")
                    log(result, "   🌐 ${user.website ?: "—"}")
                } catch (e: HttpException) {
                    log(result, "❌ HTTP ${e.code()}：用户不存在或服务异常")
                } catch (e: IOException) {
                    log(result, "❌ 网络错误：${e.message}")
                } catch (e: Exception) {
                    log(result, "❌ 异常：${e.message}")
                }
            }
        }
    }

    // ───────────────────────────── Demo 3: POST ─────────────────────────────

    private fun bindPostDemo() {
        val result = findViewById<TextView>(R.id.tv_post_result)
        val titleInput = findViewById<EditText>(R.id.et_post_title)
        val bodyInput = findViewById<EditText>(R.id.et_post_body)

        findViewById<TextView>(R.id.btn_post_demo).setOnClickListener {
            val title = titleInput.text.toString().ifBlank { "默认标题" }
            val body = bodyInput.text.toString().ifBlank { "默认内容" }

            log(result, "🚀 发起 POST /posts 请求")
            log(result, "   请求体：")
            log(result, "   { userId=1, title=\"$title\", body=\"$body\" }")

            scope.launch {
                try {
                    /// 构造 Post 数据类，Gson 会自动将其序列化为 JSON
                    val newPost = Post(userId = 1, title = title, body = body)
                    val created = api.createPost(newPost)
                    log(result, "✅ 创建成功，服务端返回：")
                    log(result, "   id = ${created.id}")
                    log(result, "   userId = ${created.userId}")
                    log(result, "   title = ${created.title}")
                    log(result, "   body = ${created.body}")
                } catch (e: HttpException) {
                    log(result, "❌ HTTP 错误：${e.code()}")
                } catch (e: IOException) {
                    log(result, "❌ 网络错误：${e.message}")
                } catch (e: Exception) {
                    log(result, "❌ 异常：${e.message}")
                }
            }
        }

        findViewById<TextView>(R.id.btn_post_clear).setOnClickListener {
            clear(result, "点击发送 POST 请求")
        }
    }

    // ───────────────────────────── Demo 4: 异常处理 ─────────────────────────────

    private fun bindErrorDemo() {
        val result = findViewById<TextView>(R.id.tv_error_result)

        findViewById<TextView>(R.id.btn_error_demo).setOnClickListener {
            log(result, "🚀 故意请求一个不存在的资源 /users/9999")

            scope.launch {
                try {
                    val user = api.getUser(9999)
                    log(result, "意外得到结果：$user")
                } catch (e: HttpException) {
                    log(result, "✅ 捕获到 HttpException")
                    log(result, "   状态码：${e.code()}")
                    log(result, "   消息：${e.message()}")
                    log(result, "   说明：服务端返回了非 2xx 响应")
                } catch (e: IOException) {
                    log(result, "✅ 捕获到 IOException")
                    log(result, "   消息：${e.message}")
                    log(result, "   说明：网络连接失败或超时")
                } catch (e: Exception) {
                    log(result, "✅ 捕获到其他异常：${e.javaClass.simpleName}")
                    log(result, "   消息：${e.message}")
                }
            }
        }

        findViewById<TextView>(R.id.btn_error_clear).setOnClickListener {
            clear(result, "点击触发一次失败请求")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
