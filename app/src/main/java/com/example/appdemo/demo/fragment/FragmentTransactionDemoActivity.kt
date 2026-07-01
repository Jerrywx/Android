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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FragmentTransaction 事务操作演示。
 *
 * 覆盖的操作：
 *   1. add        —— 在容器中新增 Fragment，之前的 Fragment 视图仍存在
 *   2. replace    —— 移除容器中所有 Fragment，添加新的
 *   3. show/hide  —— 只切换可见性，Fragment 视图与状态都保留
 *   4. attach/detach —— 销毁视图但保留实例，重新 attach 会重建视图
 *   5. remove     —— 完全移除 Fragment
 *   6. addToBackStack —— 加入回退栈，按返回键可回滚事务
 *   7. popBackStack —— 手动出栈
 *
 * 页面上会实时展示 FragmentManager 中的所有 Fragment 列表和回退栈条数。
 */
class FragmentTransactionDemoActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private lateinit var infoView: TextView
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_transaction)
        setupDemoToolbar(R.string.frag_transaction, R.id.fragment_tx_root)

        logView = findViewById(R.id.tv_tx_log)
        infoView = findViewById(R.id.tv_tx_info)

        findViewById<View>(R.id.btn_tx_add_a).setOnClickListener { addPage("A", Color.parseColor("#FFEAF4FF")) }
        findViewById<View>(R.id.btn_tx_add_b).setOnClickListener { addPage("B", Color.parseColor("#FFFFF3E0")) }
        findViewById<View>(R.id.btn_tx_replace_c).setOnClickListener { replacePage("C", Color.parseColor("#FFE8F5E9")) }
        findViewById<View>(R.id.btn_tx_hide).setOnClickListener { toggleVisibility(hide = true) }
        findViewById<View>(R.id.btn_tx_show).setOnClickListener { toggleVisibility(hide = false) }
        findViewById<View>(R.id.btn_tx_attach).setOnClickListener { toggleAttach(attach = true) }
        findViewById<View>(R.id.btn_tx_detach).setOnClickListener { toggleAttach(attach = false) }
        findViewById<View>(R.id.btn_tx_pop).setOnClickListener {
            log("→ popBackStack()")
            supportFragmentManager.popBackStack()
        }
        findViewById<View>(R.id.btn_tx_clear).setOnClickListener {
            log("→ 清空所有 Fragment")
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val list = supportFragmentManager.fragments.toList()
            supportFragmentManager.beginTransaction().apply {
                list.forEach { remove(it) }
            }.commit()
        }

        supportFragmentManager.addOnBackStackChangedListener { refreshInfo() }
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, s: Bundle?) {
                    refreshInfo()
                }
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    refreshInfo()
                }
                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    refreshInfo()
                }
            }, false,
        )
        refreshInfo()
    }

    private fun addPage(tag: String, color: Int) {
        log("→ add(TxPageFragment[$tag]) 并加入回退栈")
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_tx_container, TxPageFragment.newInstance(tag, color), tag)
            .addToBackStack("add-$tag")
            .commit()
    }

    private fun replacePage(tag: String, color: Int) {
        log("→ replace(TxPageFragment[$tag]) 并加入回退栈")
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_tx_container, TxPageFragment.newInstance(tag, color), tag)
            .addToBackStack("replace-$tag")
            .commit()
    }

    private fun toggleVisibility(hide: Boolean) {
        val target = supportFragmentManager.fragments.lastOrNull()
        if (target == null) {
            log("⚠ 容器为空，请先添加 Fragment")
            return
        }
        log("→ ${if (hide) "hide" else "show"}(${target.tag})  视图保留、可见性切换")
        supportFragmentManager.beginTransaction().apply {
            if (hide) hide(target) else show(target)
        }.commit()
        refreshInfo()
    }

    private fun toggleAttach(attach: Boolean) {
        val target = supportFragmentManager.fragments.lastOrNull()
        if (target == null) {
            log("⚠ 容器为空，请先添加 Fragment")
            return
        }
        log("→ ${if (attach) "attach" else "detach"}(${target.tag})  ${if (attach) "重建视图" else "销毁视图但保留实例"}")
        supportFragmentManager.beginTransaction().apply {
            if (attach) attach(target) else detach(target)
        }.commit()
        refreshInfo()
    }

    private fun refreshInfo() {
        val fragments = supportFragmentManager.fragments
        val stack = supportFragmentManager.backStackEntryCount
        val sb = StringBuilder()
        sb.append("容器 Fragment 列表：\n")
        if (fragments.isEmpty()) {
            sb.append("  （空）\n")
        } else {
            fragments.forEachIndexed { idx, f ->
                val vis = if (f.isVisible) "可见" else if (f.isHidden) "隐藏" else "无视图"
                val det = if (f.isDetached) "·detached" else ""
                sb.append("  ${idx + 1}. tag=${f.tag}  $vis$det\n")
            }
        }
        sb.append("\n回退栈条目数：$stack")
        infoView.text = sb
    }

    private fun log(msg: String) {
        val line = "[${timeFmt.format(Date())}] $msg"
        val old = logView.text.toString()
        logView.text = if (old.isEmpty()) line else "$old\n$line"
    }

    /** 演示用页面 Fragment，通过参数控制颜色与标签。 */
    class TxPageFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            val root = inflater.inflate(R.layout.fragment_tx_page, container, false)
            val tag = arguments?.getString(ARG_TAG) ?: "?"
            val color = arguments?.getInt(ARG_COLOR) ?: Color.LTGRAY
            root.setBackgroundColor(color)
            root.findViewById<TextView>(R.id.tv_tx_page_tag).text = "Fragment $tag"
            root.findViewById<TextView>(R.id.tv_tx_page_hash).text = "实例 hash: ${System.identityHashCode(this).toString(16)}"
            return root
        }

        companion object {
            private const val ARG_TAG = "tag"
            private const val ARG_COLOR = "color"
            fun newInstance(tag: String, color: Int) = TxPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAG, tag)
                    putInt(ARG_COLOR, color)
                }
            }
        }
    }
}
