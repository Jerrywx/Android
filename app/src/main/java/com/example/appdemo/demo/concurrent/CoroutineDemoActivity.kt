package com.example.appdemo.demo.concurrent

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
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoroutineDemoActivity : AppCompatActivity() {

    // 自管理协程作用域，绑定 Activity 生命周期
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 用于取消演示的 Job
    private var cancelJob: Job? = null

    // 用于进度条演示的 Job
    private var progressJob: Job? = null

    // 主线程 Handler（供 HandlerThread 示例使用）
    private val mainHandler = Handler(Looper.getMainLooper())

    // 时间戳格式化
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coroutine_demo)
        setupDemoToolbar(R.string.coroutine_title, R.id.coroutine_root)

        bindThreadDemo()
        bindHandlerDemo()
        bindLaunchDemo()
        bindAsyncDemo()
        bindDispatchersDemo()
        bindWithContextDemo()
        bindCancelDemo()
        bindFlowDemo()
        bindParallelDemo()
        bindProgressDemo()
    }

    // ───────────────────────────── 辅助方法 ─────────────────────────────

    /** 将一行日志追加到指定的 TextView */
    private fun log(tv: TextView, msg: String) {
        val time = timeFmt.format(Date())
        val existing = tv.text.toString()
        val line = "[$time] $msg"
        tv.text = if (existing.isBlank() || existing == "点击上方按钮查看执行结果") {
            line
        } else {
            "$existing\n$line"
        }
    }

    /** 清空指定 TextView */
    private fun clear(vararg tvs: TextView) {
        tvs.forEach { it.text = "点击上方按钮查看执行结果" }
    }

    /** 获取当前线程名称（缩短包名） */
    private val threadName: String
        get() {
            val full = Thread.currentThread().name
            return when {
                full.contains("main") -> "main"
                full.contains("Thread") -> full.substringAfterLast(" ")
                full.contains("kotlinx") -> full.substringAfterLast(" ")
                else -> full
            }
        }

    // ───────────────────────────── Demo 1: Thread ─────────────────────────────

    private fun bindThreadDemo() {
        val result = findViewById<TextView>(R.id.tv_thread_result)

        findViewById<TextView>(R.id.btn_thread_demo).setOnClickListener {
            log(result, "🚀 主线程启动新线程")
            Thread {
                logOnMain(result, "🧵 子线程开始工作，线程：${Thread.currentThread().name}")
                try {
                    // 模拟耗时任务
                    Thread.sleep(800)
                    logOnMain(result, "✅ 子线程任务完成 (模拟 800ms 计算)")
                    logOnMain(result, "📊 计算结果: 42")
                } catch (e: InterruptedException) {
                    logOnMain(result, "❌ 线程被中断: ${e.message}")
                }
                logOnMain(result, "🏁 子线程结束")
            }.apply {
                name = "worker-thread-1"
                start()
            }
            log(result, "⏩ 主线程继续执行（不阻塞）")
        }

        findViewById<TextView>(R.id.btn_thread_clear).setOnClickListener { clear(result) }
    }

    /** 在线程中安全地更新 UI */
    private fun logOnMain(tv: TextView, msg: String) {
        mainHandler.post { log(tv, msg) }
    }

    // ───────────────────────────── Demo 2: HandlerThread ─────────────────────────────

    private var handlerThread: HandlerThread? = null
    private var threadHandler: Handler? = null

    private fun bindHandlerDemo() {
        val result = findViewById<TextView>(R.id.tv_handler_result)

        findViewById<TextView>(R.id.btn_handler_demo).setOnClickListener {
            // 初始化 HandlerThread
            if (handlerThread == null) {
                handlerThread = HandlerThread("demo-worker").also { it.start() }
                threadHandler = Handler(handlerThread!!.looper)
                log(result, "✅ HandlerThread 已创建并启动")
            }

            log(result, "🚀 发送 3 个任务到 HandlerThread 队列")
            log(result, "   任务会串行执行，按顺序完成")

            threadHandler?.post {
                logOnMain(result, "📋 [任务1] 开始 — 线程: ${Thread.currentThread().name}")
                Thread.sleep(400)
                logOnMain(result, "✅ [任务1] 完成 (400ms)")
            }

            threadHandler?.post {
                logOnMain(result, "📋 [任务2] 开始 — 线程: ${Thread.currentThread().name}")
                Thread.sleep(300)
                logOnMain(result, "✅ [任务2] 完成 (300ms)")
            }

            threadHandler?.post {
                logOnMain(result, "📋 [任务3] 开始 — 线程: ${Thread.currentThread().name}")
                Thread.sleep(500)
                logOnMain(result, "✅ [任务3] 完成 (500ms)")
            }

            log(result, "⏩ 主线程继续（HandlerThread 异步处理中）")
        }

        findViewById<TextView>(R.id.btn_handler_clear).setOnClickListener {
            clear(result)
            // 清理 HandlerThread
            handlerThread?.quitSafely()
            handlerThread = null
            threadHandler = null
            log(result, "🧹 HandlerThread 已清理")
        }
    }

    // ───────────────────────────── Demo 3: launch ─────────────────────────────

    private fun bindLaunchDemo() {
        val result = findViewById<TextView>(R.id.tv_launch_result)

        findViewById<TextView>(R.id.btn_launch_demo).setOnClickListener {
            log(result, "🚀 scope.launch 启动协程")
            log(result, "   当前线程: $threadName")

            // 使用 scope 确保协程与 Activity 生命周期绑定
            scope.launch {
                log(result, "📌 协程开始 — 线程: $threadName")
                log(result, "    Dispatchers.Default 线程池执行")

                // 模拟耗时操作
                val result1 = doSomethingSlow("任务A", 600)
                log(result, "✅ $result1")

                val result2 = doSomethingSlow("任务B", 400)
                log(result, "✅ $result2")

                log(result, "🏁 所有任务顺序完成，耗时约 1000ms")
            }

            log(result, "⏩ 主线程没有阻塞！协程在后台执行")
        }

        findViewById<TextView>(R.id.btn_launch_clear).setOnClickListener { clear(result) }
    }

    /** 模拟耗时挂起函数 */
    private suspend fun doSomethingSlow(name: String, ms: Long): String {
        // withContext(Dispatchers.IO) 切换到 IO 线程池
        return withContext(Dispatchers.IO) {
            delay(ms) // 非阻塞挂起
            "$name 在 ${Thread.currentThread().name} 执行完毕 (${ms}ms)"
        }
    }

    // ───────────────────────────── Demo 4: async / await ─────────────────────────────

    private fun bindAsyncDemo() {
        val result = findViewById<TextView>(R.id.tv_async_result)

        findViewById<TextView>(R.id.btn_async_demo).setOnClickListener {
            log(result, "🚀 async/await 演示：并发执行两个任务")
            log(result, "   分别计算 1~100 的和 与 1~100 的积")

            scope.launch {
                log(result, "📌 协程开始 — 线程: $threadName")

                val startTime = System.currentTimeMillis()

                // 两个 async 并发执行
                val sumDeferred = async(Dispatchers.Default) {
                    log(result, "   🔶 async-求和 启动，线程: ${Thread.currentThread().name}")
                    var sum = 0
                    for (i in 1..100) {
                        delay(5) // 模拟每步耗时
                        sum += i
                    }
                    sum
                }

                val countDeferred = async(Dispatchers.Default) {
                    log(result, "   🔷 async-计数 启动，线程: ${Thread.currentThread().name}")
                    var count = 0
                    for (i in 1..100) {
                        delay(5)
                        count++
                    }
                    count
                }

                // await() 挂起等待结果，但两个任务已在并发执行
                val sum = sumDeferred.await()
                val count = countDeferred.await()

                val elapsed = System.currentTimeMillis() - startTime
                log(result, "📊 1~100 求和 = $sum")
                log(result, "📊 1~100 计数 = $count")
                log(result, "⏱ 两个任务并发完成，总耗时: ${elapsed}ms")
                log(result, "   （如果顺序执行需要 ~1000ms，并发只需 ~500ms）")
            }
        }

        findViewById<TextView>(R.id.btn_async_clear).setOnClickListener { clear(result) }
    }

    // ───────────────────────────── Demo 5: Dispatchers ─────────────────────────────

    private fun bindDispatchersDemo() {
        val result = findViewById<TextView>(R.id.tv_dispatchers_result)

        findViewById<TextView>(R.id.btn_dispatchers_demo).setOnClickListener {
            log(result, "🚀 对比四种 Dispatchers 的线程信息")

            scope.launch {
                // Main
                log(result, "━━━ Dispatchers.Main ━━━")
                log(result, "   线程: $threadName")
                log(result, "   用途: UI 更新、界面交互")

                // Default
                launch(Dispatchers.Default) {
                    log(result, "━━━ Dispatchers.Default ━━━")
                    log(result, "   线程: $threadName")
                    log(result, "   用途: CPU 密集型计算、排序、解析")
                }.join()

                // IO
                launch(Dispatchers.IO) {
                    log(result, "━━━ Dispatchers.IO ━━━")
                    log(result, "   线程: $threadName")
                    log(result, "   用途: 网络请求、文件读写、数据库")
                }.join()

                // Unconfined
                launch(Dispatchers.Unconfined) {
                    log(result, "━━━ Dispatchers.Unconfined ━━━")
                    log(result, "   线程: $threadName（跟随启动线程）")
                    log(result, "   用途: 不关心线程的快速操作")
                }.join()

                log(result, "")
                log(result, "💡 Default 和 IO 共享线程池，但 IO 允许更多线程")
            }
        }

        findViewById<TextView>(R.id.btn_dispatchers_clear).setOnClickListener { clear(result) }
    }

    // ───────────────────────────── Demo 6: withContext ─────────────────────────────

    private fun bindWithContextDemo() {
        val result = findViewById<TextView>(R.id.tv_withctx_result)

        findViewById<TextView>(R.id.btn_withctx_demo).setOnClickListener {
            log(result, "🚀 withContext 线程切换演示")
            log(result, "   模拟：主线程启动 → IO 线程取数据 → 主线程展示")

            scope.launch {
                log(result, "📌 1/3 当前线程: $threadName")

                // 切换到 IO 线程执行耗时操作
                val data = withContext(Dispatchers.IO) {
                    log(result, "📌 2/3 切换到 IO 线程: $threadName")
                    log(result, "    🔍 模拟网络请求 / 数据库查询...")
                    delay(800) // 模拟耗时
                    "用户数据: { name=Alice, score=98, level=5 }"
                }

                // 自动切换回原调度器（Main）
                log(result, "📌 3/3 回到主线程: $threadName")
                log(result, "✅ 获取到数据: $data")
                log(result, "🎉 可以在 UI 上安全展示结果了")
            }
        }

        findViewById<TextView>(R.id.btn_withctx_clear).setOnClickListener { clear(result) }
    }

    // ───────────────────────────── Demo 7: 取消与超时 ─────────────────────────────

    private fun bindCancelDemo() {
        val result = findViewById<TextView>(R.id.tv_cancel_result)

        findViewById<TextView>(R.id.btn_cancel_1).setOnClickListener {
            clear(result)
            log(result, "🚀 启动一个可取消的协程（模拟 10 步耗时任务）")

            cancelJob = scope.launch(Dispatchers.Default) {
                for (i in 1..10) {
                    // 检查是否被取消
                    if (!isActive) {
                        logOnMain(result, "❌ 协程已被取消，提前终止")
                        return@launch
                    }

                    logOnMain(result, "   🔄 步骤 $i/10 — 线程: $threadName")
                    delay(300) // 模拟每步耗时

                    if (!isActive) {
                        logOnMain(result, "❌ 协程在步骤 $i 被取消")
                        return@launch
                    }

                    // 也可以在耗时操作中主动抛出 CancellationException
                }
                logOnMain(result, "✅ 所有步骤完成（未被取消）")
            }

            log(result, "💡 点击「触发取消」按钮中断此任务")
        }

        findViewById<TextView>(R.id.btn_cancel_2).setOnClickListener {
            if (cancelJob?.isActive == true) {
                cancelJob?.cancel()
                log(result, "🛑 已发送取消信号！协程将在下一次挂起点停止")
                log(result, "   协程状态: isCancelled=${cancelJob?.isCancelled}")
            } else {
                log(result, "⚠️ 没有正在运行的协程，请先点击「取消 Demo」")
            }
        }
    }

    // ───────────────────────────── Demo 8: Flow ─────────────────────────────

    private fun bindFlowDemo() {
        val result = findViewById<TextView>(R.id.tv_flow_result)

        findViewById<TextView>(R.id.btn_flow_demo).setOnClickListener {
            log(result, "🚀 Flow 异步数据流演示")
            log(result, "   每秒发射一个数字，共 5 个，并应用 map 操作符")

            scope.launch {
                log(result, "📌 开始收集 Flow 数据流")

                try {
                    numberFlow()
                        .collect { value ->
                            log(result, "   📥 收集到: $value")
                        }
                } catch (e: Exception) {
                    log(result, "❌ Flow 异常: ${e.message}")
                }

                log(result, "🏁 Flow 数据流结束")
            }
        }

        findViewById<TextView>(R.id.btn_flow_clear).setOnClickListener { clear(result) }
    }

    private fun numberFlow(): Flow<String> = flow {
        for (i in 0 until 5) {
            delay(500) // 模拟异步延迟
            val mapped = "数字-$i (平方=${i * i})"
            emit(mapped) // 发射数据
        }
    }

    // ───────────────────────────── Demo 9: 并发请求实战 ─────────────────────────────

    private fun bindParallelDemo() {
        val result = findViewById<TextView>(R.id.tv_parallel_result)

        findViewById<TextView>(R.id.btn_parallel_demo).setOnClickListener {
            log(result, "🚀 实战：并发请求用户信息和商品列表")
            log(result, "   模拟网络请求，等两者都返回后合并展示")

            scope.launch {
                val startTime = System.currentTimeMillis()
                log(result, "📌 启动并发请求")

                // 并发启动两个"网络请求"
                val userDeferred = async(Dispatchers.IO) {
                    log(result, "   🔶 请求用户信息中...（模拟 600ms）")
                    delay(600)
                    UserInfo(uid = 1001, name = "Alice", avatar = "alice.png", level = 8)
                }

                val productsDeferred = async(Dispatchers.IO) {
                    log(result, "   🔷 请求商品列表中...（模拟 800ms）")
                    delay(800)
                    listOf(
                        Product("商品A", 29.9),
                        Product("商品B", 49.9),
                        Product("商品C", 99.0),
                    )
                }

                // 等待两个结果
                val user = userDeferred.await()
                val products = productsDeferred.await()

                val elapsed = System.currentTimeMillis() - startTime

                log(result, "")
                log(result, "━━━ 合并结果展示 ━━━")
                log(result, "👤 用户: ${user.name} (Lv.${user.level})")
                log(result, "🛒 商品列表:")
                products.forEachIndexed { i, p ->
                    log(result, "     ${i + 1}. ${p.name} ¥${p.price}")
                }
                log(result, "")
                log(result, "⏱ 总耗时: ${elapsed}ms（如果串行需 1400ms）")
            }
        }

        findViewById<TextView>(R.id.btn_parallel_clear).setOnClickListener { clear(result) }
    }

    private data class UserInfo(val uid: Int, val name: String, val avatar: String, val level: Int)
    private data class Product(val name: String, val price: Double)

    // ───────────────────────────── Demo 10: 进度条 ─────────────────────────────

    private fun bindProgressDemo() {
        val progressBar = findViewById<View>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.tv_progress_text)

        findViewById<TextView>(R.id.btn_progress_demo).setOnClickListener {
            // 取消上一次进度
            progressJob?.cancel()

            progressText.text = "进度：0%"
            progressBar.layoutParams?.width = 0
            progressBar.requestLayout()

            progressJob = scope.launch(Dispatchers.Default) {
                for (i in 0..100 step 5) {
                    delay(80) // 模拟耗时
                    val percent = i

                    // 切换到主线程更新 UI
                    withContext(Dispatchers.Main) {
                        // 更新进度条宽度
                        val totalWidth = findViewById<View>(R.id.section_progress).width - 32
                        progressBar.layoutParams?.width = totalWidth * percent / 100
                        progressBar.requestLayout()
                        progressText.text = "进度：${percent}%"
                    }
                }

                // 完成后切回主线程
                withContext(Dispatchers.Main) {
                    val totalWidth = findViewById<View>(R.id.section_progress).width - 32
                    progressBar.layoutParams?.width = totalWidth
                    progressBar.requestLayout()
                    progressText.text = "✅ 下载完成！进度：100%"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelJob?.cancel()
        progressJob?.cancel()
        scope.cancel()
        handlerThread?.quitSafely()
    }
}
