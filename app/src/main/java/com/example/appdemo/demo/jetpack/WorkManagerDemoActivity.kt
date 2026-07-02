package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.appdemo.R
import com.example.appdemo.common.setupDemoToolbar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManager 演示 —— 后台任务的正确姿势。
 *
 * 涵盖：
 *   1) OneTimeWorkRequest：一次性任务
 *   2) PeriodicWorkRequest：周期任务（最短 15 分钟，这里仅演示 API）
 *   3) 约束（网络 / 电量 / 充电中）
 *   4) 通过 LiveData<WorkInfo> 观察 State 变化
 *   5) 传参 setInputData / 结果 outputData
 */
class WorkManagerDemoActivity : AppCompatActivity() {

    private companion object {
        const val TAG_ONE = "demo_one_time"
        const val TAG_PERIODIC = "demo_periodic"
    }

    private lateinit var log: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jetpack_workmanager)
        setupDemoToolbar(R.string.jetpack_work_title, R.id.jetpack_work_root)

        log = findViewById(R.id.tv_work_log)
        val wm = WorkManager.getInstance(applicationContext)

        findViewById<TextView>(R.id.btn_work_one).setOnClickListener {
            /// 一次性任务 + 输入数据
            val req = OneTimeWorkRequestBuilder<DemoWorker>()
                .setInputData(workDataOf("input" to "从 Activity 传入"))
                .addTag(TAG_ONE)
                .build()
            wm.enqueue(req)
            appendLog("🚀 已入队 OneTimeWorkRequest id=${req.id}")

            /// LiveData 观察状态变化直到完成
            wm.getWorkInfoByIdLiveData(req.id).observe(this) { info ->
                info ?: return@observe
                appendLog("📡 状态：${info.state}")
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    val out = info.outputData.getString("output") ?: "—"
                    appendLog("✅ 输出：$out")
                }
            }
        }

        findViewById<TextView>(R.id.btn_work_constraint).setOnClickListener {
            /// 带约束的任务：需要联网 + 未充电时不跑
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val req = OneTimeWorkRequestBuilder<DemoWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("input" to "受约束的任务"))
                .build()
            wm.enqueue(req)
            appendLog("🚀 已入队带约束任务，需联网 + 电量 OK")
            appendLog("   状态会一直 ENQUEUED 直到约束满足")
        }

        findViewById<TextView>(R.id.btn_work_periodic).setOnClickListener {
            /// 周期任务：系统最小周期 15 分钟，这里只入队演示 API
            val req = PeriodicWorkRequestBuilder<DemoWorker>(15, TimeUnit.MINUTES)
                .addTag(TAG_PERIODIC)
                .build()
            wm.enqueueUniquePeriodicWork(
                TAG_PERIODIC,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
            appendLog("🔄 已入队周期任务（15 分钟）")
        }

        findViewById<TextView>(R.id.btn_work_cancel).setOnClickListener {
            wm.cancelAllWork()
            appendLog("🛑 已取消所有任务")
        }
    }

    private fun appendLog(msg: String) {
        val time = timeFmt.format(Date())
        val line = "[$time] $msg"
        log.text = if (log.text.isNullOrBlank() || log.text.startsWith("点击")) line
        else "${log.text}\n$line"
    }

    /**
     * 演示用 Worker —— 模拟耗时 5 秒的后台工作。
     * CoroutineWorker 直接支持 suspend / delay，比 Worker 更好用。
     */
    class DemoWorker(ctx: android.content.Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
        override suspend fun doWork(): Result {
            val input = inputData.getString("input") ?: "—"
            /// 模拟 5 秒工作
            delay(5000)
            val output = Data.Builder()
                .putString("output", "处理完成：$input @ ${System.currentTimeMillis()}")
                .build()
            return Result.success(output)
        }
    }
}
