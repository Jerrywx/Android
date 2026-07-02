package com.example.appdemo.demo.jetpack

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appdemo.R

/**
 * NavGraph 的第二个目的地。通过 Bundle 接收从 NavHomeFragment 传来的参数，
 * 演示 Navigation 的返回栈弹出（findNavController().popBackStack）。
 */
class NavDetailFragment : Fragment(R.layout.fragment_nav_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getInt("userId") ?: -1
        val userName = arguments?.getString("userName") ?: "—"

        view.findViewById<TextView>(R.id.tv_nav_detail_info).text =
            getString(R.string.jetpack_nav_detail_info, userId, userName)

        view.findViewById<TextView>(R.id.btn_nav_detail_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
