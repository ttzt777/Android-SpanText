package cc.bear3.span.kernel

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(private val newType: Typeface) : TypefaceSpan("") {
    override fun updateDrawState(textPaint: TextPaint) {
        apply(textPaint, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        apply(paint, newType)
    }

    private fun apply(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = tf
    }
}