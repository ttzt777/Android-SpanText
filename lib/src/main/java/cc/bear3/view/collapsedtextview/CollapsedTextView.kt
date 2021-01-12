package cc.bear3.view.collapsedtextview

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.ColorInt
import cc.bear3.view.collapsedtextview.collapsed.R

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CollapsedTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ClickableSpanTextView(context, attrs, defStyleAttr) {

    // 是否允许折叠 true -- 折叠功能 false -- 普通TextView
    private var collapseEnable = DEFAULT_COLLAPSE_ABLE

    // 是否允许展开 true -- 展开、收起 false -- 无任何点击事件，只会显示"展开全文"
    private var expandEnable = DEFAULT_EXPAND_ABLE

    // 折叠情况下显示最大行数
    private var limitLines = DEFAULT_COLLAPSED_LIMIT

    // 折叠后显示的行数
    private var collapsedLines = DEFAULT_COLLAPSED_LINES

    // 展开的文本（展开全文）
    private var endExpandText: String? = null

    // 折叠的文本（收起）
    private var endCollapseText: String? = null

    // "展开、收起"的Span的颜色
    @ColorInt
    private var textLinkColor = 0

    // "展开、收起"的Span点击后的背景颜色
    @ColorInt
    private var textLinkBgColor : Int? = null

    // 原始的文本
    private var originalText: CharSequence = ""

    // 折叠后的文本
    private var collapsedText: CharSequence? = null

    // 记录行数
    private var textLineCount = 0

    // 展开状态
    private var isExpanded = false

    // TextView中文字可显示的宽度
    private var showWidth = 0

    // 展开收起的文字Span
    private var endTextSpan: CharacterStyle? = null

    // buffer type 固定为Spannable
    private val bufferType = BufferType.SPANNABLE
    private var listener: CollapsedTextViewCallback? = null

    init {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.CollapsedTextView)
            collapseEnable = array.getBoolean(R.styleable.CollapsedTextView_ctv_collapse_able, collapseEnable)
            expandEnable = array.getBoolean(R.styleable.CollapsedTextView_ctv_expand_able, expandEnable)
            setLimitLines(
                    array.getInt(R.styleable.CollapsedTextView_ctv_limited_lines, DEFAULT_COLLAPSED_LIMIT),
                    array.getInt(R.styleable.CollapsedTextView_ctv_collapsed_lines, DEFAULT_COLLAPSED_LINES),
                    false)
            setEndExpandText(array.getString(R.styleable.CollapsedTextView_ctv_expand_text), false)
            setEndCollapseText(array.getString(R.styleable.CollapsedTextView_ctv_collapse_text), false)
            textLinkColor = array.getColor(R.styleable.CollapsedTextView_ctv_text_link_color, ColorClickableSpan.defaultTextColor)
            textLinkBgColor = if (array.hasValue(R.styleable.CollapsedTextView_ctv_text_link_bg_color)) {
                val defaultTextLinkBgColor = if (ColorClickableSpan.defaultBgColorAlphaByTextColor != null) {
                    ColorClickableSpan.getBgColorByTextColor(textLinkColor, ColorClickableSpan.defaultBgColorAlphaByTextColor!!)
                } else {
                    defaultClickableSpanBgColor ?: Color.TRANSPARENT
                }
                array.getColor(R.styleable.CollapsedTextView_ctv_text_link_bg_color, defaultTextLinkBgColor)
            } else {
                null
            }
            array.recycle()
        } else {
            setEndExpandText(null, false)
            setEndCollapseText(null, false)
            textLinkColor = ColorClickableSpan.defaultTextColor
            textLinkBgColor = null
        }

        // 重置一遍，因为TextView获取到Xml中设置的text在自定义View属性获取之前
        if (!TextUtils.isEmpty(text)) {
            text = text
        }
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        if (!collapseEnable) {
            super.setText(text, type)
            return
        }

        updateContent(text)

        // 如果text为空则直接显示
        if (originalText.isEmpty()) {
            super.setText(originalText, bufferType)
        } else if (isExpanded) {
            // 保存原始文本，去掉文本末尾的空字符
            formatExpandedText()
        } else {
            // 获取TextView中文字显示的宽度，需要在layout之后才能获取到，避免重复获取
            if (showWidth == 0) {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        showWidth = width - paddingLeft - paddingRight
                        formatCollapsedText()
                    }
                })
            } else {
                formatCollapsedText()
            }
        }
    }

    /**
     * 设置是否允许折叠
     *
     * @param collapseEnable true -- 具有折叠功能， false -- 普通TextView
     */
    fun setCollapseEnable(collapseEnable: Boolean) {
        this.collapseEnable = collapseEnable
        setText(originalText, bufferType)
    }

    /**
     * 设置是否可以展开
     * 点击"全文"是展开还是跳转（TextView或parent的OnClick）
     *
     * @param expandEnable true -- 当前展开 false -- 不做任何处理
     */
    fun setExpandEnable(expandEnable: Boolean) {
        this.expandEnable = expandEnable
        setText(originalText, bufferType)
    }

    /**
     * 设置展开的提示文本
     *
     * @param expandText 提示文本
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    @JvmOverloads
    fun setEndExpandText(expandText: String?, refresh: Boolean = true) {
        endExpandText = if (TextUtils.isEmpty(expandText)) context.getString(R.string.expand_text) else expandText
        if (refresh) {
            resetParams()
            setText(originalText, bufferType)
        }
    }

    /**
     * 设置折叠的提示文本
     *
     * @param collapseText 提示文本
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    @JvmOverloads
    fun setEndCollapseText(collapseText: String?, refresh: Boolean = true) {
        endCollapseText = if (TextUtils.isEmpty(collapseText)) context.getString(R.string.collapse_text) else collapseText
        if (refresh) {
            setText(originalText, bufferType)
        }
    }

    /**
     * 设置ClickSpan的文字颜色
     *
     * @param textLinkColor 展开、收起的字体颜色
     */
    fun setTextLinkColor(@ColorInt textLinkColor: Int) {
        this.textLinkColor = textLinkColor
        setText(originalText, bufferType)
    }

    /**
     * 设置ClickSpan的点击后的背景颜色
     *
     * @param textLinkBgColor 展开、收起的点击后的背景颜色
     */
    fun setTextLinkBgColor(@ColorInt textLinkBgColor: Int) {
        this.textLinkBgColor = textLinkBgColor
        setText(originalText, bufferType)
    }

    /**
     * 设置行数规则
     *
     * @param limitLines     折叠情况下最大显示行数，超过该行数后进行折叠
     * @param collapsedLines 折叠后显示行数
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    @JvmOverloads
    fun setLimitLines(limitLines: Int, collapsedLines: Int, refresh: Boolean = true) {
        var targetLimitLines = limitLines
        var targetCollapsedLines = collapsedLines
        if (targetLimitLines < targetCollapsedLines) {
            targetLimitLines = targetCollapsedLines
        }
        if (targetLimitLines < 1) {
            targetLimitLines = 1
        }
        if (targetCollapsedLines < 1) {
            targetCollapsedLines = 1
        }
        this.limitLines = targetLimitLines
        this.collapsedLines = targetCollapsedLines
        if (refresh) {
            resetParams()
            setText(originalText, bufferType)
        }
    }

    /**
     * 设置文字可用的显示宽度
     *
     * @param showWidth 宽度
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    @JvmOverloads
    fun setShowWidth(showWidth: Int, refresh: Boolean = true) {
        if (this.showWidth == showWidth) {
            return
        }
        this.showWidth = showWidth
        if (refresh) {
            resetParams()
            setText(originalText, bufferType)
        }
    }

    /**
     * 设置当前为折叠还是展开状态
     *
     * @param isExpanded 是否为展开状态
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    @JvmOverloads
    fun setExpanded(isExpanded: Boolean, refresh: Boolean = true) {
        if (this.isExpanded == isExpanded) {
            return
        }
        this.isExpanded = isExpanded
        if (refresh) {
            setText(originalText, bufferType)
        }
    }

    /**
     * 获取当前的展开折叠状态
     *
     * @return true -- 展开  false -- 收起
     */
    fun isExpanded(): Boolean {
        return isExpanded
    }

    /**
     * 设置点击展开、收起的点击回调
     *
     * @param listener 回调
     */
    fun setListener(listener: CollapsedTextViewCallback?) {
        this.listener = listener
    }

    private fun updateContent(charSequence: CharSequence?) {
        val targetText  = charSequence ?: ""

        if (originalText == targetText) {
            return
        }
        originalText = targetText

        // 复位
        resetParams()
    }

    /**
     * 格式化折叠时的文本
     */
    @Suppress("DEPRECATION")
    private fun formatCollapsedText() {
        if (collapsedText != null && textLineCount > 0) {
            // 有过记录，不用重新测量
            if (textLineCount <= limitLines) {
                super.setText(originalText, bufferType)
            } else {
                val spannable = SpannableStringBuilder()
                spannable.append(collapsedText)
                spannable.append(ELLIPSE)
                setSpan(spannable)
                super.setText(spannable, bufferType)
            }
            return
        }

        // 没有记录重新获取
        val layout: StaticLayout
        layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val builder = StaticLayout.Builder.obtain(
                    originalText,
                    0,
                    originalText.length,
                    paint,
                    showWidth)
            builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                    .setIncludePad(false)
            builder.build()
        } else {
            StaticLayout(originalText, paint, showWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    lineSpacingMultiplier, lineSpacingExtra, false)
        }
        textLineCount = layout.lineCount
        if (textLineCount <= limitLines) {
            // 没有超过最大行数
            collapsedText = originalText
            super.setText(originalText, bufferType)
        } else {
            // 超过限制的最大行数，进行折叠

            // 获取折叠后最后一行的文字
            val lastLineStart = layout.getLineStart(collapsedLines - 1)
            var lastLineEnd = layout.getLineVisibleEnd(collapsedLines - 1)

            // 计算后缀的宽度
            val expandedTextWidth = StaticLayout.getDesiredWidth("$ELLIPSE $endExpandText", paint)
            var lastLineWidth = StaticLayout.getDesiredWidth(originalText, lastLineStart, lastLineEnd, paint)

            // 如果大于屏幕宽度则需要减去部分字符
            while (lastLineWidth + expandedTextWidth > showWidth && lastLineStart < lastLineEnd) {
                lastLineEnd--
                lastLineWidth = StaticLayout.getDesiredWidth(originalText, lastLineStart, lastLineEnd, paint)
            }

            // 因设置的文本可能是带有样式的文本，如SpannableStringBuilder，所以根据计算的字符数从原始文本中截取
            val spannable = SpannableStringBuilder()
            // 截取文本，还是因为原始文本的样式原因不能直接使用paragraphs中的文本
            collapsedText = originalText.subSequence(0, lastLineEnd)
            spannable.append(collapsedText)
            spannable.append(ELLIPSE)
            // 设置样式
            setSpan(spannable)
            super.setText(spannable, bufferType)
        }
    }

    /**
     * 格式化展开式的文本，直接在后面拼接即可
     */
    private fun formatExpandedText() {
        val spannable = SpannableStringBuilder(originalText)
        setSpan(spannable)
        super.setText(spannable, bufferType)
    }

    /**
     * 设置提示的样式
     *
     * @param spannable 需修改样式的文本
     */
    private fun setSpan(spannable: SpannableStringBuilder) {
        // 根据提示文本需要展示的文字拼接不同的字符
        spannable.append(" ")
        // 判断是展开还是收起
        val tipsLen: Int = if (isExpanded) {
            spannable.append(endCollapseText)
            endCollapseText!!.length
        } else {
            spannable.append(endExpandText)
            endExpandText!!.length
        }
        // 设置点击事件
        spannable.setSpan(getEndTextSpan(), spannable.length - tipsLen,
                spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    private fun getEndTextSpan(): CharacterStyle {
        return if (expandEnable) {
            getEndTextClickableSpan()
        } else {
            getEndTextForegroundSpan()
        }
    }

    private fun getEndTextClickableSpan(): CharacterStyle {
        if (endTextSpan is ColorClickableSpan) {
            (endTextSpan as ColorClickableSpan).setTextColor(textLinkColor, textLinkBgColor)
        } else {
            endTextSpan = object : ColorClickableSpan(textLinkColor, textLinkBgColor) {
                override fun onClick(widget: View) {
                    isExpanded = !isExpanded
                    setText(originalText, bufferType)
                    listener?.onCollapseClick(isExpanded)
                }
            }
        }
        return endTextSpan!!
    }

    private fun getEndTextForegroundSpan(): CharacterStyle {
        if (endTextSpan is ForegroundColorSpan) {
            if ((endTextSpan as ForegroundColorSpan).foregroundColor != textLinkColor) {
                endTextSpan = ForegroundColorSpan(textLinkColor)
            }
        } else {
            endTextSpan = ForegroundColorSpan(textLinkColor)
        }
        return endTextSpan!!
    }

    private fun resetParams() {
        collapsedText = null
        textLineCount = 0
    }

    interface CollapsedTextViewCallback {
        /**
         * 点击“展开全文/收起”回调
         * @param expand 目标展开是否为展开状态
         */
        fun onCollapseClick(expand: Boolean)
    }

    companion object {
        private const val DEFAULT_COLLAPSE_ABLE = true
        private const val DEFAULT_EXPAND_ABLE = true
        private const val DEFAULT_COLLAPSED_LIMIT = 15 // 不显示展开全文的最大显示行数
        private const val DEFAULT_COLLAPSED_LINES = 10 // 折叠后行数
        private const val ELLIPSE = "..."
    }
}