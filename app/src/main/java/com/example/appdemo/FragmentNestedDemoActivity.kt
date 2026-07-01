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
 * 嵌套 Fragment 演示。
 *
 * 结构：Activity → ParentFragment → ChildFragment
 * 关键点：
 *   - 父 Fragment 需要用 childFragmentManager 来管理子 Fragment，
 *     不能用 parentFragmentManager，否则子 Fragment 会挂到 Activity。
 *   - 父 Fragment 销毁视图时，childFragmentManager 也会跟着销毁其中的子 Fragment 视图。
 */
class FragmentNestedDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_nested)
        setupDemoToolbar(R.string.frag_nested, R.id.fragment_nested_root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_parent, ParentFragment())
                .commit()
        }
    }

    /** 父 Fragment：管理三个可切换的子 Fragment（用 childFragmentManager）。 */
    class ParentFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_nested_parent, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val tabA = view.findViewById<TextView>(R.id.tab_nested_a)
            val tabB = view.findViewById<TextView>(R.id.tab_nested_b)
            val tabC = view.findViewById<TextView>(R.id.tab_nested_c)

            if (childFragmentManager.findFragmentById(R.id.container_child) == null) {
                switchChild(getString(R.string.frag_nested_tab_a))
            }

            tabA.setOnClickListener { switchChild(tabA.text.toString()) }
            tabB.setOnClickListener { switchChild(tabB.text.toString()) }
            tabC.setOnClickListener { switchChild(tabC.text.toString()) }
        }

        private fun switchChild(name: String) {
            // 关键：childFragmentManager 而不是 parentFragmentManager
            childFragmentManager.beginTransaction()
                .replace(R.id.container_child, ChildFragment.newInstance(name))
                .commit()
        }
    }

    /** 子 Fragment：只展示自己名字，标注是通过 parentFragment 引用父 Fragment。 */
    class ChildFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View = inflater.inflate(R.layout.fragment_nested_child, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val name = arguments?.getString(ARG_NAME).orEmpty()
            val text = view.findViewById<TextView>(R.id.tv_nested_child)
            val parentName = parentFragment?.javaClass?.simpleName ?: "-"
            text.text = getString(R.string.frag_nested_child, name) + "\n\n父 Fragment: $parentName"
        }

        companion object {
            private const val ARG_NAME = "name"
            fun newInstance(name: String) = ChildFragment().apply {
                arguments = Bundle().apply { putString(ARG_NAME, name) }
            }
        }
    }
}
