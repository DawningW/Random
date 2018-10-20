package io.github.dawncraft.qingchenw.random;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity
{
    // 控件
    @BindView(R.id.webView)
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
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
        String url = "file:///android_asset/help/index.html";
        webView.loadUrl(url);
    }
}
