package cc.bear3.view.collapsedtextview

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

/**
 * 自定义ClickableSpan 去掉下划线，增加背景色预设值
 * @author tt
 * @since  2020-06-13
 */
abstract class ColorClickableSpan @JvmOverloads constructor(
        var textColor: Int = defaultTextColor,
        var bgColor: Int? = null
) : ClickableSpan(), View.OnClickListener {

    init {
        checkBgColor()
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = textColor
        ds.isUnderlineText = false
        ds.clearShadowLayer()
    }

    fun setTextColor(textColor: Int, bgColor: Int? = null) {
        this.textColor = textColor
        this.bgColor = bgColor

        checkBgColor()
    }

    private fun checkBgColor() {
        if (bgColor == null) {
            defaultBgColorAlphaByTextColor?.let {
                var alpha = it
                if (alpha > 1f) {
                    alpha = 1f
                } else if (alpha < 0f) {
                    alpha = 0f
                }

                bgColor = getBgColorByTextColor(textColor, alpha)
            }
        }
    }

    companion object {
        var defaultTextColor = 0xFF557EBC.toInt()
        var defaultBgColorAlphaByTextColor : Float? = 0.3f

        fun getBgColorByTextColor(textColor: Int, alpha: Float): Int {
            val alphaInt = (Color.alpha(textColor) * alpha).toInt()
            return textColor and 0x00FFFFFF or (alphaInt shl 24)
        }
    }
}