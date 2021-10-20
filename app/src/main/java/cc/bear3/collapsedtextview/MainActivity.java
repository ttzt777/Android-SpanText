package cc.bear3.collapsedtextview;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cc.bear3.span.collapsed.CollapsedTextView;
import cc.bear3.span.kernel.ColorClickableSpan;

public class MainActivity extends AppCompatActivity {

    private CollapsedTextView titleTextView;

    private boolean changed = false;

    static {
        ColorClickableSpan.Companion.setDefaultTextColor(0xFFFFCE25);
        ColorClickableSpan.Companion.setDefaultBgColorAlphaByTextColor(0.3f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initViews();
    }

    private void bindViews() {
        titleTextView = findViewById(R.id.tv_title);

        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int showWidth = 500;
//                ViewGroup.LayoutParams params = titleTextView.getLayoutParams();
//                params.width = showWidth;
//                titleTextView.setLayoutParams(params);
//                titleTextView.setShowWidth(showWidth);

                titleTextView.setEndExpandText("来来来来来来来来来");

                titleTextView.setTextLinkColor(Color.MAGENTA);
                titleTextView.setTextLinkBgColor(Color.CYAN);
            }
        });
    }

    private void initViews() {
//        titleTextView.setContent(getString(R.string.title_text));
    }
}
