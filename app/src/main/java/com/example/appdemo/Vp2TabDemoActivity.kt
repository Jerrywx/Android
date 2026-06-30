package com.example.appdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Vp2TabDemoActivity : AppCompatActivity() {

    private val tabs by lazy {
        listOf(
            getString(R.string.vp2_tab_recommend),
            getString(R.string.vp2_tab_video),
            getString(R.string.vp2_tab_music),
            getString(R.string.vp2_tab_topic),
            getString(R.string.vp2_tab_news),
            getString(R.string.vp2_tab_live),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vp2_tab)
        setupDemoToolbar(R.string.vp2_tab_title)

        val pager = findViewById<ViewPager2>(R.id.vp2_tab_pager)
        val tabLayout = findViewById<TabLayout>(R.id.vp2_tab_layout)

        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabs.size
            override fun createFragment(position: Int): Fragment =
                Vp2TabPageFragment.newInstance(tabs[position], position)
        }
        pager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }
}
