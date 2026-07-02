package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appdemo.R

/**
 * Navigation 演示的起点页 —— 输入一段文字，通过 NavGraph 跳到详情页。
 */
class NavHomeFragment : Fragment(R.layout.fragment_nav_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val input = view.findViewById<EditText>(R.id.et_nav_input)
        view.findViewById<TextView>(R.id.btn_nav_go).setOnClickListener {
            val name = input.text.toString().ifBlank { "游客" }
            val args = Bundle().apply {
                putInt("userId", (100..999).random())
                putString("userName", name)
            }
            /// 通过 findNavController 触发 action，配合 NavGraph 中定义的 action 完成跳转
            findNavController().navigate(R.id.action_home_to_detail, args)
        }
    }
}
