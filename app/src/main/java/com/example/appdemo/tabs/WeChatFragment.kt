package com.example.appdemo.tabs

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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WeChatFragment : Fragment(R.layout.fragment_wechat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val list = view.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(view.context)
        list.adapter = MessageAdapter(FakeMessages.build()) { msg ->
            val intent = Intent(view.context, ChatActivity::class.java)
                .putExtra(ChatActivity.EXTRA_TITLE, msg.title)
            startActivity(intent)
        }
        list.addItemDecoration(
            DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
        )
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard(searchInput)
                }
            }
        })

        view.findViewById<ImageButton>(R.id.btn_star).setOnClickListener {
            Toast.makeText(view.context, "星标", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<ImageButton>(R.id.btn_plus).setOnClickListener {
            Toast.makeText(view.context, "添加", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard(focused: View) {
        if (!focused.hasFocus()) return
        val imm = focused.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focused.windowToken, 0)
        focused.clearFocus()
    }
}
