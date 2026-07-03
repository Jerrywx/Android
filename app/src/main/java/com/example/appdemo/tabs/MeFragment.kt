package com.example.appdemo.tabs

import com.example.appdemo.R

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MeFragment : Fragment(R.layout.fragment_me) {

    private val items: List<MeItem> = listOf(
        MeItem.Header,
        // 组 1：服务
        MeItem.Row(R.drawable.ic_me_service, R.color.me_icon_service,
            R.string.me_service, isGroupStart = true, isGroupEnd = true),
        // 组 2：收藏 / 朋友圈 / 作品 / 小店与卡包 / 表情
        MeItem.Row(R.drawable.ic_me_fav, R.color.me_icon_fav_a,
            R.string.me_favorite, isGroupStart = true),
        MeItem.Row(R.drawable.ic_me_moments, R.color.me_icon_moments_3,
            R.string.me_moments),
        MeItem.Row(R.drawable.ic_me_works, R.color.me_icon_works,
            R.string.me_works, hintRes = R.string.me_works_sub, showDot = true),
        MeItem.Row(R.drawable.ic_me_shop, R.color.me_icon_shop,
            R.string.me_shop, hintRes = R.string.me_shop_sub, showDot = true),
        MeItem.Row(R.drawable.ic_me_emoji, R.color.me_icon_emoji,
            R.string.me_emoji, isGroupEnd = true),
        // 组 3：设置
        MeItem.Row(R.drawable.ic_me_setting, R.color.me_icon_setting,
            R.string.me_setting, isGroupStart = true, isGroupEnd = true),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.me_list)
        list.layoutManager = LinearLayoutManager(view.context)
        list.adapter = MeAdapter(items) { toast(it) }
        list.addItemDecoration(GroupSpacingDecoration(items, dp(view, 10f)))
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun dp(v: View, value: Float): Int =
        (value * v.resources.displayMetrics.density + 0.5f).toInt()

    sealed class MeItem {
        object Header : MeItem()
        data class Row(
            @DrawableRes val icon: Int,
            @ColorRes val iconTint: Int,
            @StringRes val title: Int,
            @StringRes val hintRes: Int? = null,
            val showDot: Boolean = false,
            val isGroupStart: Boolean = false,
            val isGroupEnd: Boolean = false,
        ) : MeItem()
    }

    private class MeAdapter(
        private val items: List<MeItem>,
        private val onClick: (String) -> Unit,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            MeItem.Header -> TYPE_HEADER
            is MeItem.Row -> TYPE_ROW
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_HEADER) {
                HeaderHolder(inflater.inflate(R.layout.item_me_header, parent, false), onClick)
            } else {
                RowHolder(inflater.inflate(R.layout.item_me_row, parent, false), onClick)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                MeItem.Header -> (holder as HeaderHolder).bind()
                is MeItem.Row -> (holder as RowHolder).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        companion object {
            const val TYPE_HEADER = 0
            const val TYPE_ROW = 1
        }
    }

    private class HeaderHolder(itemView: View, onClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.findViewById<View>(R.id.me_qr).setOnClickListener { onClick("我的二维码") }
            itemView.findViewById<View>(R.id.me_status_chip).setOnClickListener { onClick("状态") }
        }

        fun bind() = Unit
    }

    private class RowHolder(itemView: View, private val onClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val container: View = itemView.findViewById(R.id.me_row_container)
        private val icon: ImageView = itemView.findViewById(R.id.me_row_icon)
        private val title: TextView = itemView.findViewById(R.id.me_row_title)
        private val hint: TextView = itemView.findViewById(R.id.me_row_hint)
        private val dot: View = itemView.findViewById(R.id.me_row_dot)
        private val divider: View = itemView.findViewById(R.id.me_row_divider)

        fun bind(item: MeItem.Row) {
            val ctx = itemView.context
            icon.setImageResource(item.icon)
            icon.imageTintList = ContextCompat.getColorStateList(ctx, item.iconTint)
            title.setText(item.title)

            if (item.hintRes != null) {
                hint.setText(item.hintRes)
                hint.isVisible = true
            } else {
                hint.isVisible = false
            }
            dot.isVisible = item.showDot
            divider.isVisible = !item.isGroupStart

            val bg = when {
                item.isGroupStart && item.isGroupEnd -> R.drawable.bg_me_group
                item.isGroupStart -> R.drawable.bg_me_group_top
                item.isGroupEnd -> R.drawable.bg_me_group_bottom
                else -> R.drawable.bg_me_group_middle
            }
            container.setBackgroundResource(bg)

            val text = ctx.getString(item.title)
            itemView.setOnClickListener { onClick(text) }
        }
    }

    private class GroupSpacingDecoration(
        private val items: List<MeItem>,
        private val gap: Int,
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            val item = items.getOrNull(position) as? MeItem.Row ?: return
            if (item.isGroupStart) outRect.top = gap
        }
    }
}
