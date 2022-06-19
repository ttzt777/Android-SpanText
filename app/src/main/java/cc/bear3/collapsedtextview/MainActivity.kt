package cc.bear3.collapsedtextview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.bear3.span.collapsed.CollapsedTextView
import cc.bear3.span.kernel.*

class MainActivity : AppCompatActivity() {
    private var titleTextView: CollapsedTextView? = null
    private var charSequenceView: TextView? = null
    private val changed = false

    init {
//        CustomClickableSpan.defaultTextColor = -0x31db
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        initViews()
    }

    private fun bindViews() {
        titleTextView = findViewById(R.id.tv_title)
        charSequenceView = findViewById(R.id.tv_char_sequence)
        titleTextView?.setOnClickListener(View.OnClickListener { //                int showWidth = 500;
//                ViewGroup.LayoutParams params = titleTextView.getLayoutParams();
//                params.width = showWidth;
//                titleTextView.setLayoutParams(params);
//                titleTextView.setShowWidth(showWidth);
            titleTextView?.setEndExpandText("来来来来来来来来来")
            titleTextView?.setTextLinkColor(Color.MAGENTA)
            titleTextView?.setTextLinkBgColor(Color.CYAN)
        })

        charSequenceView?.withCharSequence(
            createDrawableCharSequence(this, R.mipmap.ic_launcher),
            createMarginCharSequence(100),
            "正常",
            "加粗".toBold(),
            "斜体".toItalic(),
            "加粗并且斜体".toBoldItalic(),
            "上标".toSuperscript(),
            "下划线".toUnderline(),
            "下标".toSubscript(),
            "中划线".toStrikethrough(),
            "文字颜色".toForeground(Color.GREEN),
            "背景颜色".toBackground(Color.YELLOW),
            "文字大小".toFontSize(12),
            "全体缩放".toScale(2.0f),
            "X缩放".toScaleX(1.5f),
            "点击".toClickWithEffect(Color.BLUE) {
                Toast.makeText(this, "点击了Span", Toast.LENGTH_SHORT).show()
            },
            "大杂烩".toBoldItalic().toUnderline().toStrikethrough().toForeground(Color.CYAN)
                .toBackground(Color.GRAY).toFontSize(24),
            "百度一下，你就知道".toUrl("https://www.baidu.com")
        )
//        charSequenceView?.movementMethod = LinkMovementMethod.getInstance()
        charSequenceView?.setOnClickListener {
            Toast.makeText(this, "点击了TextView", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
//        titleTextView.setContent(getString(R.string.title_text));
    }
}