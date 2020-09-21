package io.github.dawncraft.qingchenw.random.ui;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity
{
    // 开源协议
    public static final String LICENSE = "license.txt";

    // 控件
    public View aboutView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        PackageInfo info = SystemUtils.getPackageInfo(this);
        String version = info != null ? info.versionName : getString(android.R.string.unknownName);
        AboutPage aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(getString(R.string.about_description))
                .addItem(new Element().setTitle(getString(R.string.app_name)))
                .addItem(new Element().setTitle(String.format(getString(R.string.about_version), version)))
                .addItem(new Element()
                        .setTitle(getString(MainActivity.haveUpdate ? R.string.have_update : R.string.no_update)))
                .addItem((new Element()).setTitle(getString(R.string.copyright)))
                .addGroup(getString(R.string.about_group_connect))
                .addWebsite("https://github.com/DawningW/Random")
                .addEmail("1132694623@qq.com")
                .addGitHub("DawningW")
                .addGroup(getString(R.string.about_group_other))
                .addItem(new Element()
                        .setTitle(getString(R.string.about_view_license))
                        .setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                licenseDialog();
                            }
                        }));
        aboutView = aboutPage.create();
        setContentView(aboutView);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    public void licenseDialog()
    {
        String license = "";
        try
        {
            license = FileUtils.read(new InputStreamReader(getResources().getAssets().open(LICENSE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_view_license).setCancelable(false);
        builder.setMessage(license);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
