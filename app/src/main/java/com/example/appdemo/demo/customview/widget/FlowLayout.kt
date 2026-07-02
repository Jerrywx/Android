package com.example.appdemo.demo.customview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 流式标签布局。
 *
 * 特点：
 *   · 子 View 水平摆放，遇边界自动换行
 *   · 支持横向 / 纵向间距
 *   · 兼容父容器 wrap_content（自身高度按内容计算）
 *
 * 关键点：
 *   · onMeasure：先量子 View，再按行累加宽高得到自身尺寸
 *   · onLayout：复用 onMeasure 中划分好的每一行，逐个摆放
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {

    var horizontalSpacing: Int = (8 * resources.displayMetrics.density).toInt()
    var verticalSpacing: Int = (8 * resources.displayMetrics.density).toInt()

    /// 记录每行的子 View 与该行高度，避免 onLayout 里重复分行
    private val lines = mutableListOf<Line>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val maxWidth = widthSize - paddingLeft - paddingRight

        lines.clear()
        var currentLine = Line()
        var totalHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val cw = child.measuredWidth
            val ch = child.measuredHeight

            /// 当前行放不下，先结束这一行
            if (currentLine.children.isNotEmpty() && currentLine.width + horizontalSpacing + cw > maxWidth) {
                lines.add(currentLine)
                totalHeight += currentLine.height + verticalSpacing
                currentLine = Line()
            }

            if (currentLine.children.isEmpty()) {
                currentLine.width = cw
            } else {
                currentLine.width += horizontalSpacing + cw
            }
            currentLine.height = maxOf(currentLine.height, ch)
            currentLine.children.add(child)
        }
        if (currentLine.children.isNotEmpty()) {
            lines.add(currentLine)
            totalHeight += currentLine.height
        }

        val measuredW = if (widthMode == MeasureSpec.EXACTLY) widthSize
        else lines.maxOfOrNull { it.width }?.plus(paddingLeft + paddingRight) ?: (paddingLeft + paddingRight)

        val measuredH = if (heightMode == MeasureSpec.EXACTLY) heightSize
        else totalHeight + paddingTop + paddingBottom

        setMeasuredDimension(measuredW, measuredH)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = paddingTop
        for (line in lines) {
            var left = paddingLeft
            for (child in line.children) {
                val cw = child.measuredWidth
                val ch = child.measuredHeight
                /// 每个 child 在行内垂直居中
                val childTop = top + (line.height - ch) / 2
                child.layout(left, childTop, left + cw, childTop + ch)
                left += cw + horizontalSpacing
            }
            top += line.height + verticalSpacing
        }
    }

    private class Line {
        val children = mutableListOf<View>()
        var width = 0
        var height = 0
    }
}
