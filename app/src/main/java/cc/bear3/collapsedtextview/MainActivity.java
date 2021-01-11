package cc.bear3.collapsedtextview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import cc.bear3.view.collapsedtextview.ClickableSpanTextView;
import cc.bear3.view.collapsedtextview.CollapsedTextView;
import cc.bear3.view.collapsedtextview.ColorClickableSpan;

public class MainActivity extends AppCompatActivity {

    private CollapsedTextView titleTextView;

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
                titleTextView.setTextLinkColor(Color.BLACK);
                titleTextView.setTextLinkBgColor(Color.CYAN);
            }
        });
    }

    private void initViews() {
//        titleTextView.setContent(getString(R.string.title_text));
    }
}
