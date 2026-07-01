package com.example.appdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel

/**
 * Fragment 状态保存对比演示。
 *
 * 两种方案：
 *   1. onSaveInstanceState + Bundle：适合轻量、可序列化的 UI 状态
 *   2. ViewModel：横竖屏切换零成本，适合大对象、异步数据
 *
 * 点击右上角"模拟旋转"按钮，Activity 会被销毁重建，观察两个 Fragment 的表现差异。
 */
class FragmentSaveStateDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_save_state)
        setupDemoToolbar(R.string.frag_state_save, R.id.fragment_save_root)

        findViewById<View>(R.id.btn_save_recreate).setOnClickListener { recreate() }
    }

    /** 使用 onSaveInstanceState 保存输入内容和计数。 */
    class BundleFragment : Fragment() {

        private var count = 0
        private var text = ""

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            val view = inflater.inflate(R.layout.fragment_save_page, container, false)
            view.findViewById<TextView>(R.id.tv_save_kind).text = "Bundle 保存（onSaveInstanceState）"

            savedInstanceState?.let {
                count = it.getInt(KEY_COUNT, 0)
                text = it.getString(KEY_TEXT).orEmpty()
            }

            val edit = view.findViewById<EditText>(R.id.et_save_input)
            val countView = view.findViewById<TextView>(R.id.tv_save_count)

            edit.setText(text)
            countView.text = getString(R.string.frag_save_count, count)

            edit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    text = s?.toString().orEmpty()
                }
            })

            view.findViewById<View>(R.id.btn_save_hit).setOnClickListener {
                count++
                countView.text = getString(R.string.frag_save_count, count)
            }
            return view
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(KEY_COUNT, count)
            outState.putString(KEY_TEXT, text)
        }

        companion object {
            private const val KEY_COUNT = "count"
            private const val KEY_TEXT = "text"
        }
    }

    /** 使用 ViewModel 保存状态，横竖屏切换后仍在。 */
    class VmFragment : Fragment() {

        private val vm: SaveVm by viewModels()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            val view = inflater.inflate(R.layout.fragment_save_page, container, false)
            view.findViewById<TextView>(R.id.tv_save_kind).text = "ViewModel 保存（配置变更零成本）"

            val edit = view.findViewById<EditText>(R.id.et_save_input)
            val countView = view.findViewById<TextView>(R.id.tv_save_count)

            edit.setText(vm.text)
            countView.text = getString(R.string.frag_save_count, vm.count)

            edit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    vm.text = s?.toString().orEmpty()
                }
            })

            view.findViewById<View>(R.id.btn_save_hit).setOnClickListener {
                vm.count++
                countView.text = getString(R.string.frag_save_count, vm.count)
            }
            return view
        }
    }

    class SaveVm : ViewModel() {
        var count = 0
        var text = ""
    }
}
