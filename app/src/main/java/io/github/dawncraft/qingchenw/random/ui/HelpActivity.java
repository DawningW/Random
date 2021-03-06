package io.github.dawncraft.qingchenw.random.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity
{
    // 控件
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 设置字符集编码
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        // 加载assets目录下的网页
        webView.loadUrl("file:///android_asset/help/index.html");
    }
}
