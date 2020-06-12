package cc.bear3.weight.textview.collapsed;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * @author yiw
 * @Description:
 * @date 16/1/2 16:32
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

    public int getBgColor() {
        return bgColor;
    }

    private static int getDefaultBgColorByTextColor(int textColor) {
        int alpha = (int) (Color.alpha(textColor) * 0.3f);
        return (textColor & 0x00FFFFFF) | (alpha << 24);
    }
}
