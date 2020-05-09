package cc.bear3.weight.textview.collapsed;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * @author yiw
 * @Description:
 * @date 16/1/2 16:32
 */
public abstract class SpannableClickable extends ClickableSpan implements View.OnClickListener {

    public static int DEFAULT_COLOR = 0xff557EBC;

    /**
     * text颜色
     */
    private int textColor;

    public SpannableClickable() {
        this.textColor = DEFAULT_COLOR;
    }

    public SpannableClickable(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);

        ds.setColor(textColor);
        ds.setUnderlineText(false);
        ds.clearShadowLayer();

    }
}
