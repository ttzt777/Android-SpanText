package cc.bear3.span.kernel

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

/**
 * 自定义ClickableSpan 去掉下划线，增加背景色预设值
 * @author tt
 * @since  2020-06-13
 */
class CustomClickableSpan @JvmOverloads constructor(
    var textColor: Int = defaultTextColor,
    var underLine: Boolean = false,
    var bgColor: Int = Color.TRANSPARENT,
    val block: (view: View) -> Unit
) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = textColor
        ds.isUnderlineText = underLine
        ds.clearShadowLayer()
    }

    override fun onClick(view: View) {
        block(view)
    }

    companion object {
        var defaultTextColor = 0xFF557EBC.toInt()
    }
}