package io.github.dawncraft.qingchenw.random.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.baidu.tts.client.SpeechSynthesizerListener;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import org.mozilla.javascript.tools.jsc.Main;

import java.util.List;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.ui.fragments.*;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

import static java.lang.System.currentTimeMillis;

/**
 * 叫号应用的主界面
 * <p>
 * Created by QingChenW on 2018/6/25
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, SpeechSynthesizerListener
{
    // 退出计时器
    private long exitTime = 0;
    // 内部的Fragment
    private BaseModeFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        int mode = getIntent().getIntExtra("mode", 0);
        fragment = generateModeFragment(mode);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 进入主Activity时申请权限
        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest)
                    {
                        scope.showRequestReasonDialog(deniedList, "列表导入/导出功能及应用更新需要使用外部存储,请授予权限!", "好的");
                    }
                })
                .request(new RequestCallback()
                {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList)
                    {
                        if (!allGranted)
                        {
                            SystemUtils.toast(MainActivity.this, "未授予外部存储权限, 无法使用列表导入和导出功能!");
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_flashlight:
            {
                startActivity(new Intent(this, FlashlightActivity.class));
                return true;
            }
            case R.id.action_code:
            {
                startActivity(new Intent(this, CodeActivity.class));
                return true;
            }
            case R.id.action_feedback:
            {
                // TODO 反馈
                return true;
            }
            case R.id.action_help:
            {
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            }
            case R.id.action_about:
            {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            case R.id.action_exit:
            {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (fragment.stop()) return;
        if ((currentTimeMillis() - exitTime) > 2000)
        {
            SystemUtils.toast(this, R.string.exit);
            exitTime = currentTimeMillis();
        }
        else
        {
            finish();
        }
    }

    private BaseModeFragment generateModeFragment(int mode)
    {
        switch (mode)
        {
            default:
            case 0: return new SingleModeFragment();
            case 1: return new ContinuousModeFragment();
            case 2: return new RemoteModeFragment();
        }
    }
}
