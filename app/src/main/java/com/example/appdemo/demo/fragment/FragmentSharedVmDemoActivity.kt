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
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 共享 ViewModel 演示。
 *
 * 场景：宿主 Activity 内有两个兄弟 Fragment，
 *   - 左侧：输入并广播消息
 *   - 右侧：观察并显示消息
 *
 * 通过 activityViewModels()，两个 Fragment 拿到同一个 ViewModel 实例，
 * 是 Fragment 之间通信最简单也最推荐的方案，天然支持配置变更保留。
 */
class FragmentSharedVmDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_shared_vm)
        setupDemoToolbar(R.string.frag_shared_vm, R.id.fragment_shared_root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_shared_left, SenderFragment())
                .replace(R.id.container_shared_right, ReceiverFragment())
                .commit()
        }
    }

    /** 宿主 Activity 作用域下的 ViewModel，供两个 Fragment 共享。 */
    class SharedVm : ViewModel() {
        val message = MutableLiveData<String>("")
    }

    /** 发送端：使用 activityViewModels() 拿到与 Activity 作用域绑定的实例。 */
    class SenderFragment : Fragment() {

        private val vm: SharedVm by activityViewModels()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_shared_sender, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val input = view.findViewById<EditText>(R.id.et_shared_input)
            view.findViewById<View>(R.id.btn_shared_send).setOnClickListener {
                vm.message.value = input.text.toString()
            }
        }
    }

    /** 接收端：同样通过 activityViewModels() 拿到同一实例并观察 LiveData。 */
    class ReceiverFragment : Fragment() {

        private val vm: SharedVm by activityViewModels()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_shared_receiver, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val text = view.findViewById<TextView>(R.id.tv_shared_current)
            // viewLifecycleOwner 是 Fragment 视图层生命周期，比 this(Fragment) 更安全
            vm.message.observe(viewLifecycleOwner) { value ->
                val shown = value.ifBlank { getString(R.string.frag_shared_empty) }
                text.text = getString(R.string.frag_shared_current, shown)
            }
        }
    }
}
