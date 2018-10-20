package io.github.dawncraft.qingchenw.random;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity
{
    // 更新地址
    public static final String WEBSITE_URL = "https://github.com/DawningW/Random";
    // 开源协议
    public static final String LICENSE = "license.txt";

    // 控件
    @BindView(R.id.versionText)
    public TextView versionText;
    @BindView(R.id.updateText)
    public TextView updateText;
    @BindView(R.id.websiteText)
    public TextView websiteText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        PackageInfo info = Utils.getPackageInfo(this);
        if (info != null) versionText.setText(info.versionName);
        updateText.setText(MainActivity.haveUpdate ? R.string.have_update : R.string.no_update);
        websiteText.setText(WEBSITE_URL);
    }

    public void onClicked(View view)
    {
        switch(view.getId())
        {
            case R.id.websiteText:
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)));
                break;
            }
            case R.id.licenseButton:
            {
                licenseDialog();
                break;
            }
        }
    }

    public void licenseDialog()
    {
        String license = "";
        try
        {
            license = Utils.read(new InputStreamReader(getResources().getAssets().open(LICENSE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view_license).setCancelable(false);
        builder.setMessage(license);
        builder.setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
