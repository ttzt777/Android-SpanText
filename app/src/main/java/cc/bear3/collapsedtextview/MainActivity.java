package cc.bear3.collapsedtextview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cc.bear3.weight.textview.collapsed.CollapsedTextView;

public class MainActivity extends AppCompatActivity {

    private CollapsedTextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initViews();
    }

    private void bindViews() {
        titleTextView = findViewById(R.id.tv_title);
    }

    private void initViews() {
        titleTextView.setContent(getString(R.string.title_text));
    }
}
