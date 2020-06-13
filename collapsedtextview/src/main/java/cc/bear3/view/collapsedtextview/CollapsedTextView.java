package cc.bear3.view.collapsedtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import cc.bear3.view.collapsedtextview.collapsed.R;

@SuppressWarnings("unused")
public class CollapsedTextView extends ClickableSpanTextView {
    private static final boolean DEFAULT_COLLAPSE_ABLE = true;
    private static final boolean DEFAULT_EXPAND_ABLE = true;
    private static final int DEFAULT_COLLAPSED_LIMIT = 15;      // 不显示展开全文的最大显示行数
    private static final int DEFAULT_COLLAPSED_LINES = 10;      // 折叠后行数
    private static final String ELLIPSE = "...";

    // 是否允许折叠 true -- 折叠功能 false -- 普通TextView
    private boolean collapseEnable = DEFAULT_COLLAPSE_ABLE;
    // 是否允许展开 true -- 展开、收起 false -- 无任何点击事件，只会显示"展开全文"
    private boolean expandEnable = DEFAULT_EXPAND_ABLE;
    // 折叠情况下显示最大行数
    private int limitLines = DEFAULT_COLLAPSED_LIMIT;
    // 折叠后显示的行数
    private int collapsedLines = DEFAULT_COLLAPSED_LINES;
    // 展开的文本（展开全文）
    private String endExpandText;
    // 折叠的文本（收起）
    private String endCollapseText;
    // "展开、收起"的Span的颜色
    private @ColorInt
    int textLinkColor;
    // "展开、收起"的Span点击后的背景颜色
    private @ColorInt
    int textLinkBgColor;

    // 原始的文本
    private CharSequence originalText;
    // 折叠后的文本
    private CharSequence collapsedText;
    // 记录行数
    private int lineCount;
    // 展开状态
    private boolean isExpanded;
    // TextView中文字可显示的宽度
    private int showWidth;

    // 展开收起的文字Span
    private CharacterStyle endTextSpan;
    // buffer type 固定为Spannable
    private BufferType bufferType = BufferType.SPANNABLE;

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

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!collapseEnable) {
            super.setText(text, type);
            return;
        }

        if (text == null) {
            text = "";
        }

        updateContent(CharUtil.trimFrom(text));

        // 如果text为空则直接显示
        if (TextUtils.isEmpty(originalText)) {
            super.setText(originalText, this.bufferType);
        } else if (isExpanded) {
            // 保存原始文本，去掉文本末尾的空字符
            formatExpandedText();
        } else {
            // 获取TextView中文字显示的宽度，需要在layout之后才能获取到，避免重复获取
            if (showWidth == 0) {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        showWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                        formatCollapsedText();
                    }
                });
            } else {
                formatCollapsedText();
            }
        }
    }

    /**
     * 设置是否允许折叠
     *
     * @param collapseEnable true -- 具有折叠功能， false -- 普通TextView
     */
    public void setCollapseEnable(boolean collapseEnable) {
        this.collapseEnable = collapseEnable;

        setText(getText(), bufferType);
    }

    /**
     * 设置是否可以展开
     * 点击"全文"是展开还是跳转（TextView或parent的OnClick）
     *
     * @param expandEnable true -- 当前展开 false -- 不做任何处理
     */
    public void setExpandEnable(boolean expandEnable) {
        this.expandEnable = expandEnable;

        setText(getText(), bufferType);
    }

    /**
     * 设置展开的提示文本{@link CollapsedTextView#setEndExpandText(String, boolean)}
     *
     * @param expandText 提示文本
     */
    public void setEndExpandText(String expandText) {
        setEndExpandText(expandText, true);
    }

    /**
     * 设置展开的提示文本
     *
     * @param expandText 提示文本
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    public void setEndExpandText(String expandText, boolean refresh) {
        this.endExpandText = TextUtils.isEmpty(expandText) ? getContext().getString(R.string.expand_text) : expandText;

        if (refresh) {
            setText(getText(), bufferType);
        }
    }

    /**
     * 设置折叠的提示文本{@link CollapsedTextView#setEndCollapseText(String, boolean)}
     *
     * @param collapseText 提示文本
     */
    public void setEndCollapseText(String collapseText) {
        setEndCollapseText(collapseText, true);
    }

    /**
     * 设置折叠的提示文本
     *
     * @param collapseText 提示文本
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    public void setEndCollapseText(String collapseText, boolean refresh) {
        this.endCollapseText = TextUtils.isEmpty(collapseText) ? getContext().getString(R.string.collapse_text) : collapseText;

        if (refresh) {
            setText(getText(), bufferType);
        }
    }

    /**
     * 设置ClickSpan的文字颜色
     *
     * @param textLinkColor 展开、收起的字体颜色
     */
    public void setTextLinkColor(@ColorInt int textLinkColor) {
        this.textLinkColor = textLinkColor;

        setText(getText(), bufferType);
    }

    /**
     * 设置ClickSpan的点击后的背景颜色
     *
     * @param textLinkBgColor 展开、收起的点击后的背景颜色
     */
    public void setTextLinkBgColor(@ColorInt int textLinkBgColor) {
        this.textLinkBgColor = textLinkBgColor;

        setText(getText(), bufferType);
    }

    /**
     * 设置折叠行数{@link CollapsedTextView#setLimitLines(int, int, boolean)}
     * 
     * @param limitLines 超过该行数开始折叠
     * @param collapsedLines 折叠后的行数
     */
    public void setLimitLines(int limitLines, int collapsedLines) {
        setLimitLines(limitLines, collapsedLines, true);
    }

    /**
     * 设置行数规则
     *
     * @param limitLines     折叠情况下最大显示行数，超过该行数后进行折叠
     * @param collapsedLines 折叠后显示行数
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    public void setLimitLines(int limitLines, int collapsedLines, boolean refresh) {
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

        if (refresh) {
            setText(getText(), bufferType);
        }
    }

    /**
     * 设置文字可用的显示宽度{@link CollapsedTextView#setShowWidth(int, boolean)}
     * 
     * @param showWidth 宽度
     */
    public void setShowWidth(int showWidth) {
        setShowWidth(showWidth, false);
    }

    /**
     * 设置文字可用的显示宽度
     * 
     * @param showWidth 宽度
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    public void setShowWidth(int showWidth, boolean refresh) {
        if (this.showWidth == showWidth) {
            return;
        }

        this.showWidth = showWidth;

        if (refresh) {
            setText(getText(), bufferType);
        }
    }
    
    /**
     * 设置当前为折叠还是展开状态{@link CollapsedTextView#setExpanded(boolean, boolean)}
     *
     * @param isExpanded 是否为展开状态
     */
    public void setExpanded(boolean isExpanded) {
        setExpanded(isExpanded, false);
    }

    /**
     * 设置当前为折叠还是展开状态
     * 
     * @param isExpanded 是否为展开状态
     * @param refresh 是否刷新，具体看在这个方法调用后有没有调用setText方法
     */
    public void setExpanded(boolean isExpanded, boolean refresh) {
        if (this.isExpanded == isExpanded) {
            return;
        }

        this.isExpanded = isExpanded;

        if (refresh) {
            setText(getText(), bufferType);
        }
    }

    /**
     * 获取当前的展开折叠状态
     * 
     * @return true -- 展开  false -- 收起
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * 设置点击展开、收起的点击回调
     * 
     * @param listener 回调
     */
    public void setListener(CollapsedTextViewCallback listener) {
        this.listener = listener;
    }


    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CollapsedTextView);

            collapseEnable = array.getBoolean(R.styleable.CollapsedTextView_ctv_collapse_able, collapseEnable);
            expandEnable = array.getBoolean(R.styleable.CollapsedTextView_ctv_expand_able, expandEnable);
            setLimitLines(
                    array.getInt(R.styleable.CollapsedTextView_ctv_limited_lines, DEFAULT_COLLAPSED_LIMIT),
                    array.getInt(R.styleable.CollapsedTextView_ctv_collapsed_lines, DEFAULT_COLLAPSED_LINES),
                    false);
            setEndExpandText(array.getString(R.styleable.CollapsedTextView_ctv_expand_text), false);
            setEndCollapseText(array.getString(R.styleable.CollapsedTextView_ctv_collapse_text), false);
            textLinkColor = array.getColor(R.styleable.CollapsedTextView_ctv_text_link_color, ColorClickableSpan.DEFAULT_COLOR);
            textLinkBgColor = array.getColor(R.styleable.CollapsedTextView_ctv_text_link_color, ColorClickableSpan.getDefaultBgColorByTextColor(ColorClickableSpan.DEFAULT_COLOR));

            array.recycle();
        } else {
            setEndExpandText(null, false);
            setEndCollapseText(null, false);
            textLinkColor = ColorClickableSpan.DEFAULT_COLOR;
            textLinkBgColor = ColorClickableSpan.getDefaultBgColorByTextColor(ColorClickableSpan.DEFAULT_COLOR);
        }

        // 重置一遍，因为TextView获取到Xml中设置的text在自定义View属性获取之前
        if (!TextUtils.isEmpty(getText())) {
            setText(getText());
        }
    }

    private void updateContent(CharSequence charSequence) {
        if (originalText != null && originalText.equals(charSequence)) {
            return;
        }

        originalText = charSequence;

        // 复位
        collapsedText = null;
        lineCount = 0;
    }

    /**
     * 格式化折叠时的文本
     */
    private void formatCollapsedText() {
        if (collapsedText != null && lineCount > 0) {
            // 有过记录，不用重新测量
            if (lineCount <= limitLines) {
                super.setText(originalText, bufferType);
            } else {
                SpannableStringBuilder spannable = new SpannableStringBuilder();
                spannable.append(collapsedText);
                spannable.append(ELLIPSE);
                setSpan(spannable);
                super.setText(spannable, bufferType);
            }

            return;
        }

        // 没有记录重新获取
        StaticLayout layout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(
                    originalText,
                    0,
                    originalText.length(),
                    getPaint(),
                    showWidth);
            builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                    .setIncludePad(false);
            layout = builder.build();
        } else {
            layout = new StaticLayout(originalText, getPaint(), showWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    getLineSpacingMultiplier(), getLineSpacingExtra(), false);
        }

        lineCount = layout.getLineCount();

        if (lineCount <= limitLines) {
            // 没有超过最大行数
            collapsedText = originalText;
            super.setText(originalText, bufferType);
        } else {
            // 超过限制的最大行数，进行折叠

            // 获取折叠后最后一行的文字
            int lastLineStart = layout.getLineStart(collapsedLines - 1);
            int lastLineEnd = layout.getLineVisibleEnd(collapsedLines - 1);

            // 计算后缀的宽度
            float expandedTextWidth = StaticLayout.getDesiredWidth(ELLIPSE + " " + endExpandText, getPaint());
            float lastLineWidth = StaticLayout.getDesiredWidth(originalText, lastLineStart, lastLineEnd, getPaint());

            // 如果大于屏幕宽度则需要减去部分字符
            if (lastLineWidth + expandedTextWidth > showWidth) {
                int cutCount = getPaint().breakText(originalText, lastLineStart, lastLineEnd, true, expandedTextWidth, null);
                lastLineEnd -= cutCount;
            }

            // 因设置的文本可能是带有样式的文本，如SpannableStringBuilder，所以根据计算的字符数从原始文本中截取
            SpannableStringBuilder spannable = new SpannableStringBuilder();
            // 截取文本，还是因为原始文本的样式原因不能直接使用paragraphs中的文本
            collapsedText = StringUtil.removeLastHalfEmoj(originalText.subSequence(0, lastLineEnd));
            spannable.append(collapsedText);
            spannable.append(ELLIPSE);
            // 设置样式
            setSpan(spannable);
            super.setText(spannable, bufferType);
        }
    }

    /**
     * 格式化展开式的文本，直接在后面拼接即可
     */
    private void formatExpandedText() {
        SpannableStringBuilder spannable = new SpannableStringBuilder(originalText);
        setSpan(spannable);
        super.setText(spannable, bufferType);
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
        if (isExpanded) {
            spannable.append(endCollapseText);
            tipsLen = endCollapseText.length();
        } else {
            spannable.append(endExpandText);
            tipsLen = endExpandText.length();
        }
        // 设置点击事件
        spannable.setSpan(getEndTextSpan(), spannable.length() - tipsLen,
                spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @NonNull
    private CharacterStyle getEndTextSpan() {
        if (expandEnable) {
            return getEndTextClickableSpan();
        } else {
            return getEndTextForegroundSpan();
        }
    }

    @NonNull
    private CharacterStyle getEndTextClickableSpan() {
        if (endTextSpan instanceof ColorClickableSpan) {
            ((ColorClickableSpan) endTextSpan).setTextColor(textLinkColor, textLinkBgColor);
        } else {
            endTextSpan = new ColorClickableSpan(textLinkColor, clickableSpanBgColor) {
                @Override
                public void onClick(@NonNull View widget) {
                    isExpanded = !isExpanded;
                    setText(originalText, bufferType);

                    if (listener != null) {
                        listener.onCollapseClick(isExpanded);
                    }
                }
            };
        }

        return endTextSpan;
    }

    @NonNull
    private CharacterStyle getEndTextForegroundSpan() {
        if (endTextSpan instanceof ForegroundColorSpan) {
            if (((ForegroundColorSpan) endTextSpan).getForegroundColor() != textLinkColor) {
                endTextSpan = new ForegroundColorSpan(textLinkColor);
            }
        } else {
            endTextSpan = new ForegroundColorSpan(textLinkColor);
        }

        return endTextSpan;
    }

    public interface CollapsedTextViewCallback {
        /**
         * 点击“展开全文/收起”回调
         * @param expand 目标展开是否为展开状态
         */
        void onCollapseClick(boolean expand);
    }
}
