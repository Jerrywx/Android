package com.example.appdemo.demo.fragment

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment 生命周期演示。
 *
 * 展示 12 个关键回调：
 *   onAttach → onCreate → onCreateView → onViewCreated → onViewStateRestored
 *   → onStart → onResume → onPause → onStop → onDestroyView → onDestroy → onDetach
 *
 * 提供三个按钮：Add / Remove / Recreate 来触发不同的生命周期路径。
 */
class FragmentLifecycleDemoActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_lifecycle)
        setupDemoToolbar(R.string.frag_lifecycle, R.id.fragment_lifecycle_root)

        logView = findViewById(R.id.tv_lifecycle_log)
        appendLog("Activity onCreate — savedInstanceState=${savedInstanceState != null}")

        findViewById<View>(R.id.btn_lifecycle_add).setOnClickListener { addFragment() }
        findViewById<View>(R.id.btn_lifecycle_remove).setOnClickListener { removeFragment() }
        findViewById<View>(R.id.btn_lifecycle_clear).setOnClickListener { logView.text = "" }
    }

    private fun addFragment() {
        if (supportFragmentManager.findFragmentByTag(TAG) != null) {
            appendLog("Fragment 已经存在，忽略 Add")
            return
        }
        appendLog("→ 主动 Add Fragment")
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_lifecycle_container, ChildFragment(), TAG)
            .commit()
    }

    private fun removeFragment() {
        val f = supportFragmentManager.findFragmentByTag(TAG) ?: run {
            appendLog("Fragment 不存在，忽略 Remove")
            return
        }
        appendLog("→ 主动 Remove Fragment")
        supportFragmentManager.beginTransaction().remove(f).commit()
    }

    private fun recreateFragment() {
        appendLog("→ Recreate Activity（模拟旋转 / 重建）")
        recreate()
    }

    fun appendLog(msg: String) {
        val line = "[${timeFmt.format(Date())}] $msg"
        val old = logView.text.toString()
        logView.text = if (old.isEmpty()) line else "$old\n$line"
    }

    /** 演示用子 Fragment，重写全部关键生命周期回调。 */
    class ChildFragment : Fragment() {

        private fun log(msg: String) {
            (activity as? FragmentLifecycleDemoActivity)?.appendLog("[Fragment] $msg")
        }

        override fun onAttach(context: android.content.Context) {
            super.onAttach(context)
            log("onAttach")
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            log("onCreate — savedInstanceState=${savedInstanceState != null}")
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            log("onCreateView")
            return inflater.inflate(R.layout.fragment_lifecycle_child, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            log("onViewCreated")
            view.findViewById<TextView>(R.id.tv_child_state).text = "已就绪"
        }

        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)
            log("onViewStateRestored")
        }

        override fun onStart() {
            super.onStart()
            log("onStart")
        }

        override fun onResume() {
            super.onResume()
            log("onResume  ✅ 可见 + 可交互")
        }

        override fun onPause() {
            super.onPause()
            log("onPause")
        }

        override fun onStop() {
            super.onStop()
            log("onStop")
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            log("onSaveInstanceState")
        }

        override fun onDestroyView() {
            super.onDestroyView()
            log("onDestroyView")
        }

        override fun onDestroy() {
            super.onDestroy()
            log("onDestroy")
        }

        override fun onDetach() {
            super.onDetach()
            log("onDetach")
        }
    }

    companion object {
        private const val TAG = "lifecycle_child"
    }
}
