package io.github.dawncraft.qingchenw.random;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CodeActivity extends AppCompatActivity
{
    // 伪随机算法的文件名
    public static final String FILE_NAME = "random.js";
    // 伪随机算法的代码模版
    public static final String BASE_CODE = "function generate(list, range, result) { %s }";

    // 控件
    @BindView(R.id.codeSwitch)
    public Switch codeSwitch;
    @BindView(R.id.codeEditor)
    public EditText codeEditor;
    @BindView(R.id.codeButtonBar)
    public LinearLayout codeButtonBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        codeSwitch.setChecked(MainActivity.codeEnabled);
        codeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                MainActivity.codeEnabled = isChecked;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CodeActivity.this).edit();
                editor.putBoolean("custom_code", isChecked);
                editor.apply();
                switchState(isChecked);
            }
        });
        codeEditor.setText(Utils.readFile(MainActivity.VOICE_RES_PATH + FILE_NAME));
        switchState(MainActivity.codeEnabled);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (MainActivity.codeEnabled && !check(MainActivity.code))
        {
            MainActivity.codeEnabled = false;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CodeActivity.this).edit();
            editor.putBoolean("custom_code", MainActivity.codeEnabled);
            editor.apply();
            Utils.toast(this, "代码有错误,伪随机算法已禁用");
        }
    }

    public void onClicked(View view)
    {
        switch(view.getId())
        {
            case R.id.codeCheckButton:
            {
                check(String.format(CodeActivity.BASE_CODE, codeEditor.getText().toString()));
                break;
            }
            case R.id.codeSaveButton:
            {
                save();
                break;
            }
        }
    }

    public void switchState(boolean enabled)
    {
        codeEditor.setEnabled(enabled);
        for (int i = 0; i < codeButtonBar.getChildCount(); i++)
        {
            codeButtonBar.getChildAt(i).setEnabled(enabled);
        }
    }

    public boolean check(String code)
    {
        return false;
    }

    public void save()
    {
        String str = codeEditor.getText().toString();
        String code = String.format(CodeActivity.BASE_CODE, str);
        if (check(code))
        {
            MainActivity.code = code;
            Utils.writeFile(MainActivity.VOICE_RES_PATH + FILE_NAME, str);
        }
        else
        {
            Utils.toast(this, "代码有错误,伪随机算法保存失败");
        }
    }
}
