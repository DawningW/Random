package io.github.dawncraft.qingchenw.random;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.github.ahmadaghazadeh.editor.widget.CodeEditor;

public class CodeActivity extends AppCompatActivity
{
    // 伪随机算法的文件名
    public static final String FILE_NAME = "random.js";
    // 伪随机算法的代码模版
    public static final String BASE_CODE = "function generate(list, range, result) { %s }";
    public static String formatCode(String code)
    {
        return String.format(BASE_CODE, code);
    }

    // 控件
    @BindView(R.id.codeSwitch)
    public Switch codeSwitch;
    @BindView(R.id.codeEditor)
    public CodeEditor codeEditor;
    @BindView(R.id.codeButton)
    public Button codeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 初始化控件
        codeSwitch.setChecked(MainActivity.codeEnabled);
        codeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                MainActivity.codeEnabled = isChecked;
                switchState(isChecked);
            }
        });
        codeEditor.getSetting().setWorkingFolder(MainActivity.RES_PATH);
        codeEditor.getTextProcessor().setCodeCompletion(false);
        codeEditor.setText(Utils.readFile(MainActivity.RES_PATH + "/" + FILE_NAME), 1);
        switchState(MainActivity.codeEnabled);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        String str = codeEditor.getText();
        String code = formatCode(str);
        // 检查代码
        if (MainActivity.codeEnabled)
        {
            if (check(code))
            {
                MainActivity.code = code;
            }
            else
            {
                MainActivity.codeEnabled = false;
                Utils.toast(this, R.string.code_invalid2);
            }
        }
        // 保存配置
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CodeActivity.this).edit();
        editor.putBoolean("custom_code", MainActivity.codeEnabled);
        editor.apply();
        Utils.writeFile(MainActivity.RES_PATH + "/" + FILE_NAME, str);
    }

    public void onClicked(View view)
    {
        if (view.getId() == R.id.codeButton)
            check(formatCode(codeEditor.getText()));
    }

    public void switchState(boolean enabled)
    {
        codeEditor.setReadOnly(!enabled);
        codeEditor.getTextProcessor().setEnabled(enabled);
        codeButton.setEnabled(enabled);
    }

    public boolean check(String code)
    {
        // TODO 语法检查功能
        Utils.toast(this, "语法检查功能尚不可用");
        return true;
    }
}
