package com.example.appdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Fragment Result API 通信演示。
 *
 * 官方推荐的 Fragment 之间（含 Activity ↔ Fragment）通信方式，
 * 优点：
 *   - 无需接口回调、EventBus、SharedViewModel
 *   - 生命周期感知，避免内存泄漏
 *   - 支持宿主重建后依然能收到最近一次结果
 *
 * 关键 API：
 *   setFragmentResult(requestKey, bundle)                // 发送
 *   setFragmentResultListener(requestKey) { key, bundle } // 接收
 *
 * 本例展示：宿主 Activity 监听子 Fragment 的输入结果并显示。
 */
class FragmentResultDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_result)
        setupDemoToolbar(R.string.frag_result, R.id.fragment_result_root)

        val receiver = findViewById<TextView>(R.id.tv_result_received)
        receiver.text = getString(R.string.frag_result_none)

        // 注册结果监听（宿主 Activity 端）
        supportFragmentManager.setFragmentResultListener(
            SenderFragment.REQ_KEY,
            this,
        ) { _, bundle ->
            val text = bundle.getString(SenderFragment.RESULT_KEY).orEmpty()
            receiver.text = getString(R.string.frag_result_received, text)
        }

        // 插入发送端 Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_result_container, SenderFragment())
                .commit()
        }
    }

    /** 发送端 Fragment：通过 EditText 输入 + 按钮触发 setFragmentResult。 */
    class SenderFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_result_sender, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val input = view.findViewById<EditText>(R.id.et_result_input)
            view.findViewById<View>(R.id.btn_result_send).setOnClickListener {
                val text = input.text.toString().ifBlank { "Hello Fragment" }
                // 通过 parentFragmentManager 把结果送给宿主 Activity 已注册的监听
                parentFragmentManager.setFragmentResult(
                    REQ_KEY,
                    Bundle().apply { putString(RESULT_KEY, text) },
                )
            }
        }

        companion object {
            const val REQ_KEY = "req_sender"
            const val RESULT_KEY = "content"
        }
    }
}
