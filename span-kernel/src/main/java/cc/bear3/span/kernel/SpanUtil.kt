package cc.bear3.span.kernel

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat

class SpanUtil {
    companion object {
        fun contact(vararg charSequence: CharSequence): CharSequence {
            val ss = SpannableStringBuilder()
            charSequence.forEach {
                ss.append(it)
            }
            return ss
        }
    }
}

fun TextView.withCharSequence(vararg charSequence: CharSequence) {
    text = SpanUtil.contact(*charSequence)
}

fun CharSequence?.toSpannable(): Spannable {
    return (this as? Spannable) ?: SpannableString(this ?: "")
}

fun Spannable.withSpan(span: CharacterStyle, start: Int = 0, end: Int = length): CharSequence {
    if (start in 0 until end && end <= length) {
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return this
}

fun CharSequence?.toBold(): CharSequence {
    return toSpannable().withSpan(StyleSpan(Typeface.BOLD))
}

fun CharSequence?.toItalic(): CharSequence {
    return toSpannable().withSpan(StyleSpan(Typeface.ITALIC))
}

fun CharSequence?.toBoldItalic(): CharSequence {
    return toSpannable().withSpan(StyleSpan(Typeface.BOLD_ITALIC))
}

fun CharSequence?.toUnderline(): CharSequence {
    return toSpannable().withSpan(UnderlineSpan())
}

fun CharSequence?.toStrikethrough(): CharSequence {
    return toSpannable().withSpan(StrikethroughSpan())
}

fun CharSequence?.toForeground(colorInt: Int): CharSequence {
    return toSpannable().withSpan(ForegroundColorSpan(colorInt))
}

fun CharSequence?.toForeground(context: Context?, @ColorRes colorRes: Int): CharSequence {
    if (context == null) {
        return toSpannable()
    }
    return toSpannable().withSpan(ForegroundColorSpan(ContextCompat.getColor(context, colorRes)))
}

fun CharSequence?.toBackground(color: Int): CharSequence {
    return toSpannable().withSpan(BackgroundColorSpan(color))
}

fun CharSequence?.toBackground(context: Context?, @ColorRes colorRes: Int): CharSequence {
    if (context == null) {
        return toSpannable()
    }
    return toSpannable().withSpan(BackgroundColorSpan(ContextCompat.getColor(context, colorRes)))
}

fun CharSequence?.toFontSize(size: Int, dp: Boolean = true): CharSequence {
    return toSpannable().withSpan(AbsoluteSizeSpan(size, dp))
}

fun CharSequence?.toFontSize(context: Context?, @DimenRes dimenRes: Int): CharSequence {
    if (context == null) {
        return toSpannable()
    }
    return toSpannable().withSpan(
        AbsoluteSizeSpan(
            context.resources.getDimensionPixelSize(dimenRes),
            false
        )
    )
}

/**
 * Set the span of font family.
 *
 * @param fontFamily The font family.
 *                   <ul>
 *                   <li>monospace</li>
 *                   <li>serif</li>
 *                   <li>sans-serif</li>
 *                   </ul>
 * @return the single {SpanUtils} instance
 */
fun CharSequence?.toFontFamily(fontFamily: String): CharSequence {
    return toSpannable().withSpan(TypefaceSpan(fontFamily))
}

fun CharSequence?.toTypeface(typeface: Typeface): CharSequence {
    return toSpannable().withSpan(CustomTypefaceSpan(typeface))
}

fun CharSequence?.toSuperscript(fontSize: Int = 10, dp: Boolean = true): CharSequence {
    return toSpannable().withSpan(SuperscriptSpan()).toFontSize(fontSize, dp)
}

fun CharSequence?.toSuperscript(context: Context?, @DimenRes dimenRes: Int): CharSequence {
    return toSpannable().withSpan(SuperscriptSpan()).toFontSize(context, dimenRes)
}

fun CharSequence?.toSubscript(fontSize: Int = 10, dp: Boolean = true): CharSequence {
    return toSpannable().withSpan(SubscriptSpan()).toFontSize(fontSize, dp)
}

fun CharSequence?.toSubscript(context: Context?, @DimenRes dimenRes: Int): CharSequence {
    return toSpannable().withSpan(SubscriptSpan()).toFontSize(context, dimenRes)
}

fun CharSequence?.toScale(scale: Float): CharSequence {
    return toSpannable().withSpan(RelativeSizeSpan(scale))
}

fun CharSequence?.toScaleX(scaleX: Float): CharSequence {
    return toSpannable().withSpan(ScaleXSpan(scaleX))
}

fun CharSequence?.toDrawable(drawable: Drawable): CharSequence {
    return toSpannable().withSpan(CenterImageSpan(drawable))
}

fun CharSequence?.toDrawable(context: Context?, @DrawableRes drawableResId: Int): CharSequence {
    if (context == null) {
        return ""
    }
    val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return ""
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    return toDrawable(drawable)
}

fun createDrawableCharSequence(drawable: Drawable): CharSequence {
    return "[image]".toDrawable(drawable)
}

fun createDrawableCharSequence(context: Context?, @DrawableRes drawableResId: Int): CharSequence {
    return "[image]".toDrawable(context, drawableResId)
}

fun CharSequence?.toMargin(margin: Int, color: Int = Color.TRANSPARENT): CharSequence {
    return toSpannable().withSpan(MarginSpan(margin, color))
}

fun createMarginCharSequence(margin: Int, color: Int = Color.TRANSPARENT): CharSequence {
    return "< >".toMargin(margin, color)
}

fun Int.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float = 0.3f): Int {
    val alphaInt = (Color.alpha(this) * alpha).toInt()
    return this and 0x00FFFFFF or (alphaInt shl 24)
}

fun CharSequence?.toClickWithEffect(
    textColor: Int = CustomClickableSpan.defaultTextColor,
    @FloatRange(from = 0.0, to = 1.0) bgAlpha: Float = 0.3f,
    underLine: Boolean = false,
    block: (view: View) -> Unit
): CharSequence {
    return toSpannable().withSpan(
        CustomClickableSpan(
            textColor,
            underLine,
            textColor.alpha(bgAlpha),
            block
        )
    )
}

fun CharSequence?.toClick(
    textColor: Int = CustomClickableSpan.defaultTextColor,
    underLine: Boolean = false,
    bgColor: Int = Color.TRANSPARENT,
    block: (view: View) -> Unit
): CharSequence {
    return toSpannable().withSpan(CustomClickableSpan(textColor, underLine, bgColor, block))
}

fun CharSequence?.toUrl(url: String): CharSequence {
    return toSpannable().withSpan(URLSpan(url))
}