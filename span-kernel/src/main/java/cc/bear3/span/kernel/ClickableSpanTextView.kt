package cc.bear3.span.kernel

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

    // 点击Clickable后添加背景Span
    private var effectSpan: BackgroundColorSpan? = null

    override fun setText(text: CharSequence?, type: BufferType) {
        super.setText(text, BufferType.SPANNABLE)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (text !is Spannable) {
            return super.onTouchEvent(event)
        }

        val buffer = text as Spannable

        val x = event.x.toInt() - totalPaddingLeft + scrollX
        val y = event.y.toInt() - totalPaddingTop + scrollY

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                addEffectSpan(buffer, x, y)
            }
            MotionEvent.ACTION_UP -> {
                removeEffectSpan(buffer)
                getTouchedSpan(buffer, x, y)?.let { span ->
                    span.onClick(this)
                    return true
                }
            }
            else -> {
                removeEffectSpan(buffer)
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * 添加反馈效果
     */
    private fun addEffectSpan(buffer: Spannable, x: Int, y: Int) {
        val touchedSpan = getTouchedSpan(buffer, x, y)
        if (touchedSpan == null) {
            removeEffectSpan(buffer)
            return
        }

        val spanStart = buffer.getSpanStart(touchedSpan)
        val spanEnd = buffer.getSpanEnd(touchedSpan)
        Selection.setSelection(buffer, spanStart, spanEnd)

        if (effectSpan == null) {
            (touchedSpan as? CustomClickableSpan)?.let {
                if (it.bgColor != Color.TRANSPARENT) {
                    effectSpan = BackgroundColorSpan(it.bgColor).apply {
                        buffer.withSpan(this, spanStart, spanEnd)
                    }
                }
            }
            return
        }

        if (spanStart != buffer.getSpanStart(effectSpan) || spanEnd != buffer.getSpanEnd(effectSpan)) {
            // 不是同一个span
            removeEffectSpan(buffer)
            (touchedSpan as? CustomClickableSpan)?.let {
                if (it.bgColor != Color.TRANSPARENT) {
                    effectSpan = BackgroundColorSpan(it.bgColor).apply {
                        buffer.withSpan(this, spanStart, spanEnd)
                    }
                }
            }
        }
    }

    private fun removeEffectSpan(buffer: Spannable) {
        Selection.removeSelection(buffer)
        effectSpan?.let {
            //移除点击时设置的背景span
            buffer.removeSpan(it)
            effectSpan = null
        }
    }

    private fun getTouchedSpan(buffer: Spannable, x: Int, y: Int): ClickableSpan? {
        val layout = layout
        val line = layout.getLineForVertical(y)
        val position = layout.getOffsetForHorizontal(line, x.toFloat())
        val spans = buffer.getSpans(position, position, ClickableSpan::class.java)
        if (spans.isEmpty()) {
            return null
        }
        val touchedSpan = spans[0]
        val spanStart = buffer.getSpanStart(touchedSpan)
        val spanEnd = buffer.getSpanEnd(touchedSpan)
        if (position < spanStart || position > spanEnd) {
            return null
        }
        if (position == spanEnd) {
            // 特殊情况，当前可点击的span处于行尾，判定不准确，需要再次判定x坐标
            if (x > layout.getSecondaryHorizontal(position)) {
                return null
            }
        }
        return touchedSpan
    }

}