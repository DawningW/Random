package io.github.dawncraft.qingchenw.random.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.RandomApplication;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

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
        codeEditor.getSetting().setWorkingFolder(RandomApplication.RES_PATH);
        codeEditor.getTextProcessor().setCodeCompletion(false);
        codeEditor.setText(FileUtils.readFile(RandomApplication.RES_PATH + "/" + FILE_NAME), 1);
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
                SystemUtils.toast(this, R.string.code_invalid2);
            }
        }
        // 保存配置
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CodeActivity.this).edit();
        editor.putBoolean("custom_code", MainActivity.codeEnabled);
        editor.apply();
        FileUtils.writeFile(RandomApplication.RES_PATH + "/" + FILE_NAME, str);
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
        SystemUtils.toast(this, "语法检查功能尚不可用");
        return true;
    }
}
