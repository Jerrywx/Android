package com.example.appdemo

import com.example.appdemo.tabs.WeChatFragment
import com.example.appdemo.tabs.ContactsFragment
import com.example.appdemo.tabs.DiscoverFragment
import com.example.appdemo.tabs.MeFragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager2

    private val tabs = listOf(
        TabSpec(R.id.tab_wechat, R.id.tab_wechat_icon, R.id.tab_wechat_text) { WeChatFragment() },
        TabSpec(R.id.tab_contacts, R.id.tab_contacts_icon, R.id.tab_contacts_text) { ContactsFragment() },
        TabSpec(R.id.tab_discover, R.id.tab_discover_icon, R.id.tab_discover_text) { DiscoverFragment() },
        TabSpec(R.id.tab_me, R.id.tab_me_icon, R.id.tab_me_text) { MeFragment() },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        applyInsets()

        pager = findViewById(R.id.pager)
        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = tabs.size - 1
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].factory()
        }

        tabs.forEachIndexed { index, spec ->
            findViewById<View>(spec.containerId).setOnClickListener {
                pager.setCurrentItem(index, false)
            }
        }

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateSelection(position)
        })
        updateSelection(0)
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun updateSelection(selected: Int) {
        tabs.forEachIndexed { index, spec ->
            val on = index == selected
            findViewById<View>(spec.containerId).isSelected = on
            findViewById<AppCompatImageView>(spec.iconId).isSelected = on
            findViewById<TextView>(spec.textId).isSelected = on
        }
    }

    private data class TabSpec(
        val containerId: Int,
        val iconId: Int,
        val textId: Int,
        val factory: () -> Fragment,
    )
}
