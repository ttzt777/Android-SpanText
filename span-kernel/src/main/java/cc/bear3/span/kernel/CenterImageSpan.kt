package cc.bear3.span.kernel

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

/**
 *
 * @author TT
 * @since 2021-3-1
 */
class CenterImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val b = drawable
        val fm = paint.fontMetricsInt
        val transY = (y + fm.descent + y + fm.ascent) / 2 - b.bounds.bottom / 2 //计算y方向的位移

        canvas.save()
        canvas.translate(x, transY.toFloat()) //绘制图片位移一段距离

        b.draw(canvas)
        canvas.restore()
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val d = drawable
        val rect = d.bounds
        fm?.let {
            val fmPaint = paint.fontMetricsInt

            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top

            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4

            it.ascent = -bottom
            it.top = -bottom
            it.bottom = top
            it.descent = top
        }

        return rect.right
    }
}