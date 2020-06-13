package cc.bear3.view.collapsedtextview;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * 自定义ClickableSpan 去掉下划线，增加背景色预设值
 * @author tt
 * @since  2020-06-13
 */
public abstract class ColorClickableSpan extends ClickableSpan implements View.OnClickListener {

    public static int DEFAULT_COLOR = 0xff557EBC;

    /**
     * text颜色
     */
    private int textColor;
    private int bgColor;

    public ColorClickableSpan() {
        this(DEFAULT_COLOR);
    }

    public ColorClickableSpan(int textColor) {
        this(textColor, getDefaultBgColorByTextColor(textColor));
    }

    public ColorClickableSpan(int textColor, int bgColor) {
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);

        ds.setColor(textColor);
        ds.setUnderlineText(false);
        ds.clearShadowLayer();
    }

    public void setTextColor(int textColor, int bgColor) {
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    static int getDefaultBgColorByTextColor(int textColor) {
        int alpha = (int) (Color.alpha(textColor) * 0.3f);
        return (textColor & 0x00FFFFFF) | (alpha << 24);
    }
}
