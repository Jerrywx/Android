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

/**
 * Fragment 参数传递演示。
 *
 * 核心要点：
 *   1. 不要给 Fragment 写自定义构造函数 —— 配置变更后系统会用无参构造重建
 *   2. 通过静态 newInstance() 工厂方法 + Bundle 传参
 *   3. 使用 requireArguments() 表明「参数必须存在」，比 arguments?. 更安全
 *   4. Bundle 支持基础类型 / Parcelable / Serializable / Array 等
 */
class FragmentArgsDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_args)
        setupDemoToolbar(R.string.frag_arguments, R.id.fragment_args_root)

        val nameInput = findViewById<EditText>(R.id.et_args_name)
        val ageInput = findViewById<EditText>(R.id.et_args_age)

        findViewById<View>(R.id.btn_args_create).setOnClickListener {
            val name = nameInput.text.toString().ifBlank { "Alice" }
            val age = ageInput.text.toString().toIntOrNull() ?: 18
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_args_container, ProfileFragment.newInstance(name, age))
                .commit()
        }
        findViewById<View>(R.id.btn_args_reset).setOnClickListener {
            supportFragmentManager.fragments.toList().forEach {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        }
    }

    /** 演示：通过 newInstance + Bundle 传参的最佳实践。 */
    class ProfileFragment : Fragment() {

        // 用 by lazy + requireArguments() 表明参数一定存在
        private val userName: String by lazy { requireArguments().getString(ARG_NAME).orEmpty() }
        private val userAge: Int by lazy { requireArguments().getInt(ARG_AGE) }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_args_profile, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.findViewById<TextView>(R.id.tv_args_display).text =
                getString(R.string.frag_args_display, userName, userAge)
            view.findViewById<TextView>(R.id.tv_args_extra).text =
                "arguments = { name=$userName, age=$userAge }"
        }

        companion object {
            private const val ARG_NAME = "name"
            private const val ARG_AGE = "age"

            /** 工厂方法：参数在此写入 Bundle，外部无法误传其它内容。 */
            fun newInstance(name: String, age: Int) = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putInt(ARG_AGE, age)
                }
            }
        }
    }
}
