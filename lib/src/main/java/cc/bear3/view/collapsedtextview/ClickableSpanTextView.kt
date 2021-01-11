package cc.bear3.view.collapsedtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Selection
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import cc.bear3.view.collapsedtextview.collapsed.R

/**
 * 替代LinkMomentMethod，实现TextView有ClickableSpan的时候实现点击事件
 *
 * // todo 点击在ClickableSpan上面没有办法响应TextView的长按事件
 *
 * @author TT
 * @since 2019/06/12
 */
open class ClickableSpanTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // 点击部分文字时部分文字的背景色
    protected var clickableSpanBgColor: Int? = null

    // 点击Clickable后添加背景Span
    private var mBgSpan: BackgroundColorSpan? = null

    // 获取的ClickableSpan
    private var mClickLinks: Array<ClickableSpan> = emptyArray()

    init {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.ClickableSpanTextView)

            clickableSpanBgColor = if (array.hasValue(R.styleable.ClickableSpanTextView_clickable_span_background)) {
                array.getColor(
                        R.styleable.ClickableSpanTextView_clickable_span_background,
                        Color.TRANSPARENT)
            } else {
                defaultClickableSpanBgColor
            }

            array.recycle()
        } else {
            clickableSpanBgColor = defaultClickableSpanBgColor
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (text !is Spannable) {
            return super.onTouchEvent(event)
        }

        val buffer = text as Spannable

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 按下事件
                var x = event.x
                var y = event.y

                x -= totalPaddingLeft
                y -= totalPaddingTop

                x += scrollX
                y += scrollY

                val line = layout.getLineForVertical(y.toInt())
                val off = layout.getOffsetForHorizontal(line, x)

                // 点击文字后面的空白会导致 off = 文字长度，此时span在文字最后会导致getSpans()判断不准确
                // 原写法 mClickLinks = buffer.getSpans(off, off, ClickableSpan.class)
                mClickLinks = if (off < buffer.length) {
                    buffer.getSpans(off, off, ClickableSpan::class.java)
                } else {
                    emptyArray()
                }

                if (mClickLinks.isNotEmpty()) {
                    val clickableSpan = mClickLinks[0]
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(clickableSpan),
                            buffer.getSpanEnd(clickableSpan))

                    // 获取点击区域将要设置的背景颜色
                    var bgColor : Int? = null
                    // 如果是ColorClickableSpan，先取里面设置的背景值
                    if (clickableSpan is ColorClickableSpan) {
                        bgColor = clickableSpan.bgColor
                    }
                    // 没有值取变量值
                    if (bgColor == null) {
                        bgColor = clickableSpanBgColor
                    }

                    //设置点击区域的背景色
                    bgColor?.let {
                        mBgSpan = BackgroundColorSpan(it)
                        buffer.setSpan(mBgSpan,
                                buffer.getSpanStart(clickableSpan),
                                buffer.getSpanEnd(clickableSpan),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                Selection.removeSelection(buffer)
                mBgSpan?.let {
                    //移除点击时设置的背景span
                    buffer.removeSpan(it)
                    mBgSpan = null
                }

                if (mClickLinks.isNotEmpty()) {
                    mClickLinks[0].onClick(this)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {

            }
            else -> {
                mBgSpan?.let {
                    //移除点击时设置的背景span
                    buffer.removeSpan(it)
                    mBgSpan = null
                }
            }
        }

        return super.onTouchEvent(event)
    }

    companion object {
        var defaultClickableSpanBgColor: Int? = null
    }
}