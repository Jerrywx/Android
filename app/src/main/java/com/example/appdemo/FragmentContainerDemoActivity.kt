package com.example.appdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * FragmentContainerView + 手动切换演示。
 *
 * 场景：类似底部导航栏 —— 每个 Tab 对应一个 Fragment，切换时不销毁，保留每个 Tab 内部状态。
 *
 * 实现要点：
 *   1. 首次进入用 add() 添加所有 Tab Fragment
 *   2. 切换时用 hide()/show()，视图与内部状态都保留
 *   3. 每个 Tab 内部维护自己的点击计数，切换回来后仍在
 */
class FragmentContainerDemoActivity : AppCompatActivity() {

    private val tabs = listOf(
        TabSpec("home", R.string.frag_container_tab_home),
        TabSpec("message", R.string.frag_container_tab_message),
        TabSpec("profile", R.string.frag_container_tab_profile),
    )
    private var currentTag: String = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_container)
        setupDemoToolbar(R.string.frag_container, R.id.fragment_container_root)

        if (savedInstanceState == null) {
            val tx = supportFragmentManager.beginTransaction()
            tabs.forEach { t ->
                tx.add(R.id.fragment_container_host, ContainerPageFragment.newInstance(getString(t.title)), t.tag)
                if (t.tag != currentTag) tx.hide(supportFragmentManager.findFragmentByTag(t.tag) ?: return@forEach)
            }
            tx.commit()
        } else {
            currentTag = savedInstanceState.getString(STATE_TAG) ?: currentTag
        }

        bindTab(R.id.tab_container_home, "home")
        bindTab(R.id.tab_container_message, "message")
        bindTab(R.id.tab_container_profile, "profile")
        refreshTabSelection()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_TAG, currentTag)
    }

    private fun bindTab(viewId: Int, tag: String) {
        findViewById<View>(viewId).setOnClickListener { switchTo(tag) }
    }

    private fun switchTo(tag: String) {
        if (tag == currentTag) return
        val target = supportFragmentManager.findFragmentByTag(tag) ?: return
        val current = supportFragmentManager.findFragmentByTag(currentTag) ?: return
        supportFragmentManager.beginTransaction()
            .hide(current)
            .show(target)
            .commit()
        currentTag = tag
        refreshTabSelection()
    }

    private fun refreshTabSelection() {
        findViewById<TextView>(R.id.tab_container_home).isSelected = currentTag == "home"
        findViewById<TextView>(R.id.tab_container_message).isSelected = currentTag == "message"
        findViewById<TextView>(R.id.tab_container_profile).isSelected = currentTag == "profile"
    }

    private data class TabSpec(val tag: String, val title: Int)

    /**
     * 简单的 Tab 页 Fragment，内部维护点击计数以证明状态保留。
     */
    class ContainerPageFragment : Fragment() {

        private var count: Int = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            count = savedInstanceState?.getInt(STATE_COUNT) ?: 0
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_container_page, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val title = arguments?.getString(ARG_TITLE).orEmpty()
            val titleView = view.findViewById<TextView>(R.id.tv_container_title)
            val countView = view.findViewById<TextView>(R.id.tv_container_count)
            titleView.text = title
            refresh(view, title)
            view.findViewById<View>(R.id.btn_container_hit).setOnClickListener {
                count++
                refresh(view, title)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(STATE_COUNT, count)
        }

        private fun refresh(view: View, title: String) {
            val countView = view.findViewById<TextView>(R.id.tv_container_count)
            countView.text = getString(R.string.frag_container_page, title, count)
        }

        companion object {
            private const val ARG_TITLE = "title"
            private const val STATE_COUNT = "count"
            fun newInstance(title: String) = ContainerPageFragment().apply {
                arguments = Bundle().apply { putString(ARG_TITLE, title) }
            }
        }
    }

    companion object {
        private const val STATE_TAG = "current_tag"
    }
}
