package cc.bear3.weight.textview.collapsed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 替代LinkMomentMethod，实现TextView有ClickableSpan的时候实现点击事件
 *
 * // todo 点击在ClickableSpan上面没有办法响应TextView的长按事件
 *
 * @author TT
 * @since 2019/06/12
 */
@SuppressLint("AppCompatCustomView")
public class ClickableSpanTextView extends TextView {

    private final static int DEFAULT_CLICKABLE_SPAN_BACKGROUND = 0xFFCCCCCC;

    // 点击部分文字时部分文字的背景色
    protected int clickableSpanBgColor;

    // 点击Clickable后添加背景Span
    private BackgroundColorSpan mBgSpan;
    // 获取的ClickableSpan
    private ClickableSpan[] mClickLinks;

    public ClickableSpanTextView(Context context) {
        this(context, null);
    }

    public ClickableSpanTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickableSpanTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClickableSpanTextView);

            clickableSpanBgColor = array.getColor(R.styleable.ClickableSpanTextView_clickable_span_background,
                    DEFAULT_CLICKABLE_SPAN_BACKGROUND);

            array.recycle();
        } else {
            clickableSpanBgColor = DEFAULT_CLICKABLE_SPAN_BACKGROUND;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!(getText() instanceof Spannable)) {
            return super.onTouchEvent(event);
        }

        Spannable buffer = (Spannable) getText();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();

            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            // 点击文字后面的空白会导致 off = 文字长度，此时span在文字最后会导致getSpans()判断不准确
            if (off < buffer.length()) {
                mClickLinks = buffer.getSpans(off, off, ClickableSpan.class);
            } else {
                mClickLinks = new ClickableSpan[0];
            }

//            mClickLinks = buffer.getSpans(off, off, ClickableSpan.class);
            if (mClickLinks.length > 0) {
                ClickableSpan clickableSpan = mClickLinks[0];

                Selection.setSelection(buffer,
                        buffer.getSpanStart(clickableSpan),
                        buffer.getSpanEnd(clickableSpan));
                //设置点击区域的背景色
                int bgColor = clickableSpanBgColor;
                if (clickableSpan instanceof ColorClickableSpan) {
                    bgColor = ((ColorClickableSpan) clickableSpan).getBgColor();
                }
                mBgSpan = new BackgroundColorSpan(bgColor);
                buffer.setSpan(mBgSpan,
                        buffer.getSpanStart(clickableSpan),
                        buffer.getSpanEnd(clickableSpan),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                return true;
            }

        } else if (action == MotionEvent.ACTION_UP) {
            Selection.removeSelection(buffer);
            if (mBgSpan != null) {
                //移除点击时设置的背景span
                buffer.removeSpan(mBgSpan);
                mBgSpan = null;
            }

            if (mClickLinks.length > 0) {
                mClickLinks[0].onClick(this);
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            // Move不做任何处理
        } else {
            if (mBgSpan != null) {
                //移除点击时设置的背景span
                buffer.removeSpan(mBgSpan);
                mBgSpan = null;
            }
        }

        return super.onTouchEvent(event);
    }
}
