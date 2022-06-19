package cc.bear3.span.kernel

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.ReplacementSpan

class MarginSpan(private val width: Int, private val color: Int = Color.TRANSPARENT) :
    ReplacementSpan() {
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@MarginSpan.color
            style = Paint.Style.FILL
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return width
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        canvas.drawRect(x, top.toFloat(), x + width, bottom.toFloat(), this.paint)
    }
}