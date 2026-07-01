package com.example.appdemo

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener

/**
 * DialogFragment 演示。
 *
 * 展示 3 种常见形态：
 *   1. 完全自定义样式的 DialogFragment（推荐做法）
 *   2. 系统 AlertDialog 版本 —— 重写 onCreateDialog 使用 AlertDialog.Builder
 *   3. 全屏 DialogFragment —— setStyle 让弹窗铺满屏幕，适合表单类交互
 *
 * 3 种形态都通过 Fragment Result API 把用户选择回传给宿主 Activity。
 */
class FragmentDialogDemoActivity : AppCompatActivity() {

    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_dialog)
        setupDemoToolbar(R.string.frag_dialog, R.id.fragment_dialog_root)

        resultView = findViewById(R.id.tv_dialog_result)

        findViewById<View>(R.id.btn_dialog_custom).setOnClickListener {
            CustomDialog().show(supportFragmentManager, "custom")
        }
        findViewById<View>(R.id.btn_dialog_alert).setOnClickListener {
            AlertStyleDialog().show(supportFragmentManager, "alert")
        }
        findViewById<View>(R.id.btn_dialog_full).setOnClickListener {
            FullScreenDialog().show(supportFragmentManager, "full")
        }

        supportFragmentManager.setFragmentResultListener(KEY_RESULT, this) { _, bundle ->
            val outcome = bundle.getString(EXTRA_OUTCOME).orEmpty()
            resultView.text = getString(R.string.frag_dialog_result, outcome)
        }
    }

    /** 自定义样式 DialogFragment：完全自绘的对话框内容。 */
    class CustomDialog : DialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.dialog_custom, container, false)

        override fun onStart() {
            super.onStart()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog?.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.85f).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.findViewById<TextView>(R.id.btn_dialog_confirm).setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    KEY_RESULT,
                    bundleOf(EXTRA_OUTCOME to "自定义对话框 · 确定"),
                )
                dismiss()
            }
            view.findViewById<TextView>(R.id.btn_dialog_cancel).setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    KEY_RESULT,
                    bundleOf(EXTRA_OUTCOME to "自定义对话框 · 取消"),
                )
                dismiss()
            }
        }
    }

    /** 系统 AlertDialog 版：重写 onCreateDialog 直接返回一个 AlertDialog。 */
    class AlertStyleDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.frag_dialog_title)
                .setMessage(R.string.frag_dialog_message)
                .setPositiveButton(R.string.frag_common_confirm) { _, _ ->
                    parentFragmentManager.setFragmentResult(
                        KEY_RESULT,
                        bundleOf(EXTRA_OUTCOME to "AlertDialog · 确定"),
                    )
                }
                .setNegativeButton(R.string.frag_common_cancel) { _, _ ->
                    parentFragmentManager.setFragmentResult(
                        KEY_RESULT,
                        bundleOf(EXTRA_OUTCOME to "AlertDialog · 取消"),
                    )
                }
                .create()
        }
    }

    /** 全屏 DialogFragment：适合表单/复杂交互的全屏弹窗。 */
    class FullScreenDialog : DialogFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.Theme_AppDemo)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.dialog_fullscreen, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.findViewById<ImageButton>(R.id.btn_dialog_full_close).setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    KEY_RESULT,
                    bundleOf(EXTRA_OUTCOME to "全屏对话框 · 关闭"),
                )
                dismiss()
            }
            view.findViewById<TextView>(R.id.btn_dialog_full_done).setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    KEY_RESULT,
                    bundleOf(EXTRA_OUTCOME to "全屏对话框 · 完成"),
                )
                dismiss()
            }
        }
    }

    companion object {
        private const val KEY_RESULT = "dialog_result"
        private const val EXTRA_OUTCOME = "outcome"
    }
}
