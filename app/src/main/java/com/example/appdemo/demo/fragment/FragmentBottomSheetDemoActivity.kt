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
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialogFragment 演示。
 *
 * Material 底部抽屉，支持：
 *   - 拖拽收起 / 半展开状态
 *   - 完全自定义内容
 *   - 通过 Fragment Result API 回传选择
 */
class FragmentBottomSheetDemoActivity : AppCompatActivity() {

    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_bottom_sheet)
        setupDemoToolbar(R.string.frag_bottom_sheet, R.id.fragment_sheet_root)

        resultView = findViewById(R.id.tv_sheet_result)

        findViewById<View>(R.id.btn_sheet_open).setOnClickListener {
            ActionSheet().show(supportFragmentManager, "sheet")
        }

        supportFragmentManager.setFragmentResultListener(KEY_RESULT, this) { _, bundle ->
            val outcome = bundle.getString(EXTRA_ACTION).orEmpty()
            resultView.text = getString(R.string.frag_sheet_selected, outcome)
        }
    }

    class ActionSheet : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.sheet_bottom_actions, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            mapOf(
                R.id.sheet_action_share to "分享",
                R.id.sheet_action_save to "收藏",
                R.id.sheet_action_report to "举报",
                R.id.sheet_action_delete to "删除",
            ).forEach { (id, label) ->
                view.findViewById<TextView>(id).setOnClickListener { sendResult(label) }
            }
            view.findViewById<TextView>(R.id.sheet_action_cancel).setOnClickListener { dismiss() }
        }

        private fun sendResult(label: String) {
            parentFragmentManager.setFragmentResult(KEY_RESULT, bundleOf(EXTRA_ACTION to label))
            dismiss()
        }
    }

    companion object {
        private const val KEY_RESULT = "sheet_result"
        private const val EXTRA_ACTION = "action"
    }
}
