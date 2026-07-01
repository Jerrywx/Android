package com.example.appdemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragment 总入口 —— 按主题列出全部 Fragment Demo。
 *
 * 涵盖内容（10 个）：
 *   1. 生命周期：Fragment 完整生命周期回调可视化
 *   2. 事务操作：add / replace / show / hide / attach / detach / 回退栈
 *   3. 参数传递：Bundle + newInstance 模式
 *   4. Result API：Fragment 之间通过 FragmentResult 通信
 *   5. 共享 ViewModel：宿主 Activity 作为作用域，兄弟 Fragment 共享数据
 *   6. 嵌套 Fragment：childFragmentManager 使用
 *   7. DialogFragment：自定义弹窗
 *   8. BottomSheetDialogFragment：底部抽屉
 *   9. 状态保存：旋转 / 进程重建后的数据恢复
 *   10. FragmentContainerView：手动 Tab 切换 + 状态保留
 */
class AndroidFragmentActivity : AppCompatActivity() {

    private data class NavItem(val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_fragment)
        setupDemoToolbar(R.string.fragment_title, R.id.android_fragment_root)

        val entries = listOf(
            NavItem(getString(R.string.frag_lifecycle), getString(R.string.frag_lifecycle_hint), FragmentLifecycleDemoActivity::class.java),
            NavItem(getString(R.string.frag_transaction), getString(R.string.frag_transaction_hint), FragmentTransactionDemoActivity::class.java),
            NavItem(getString(R.string.frag_arguments), getString(R.string.frag_arguments_hint), FragmentArgsDemoActivity::class.java),
            NavItem(getString(R.string.frag_result), getString(R.string.frag_result_hint), FragmentResultDemoActivity::class.java),
            NavItem(getString(R.string.frag_shared_vm), getString(R.string.frag_shared_vm_hint), FragmentSharedVmDemoActivity::class.java),
            NavItem(getString(R.string.frag_nested), getString(R.string.frag_nested_hint), FragmentNestedDemoActivity::class.java),
            NavItem(getString(R.string.frag_dialog), getString(R.string.frag_dialog_hint), FragmentDialogDemoActivity::class.java),
            NavItem(getString(R.string.frag_bottom_sheet), getString(R.string.frag_bottom_sheet_hint), FragmentBottomSheetDemoActivity::class.java),
            NavItem(getString(R.string.frag_state_save), getString(R.string.frag_state_save_hint), FragmentSaveStateDemoActivity::class.java),
            NavItem(getString(R.string.frag_container), getString(R.string.frag_container_hint), FragmentContainerDemoActivity::class.java),
        )

        val list = findViewById<RecyclerView>(R.id.android_fragment_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = EntryAdapter(entries) { entry -> startActivity(Intent(this, entry.target)) }
    }

    private class EntryAdapter(
        private val items: List<NavItem>,
        private val onClick: (NavItem) -> Unit,
    ) : RecyclerView.Adapter<EntryAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_study_entry, parent, false)
            return VH(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position], position + 1, onClick)

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val card: View = view.findViewById(R.id.study_card)
            private val index: TextView = view.findViewById(R.id.study_index)
            private val title: TextView = view.findViewById(R.id.study_title)
            private val subtitle: TextView = view.findViewById(R.id.study_subtitle)
            fun bind(item: NavItem, no: Int, onClick: (NavItem) -> Unit) {
                index.text = no.toString()
                title.text = item.title
                subtitle.text = item.subtitle
                card.setOnClickListener { onClick(item) }
            }
        }
    }
}
