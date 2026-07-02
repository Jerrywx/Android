package com.example.appdemo.demo.customview.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.appdemo.R

/**
 * 自定义 EditText。
 *
 * · showClearIcon=true 时，有内容右侧显示清除按钮，点击命中区域清空
 * · togglePassword=true 时，密码模式额外显示眼睛图标，点击切换明文 / 密文
 * · 通过 setCompoundDrawablesRelativeWithIntrinsicBounds 挂图标
 * · onTouchEvent 判断触点是否命中右侧图标 —— setCompoundDrawablePadding 之外的宽度
 */
class ClearableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var showClearIcon: Boolean = true
    private var togglePassword: Boolean = false

    private val clearIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_close)?.mutate()
    private val eyeOpenIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_eye_open)?.mutate()
    private val eyeOffIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_eye_off)?.mutate()

    private var passwordVisible: Boolean = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText).apply {
            showClearIcon = getBoolean(R.styleable.ClearableEditText_showClearIcon, true)
            togglePassword = getBoolean(R.styleable.ClearableEditText_togglePassword, false)
            recycle()
        }
        if (togglePassword) {
            /// 显式设为密码，避免调用方漏写 inputType
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = refreshIcons()
        })
        setOnFocusChangeListener { _, _ -> refreshIcons() }
        refreshIcons()
    }

    private fun refreshIcons() {
        val right = when {
            togglePassword -> if (passwordVisible) eyeOpenIcon else eyeOffIcon
            showClearIcon && isFocused && !text.isNullOrEmpty() -> clearIcon
            else -> null
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, right, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawable = compoundDrawablesRelative[2] ?: return super.onTouchEvent(event)
            val iconWidth = drawable.bounds.width()
            /// 命中区域：右侧 iconWidth + padding 内
            val hitStart = width - paddingEnd - iconWidth
            if (event.x >= hitStart) {
                when {
                    togglePassword -> togglePasswordVisible()
                    showClearIcon -> setText("")
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun togglePasswordVisible() {
        passwordVisible = !passwordVisible
        val selection = selectionEnd
        inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        /// inputType 变化会重置字体和光标，需要重新定位
        setSelection(selection)
        refreshIcons()
    }
}
