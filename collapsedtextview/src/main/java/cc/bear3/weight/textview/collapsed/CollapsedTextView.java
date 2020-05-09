package cc.bear3.weight.textview.collapsed;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.weseepro.wesee.api.resource.ResourceHelper;

public class CollapsedTextView extends ClickableSpanTextView {
    private static final int DEFAULT_COLLAPSED_LIMIT = 15;      // 不显示展开全文的最大显示行数
    private static final int DEFAULT_COLLAPSED_LINES = 10;      // 折叠后行数
    private static final String ELLIPSE = ResourceHelper.getString(R.string.app_ellipses); // 末尾省略号
    private static final String EXPANDED_TEXT = ResourceHelper.getString(R.string.app_expanded); // 折叠时的默认文本
    private static final String COLLAPSED_TEXT = ResourceHelper.getString(R.string.app_collapsed); //展开时的默认文本

    /**
     * 折叠时的文本
     */
    private String mEndExpandedText;
    /**
     * 展开时的文本
     */
    private String mEndCollapsedText;
    /**
     * 折叠情况下显示最大行数
     */
    private int limitLines;
    /**
     * 折叠后显示的行数
     */
    private int collapsedLines;
    /**
     * 记录行数
     */
    private int lineCount;
    /**
     * 原始的文本
     */
    private CharSequence mOriginalText;
    /**
     * 折叠后的文本
     */
    private CharSequence mCollapsedText;
    /**
     * 展开状态
     */
    private boolean mIsExpanded;
    /**
     * TextView中文字可显示的宽度
     */
    private int mShowWidth;
    /**
     * TextView中出现的ClickSpan的颜色
     */
    private int mTextLinkColor;
    /**
     * 展开收起的文字Span
     */
    private SpannableClickable mClickableSpan;

    private BufferType mBufferType = BufferType.SPANNABLE;

    private CollapsedTextViewCallback listener;

    public CollapsedTextView(Context context) {
        this(context, null);
    }

    public CollapsedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CollapsedTextView);

            setEndExpandedText(array.getString(R.styleable.CollapsedTextView_expanded_text));
            setEndCollapsedText(array.getString(R.styleable.CollapsedTextView_collapsed_text));
            setLimitLines(array.getInt(R.styleable.CollapsedTextView_limited_lines, DEFAULT_COLLAPSED_LIMIT),
                    array.getInt(R.styleable.CollapsedTextView_collapsed_lines, DEFAULT_COLLAPSED_LINES));
            setTextLinkColor(array.getColor(R.styleable.CollapsedTextView_text_link_color, SpannableClickable.DEFAULT_COLOR));

            array.recycle();
        } else {
            setEndExpandedText(null);
            setEndCollapsedText(null);
            setLimitLines(DEFAULT_COLLAPSED_LIMIT, DEFAULT_COLLAPSED_LINES);
        }
    }

    /**
     * 设置折叠时的提示文本
     *
     * @param expandedText 提示文本
     */
    public void setEndExpandedText(String expandedText) {
        this.mEndExpandedText = TextUtils.isEmpty(expandedText) ? EXPANDED_TEXT : expandedText;
    }

    /**
     * 设置展开时的提示文本
     *
     * @param collapsedText 提示文本
     */
    public void setEndCollapsedText(String collapsedText) {
        this.mEndCollapsedText = TextUtils.isEmpty(collapsedText) ? COLLAPSED_TEXT : collapsedText;
    }

    /**
     * 设置ClickSpan的文字颜色
     * @param textLinkColor
     */
    public void setTextLinkColor(int textLinkColor) {
        this.mTextLinkColor = textLinkColor;
    }

    /**
     * 设置行数规则
     *
     * @param limitLines 折叠情况下最大显示行数，超过该行数后进行折叠
     * @param collapsedLines 折叠后显示行数
     */
    public void setLimitLines(int limitLines, int collapsedLines) {
        if (limitLines < collapsedLines) {
            limitLines = collapsedLines;
        }

        if (limitLines < 1) {
            limitLines = 1;
        }

        if (collapsedLines < 1) {
            collapsedLines = 1;
        }

        this.limitLines = limitLines;
        this.collapsedLines = collapsedLines;
    }

    public void setShowWidth(int showWidth) {
        this.mShowWidth = showWidth;
    }

    public void setListener(CollapsedTextViewCallback listener) {
        this.listener = listener;
    }

    public void setExpanded(boolean isExpanded) {
        this.mIsExpanded = isExpanded;
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public void setContent(CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }

        updateContent(CharUtil.trimFrom(charSequence));

        // 如果text为空则直接显示
        if (TextUtils.isEmpty(mOriginalText)) {
            super.setText(mOriginalText, this.mBufferType);
        } else if (mIsExpanded) {
            // 保存原始文本，去掉文本末尾的空字符
            formatExpandedText();
        } else {
            // 获取TextView中文字显示的宽度，需要在layout之后才能获取到，避免重复获取
            if (mShowWidth == 0) {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mShowWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                        formatCollapsedText();
                    }
                });
            } else {
                formatCollapsedText();
            }
        }
    }

    private void updateContent(CharSequence charSequence) {
        if (mOriginalText != null && mOriginalText.equals(charSequence)) {
            return;
        }

        mOriginalText = charSequence;

        // 复位
        mCollapsedText = null;
        lineCount = 0;
    }

    /**
     * 格式化折叠时的文本
     */
    private void formatCollapsedText() {
        if (mCollapsedText != null && lineCount > 0) {
            // 有过记录，不用重新测量
            if (lineCount <= limitLines) {
                super.setText(mOriginalText, mBufferType);
            } else {
                SpannableStringBuilder spannable = new SpannableStringBuilder();
                spannable.append(mCollapsedText);
                spannable.append(ELLIPSE);
                setSpan(spannable);
                super.setText(spannable, mBufferType);
            }

            return;
        }

        // 没有记录重新获取
        TextPaint paint = getPaint();

        StaticLayout layout = new StaticLayout(mOriginalText, paint, mShowWidth,
                Layout.Alignment.ALIGN_NORMAL,
                getLineSpacingMultiplier(), getLineSpacingExtra(), false);

        lineCount = layout.getLineCount();

        if (lineCount <= limitLines) {
            // 没有超过最大行数
            mCollapsedText = mOriginalText;
            super.setText(mOriginalText, mBufferType);
        } else {
            // 超过限制的最大行数，进行折叠

            // 获取折叠后最后一行的文字
            int lastLineStart = layout.getLineStart(collapsedLines - 1);
            int lastLineEnd = layout.getLineVisibleEnd(collapsedLines - 1);

            // 计算后缀的宽度
            float expandedTextWidth =  paint.measureText(ELLIPSE + " " + mEndExpandedText);
            float lastLineWidth = paint.measureText(mOriginalText, lastLineStart, lastLineEnd);

            // 如果大于屏幕宽度则需要减去部分字符
            if (lastLineWidth + expandedTextWidth > mShowWidth) {
                int cutCount = paint.breakText(mOriginalText, lastLineStart, lastLineEnd, true, expandedTextWidth, null);
                lastLineEnd -= cutCount;
            }

            // 因设置的文本可能是带有样式的文本，如SpannableStringBuilder，所以根据计算的字符数从原始文本中截取
            SpannableStringBuilder spannable = new SpannableStringBuilder();
            // 截取文本，还是因为原始文本的样式原因不能直接使用paragraphs中的文本
            mCollapsedText = StringUtil.removeLastHalfEmoj(mOriginalText.subSequence(0, lastLineEnd));
            spannable.append(mCollapsedText);
            spannable.append(ELLIPSE);
            // 设置样式
            setSpan(spannable);
            super.setText(spannable, mBufferType);
        }
    }

    /**
     * 格式化展开式的文本，直接在后面拼接即可
     */
    private void formatExpandedText() {
        SpannableStringBuilder spannable = new SpannableStringBuilder(mOriginalText);
        setSpan(spannable);
        super.setText(spannable, mBufferType);
    }

    /**
     * 设置提示的样式
     *
     * @param spannable 需修改样式的文本
     */
    private void setSpan(SpannableStringBuilder spannable) {
        // 根据提示文本需要展示的文字拼接不同的字符
        spannable.append(" ");
        int tipsLen;
        // 判断是展开还是收起
        if (mIsExpanded) {
            spannable.append(mEndCollapsedText);
            tipsLen = mEndCollapsedText.length();
        } else {
            spannable.append(mEndExpandedText);
            tipsLen = mEndExpandedText.length();
        }
        // 设置点击事件
        spannable.setSpan(getClickableSpan(), spannable.length() - tipsLen,
                spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private SpannableClickable getClickableSpan() {
        if (mClickableSpan == null) {
            mClickableSpan = new SpannableClickable(mTextLinkColor) {
                @Override
                public void onClick(@NonNull View widget) {
                    mIsExpanded = !mIsExpanded;
                    setContent(mOriginalText);

                    if (listener != null) {
                        listener.onCollapseClick(mIsExpanded);
                    }
                }
            };
        }

        return mClickableSpan;
    }

    public interface CollapsedTextViewCallback {
        /**
         * 点击“收起”回调
         */
        void onCollapseClick(boolean expand);
    }
}
