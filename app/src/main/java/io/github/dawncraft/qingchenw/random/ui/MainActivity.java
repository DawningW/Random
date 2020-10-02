package io.github.dawncraft.qingchenw.random.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.xuexiang.xupdate.XUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.RandomApplication;
import io.github.dawncraft.qingchenw.random.tts.MySynthesizer;
import io.github.dawncraft.qingchenw.random.tts.OfflineResource;
import io.github.dawncraft.qingchenw.random.ui.fragments.BaseModeFragment;
import io.github.dawncraft.qingchenw.random.ui.fragments.ContinuousModeFragment;
import io.github.dawncraft.qingchenw.random.ui.fragments.RemoteModeFragment;
import io.github.dawncraft.qingchenw.random.ui.fragments.SingleModeFragment;
import io.github.dawncraft.qingchenw.random.utils.ElementList;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;
import io.github.dawncraft.qingchenw.random.utils.RandomEngine;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;
import io.github.dawncraft.qingchenw.random.utils.Utils;

import static io.github.dawncraft.qingchenw.random.RandomApplication.RES_PATH;
import static java.lang.System.currentTimeMillis;

/**
 * 叫号应用的主界面
 * <p>
 * Created by QingChenW on 2018/6/25
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, SpeechSynthesizerListener
{
    // 灵敏度
    public static int sensitivity;
    // 震动
    public static boolean vibratorEnabled;
    public static long vibrateTime;
    // 音效
    public static boolean soundEnabled;
    // 语音合成
    public static boolean voiceEnabled;
    public static MySynthesizer.Config synthesizerConfig = new MySynthesizer.Config();
    public static OfflineResource offlineResource;
    // 随机算法
    public static boolean codeEnabled;
    public static String code;

    // 内部的Fragment
    private BaseModeFragment fragment;
    // 退出计时器
    private long exitTime = 0;

    // 元素列表
    public List<Pair<String, String>> elements = new ArrayList<>();

    // 传感器
    public SensorManager sensorManager;
    // 振动
    public Vibrator vibrator;
    // 音效池
    public SoundPool soundPool;
    // 百度语音合成引擎
    public MySynthesizer speechSynthesizer;
    // 随机数生成引擎
    public RandomEngine<Pair<String, String>> randomEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        int mode = RandomApplication.sharedPreferences.getInt("mode", 0);
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
        // 动态申请权限
        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest)
                    {
                        scope.showRequestReasonDialog(deniedList, "列表导入/导出功能及应用更新需要使用外部存储,请授予权限!", getString(android.R.string.ok));
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
        // 读取配置
        loadPreferences(RandomApplication.sharedPreferences);
        // 检查更新
        XUpdate.newBuild(this)
                .updateUrl(RandomApplication.UPDATE_URL)
                .apkCacheDir(RES_PATH)
                .update();

        // 获取传感器管理器
        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (sensorManager != null)
        {
            // 获取加速度传感器
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometerSensor != null)
            {
                // 注册传感器监听器
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
        // 获取震动
        if (vibratorEnabled)
        {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        // 获取音效
        if (soundEnabled)
        {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        }
        // 初始化语音合成引擎
        if (voiceEnabled)
        {
            speechSynthesizer = new MySynthesizer(this);
            synthesizerConfig.setListener(this);
            new Thread()
            {
                @Override
                public void run()
                {
                    super.run();
                    if (speechSynthesizer != null)
                    {
                        speechSynthesizer.init(synthesizerConfig);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                fragment.onSynthesizerInit();
                            }
                        });
                    }
                }
            }.start();
        }
        // 初始化随机数生成引擎
        randomEngine = new RandomEngine<>();
        randomEngine.setElementList(elements);
        if (codeEnabled)
        {
            randomEngine.initJSEngine();
            randomEngine.setScript(code);
        }
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
        if (fragment.isShaking())
        {
            fragment.stop();
        }
        else
        {
            if ((currentTimeMillis() - exitTime) > 2000)
            {
                SystemUtils.toast(this, R.string.press_to_exit);
                exitTime = currentTimeMillis();
            }
            else
            {
                finish();
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // 注销传感器
        if (sensorManager != null)
        {
            // 注销传感器监听器
            // 否则界面退出后摇一摇依旧生效
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        // 释放音效池
        if (soundPool != null)
        {
            soundPool.release();
            soundPool = null;
        }
        // 停止语音合成引擎
        if (speechSynthesizer != null)
        {
            speechSynthesizer.stop();
            speechSynthesizer.release();
            speechSynthesizer = null;
        }
        // 释放随机数生成引擎
        if (randomEngine != null)
        {
            randomEngine.release();
            randomEngine = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        int type = sensorEvent.sensor.getType();
        // 加速度传感器
        if (type == Sensor.TYPE_ACCELEROMETER && !fragment.isShaking())
        {
            // 获取三个方向值
            for (float value : sensorEvent.values)
            {
                if (Math.abs(value) > sensitivity)
                {
                    fragment.start();
                    break;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onSynthesizeStart(String s) {}

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int j) {}

    @Override
    public void onSynthesizeFinish(String s) {}

    @Override
    public void onSpeechStart(String s) {}

    @Override
    public void onSpeechProgressChanged(String s, int i) {}

    @Override
    public void onSpeechFinish(String s)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                fragment.onSpeechFinish(s);
            }
        });
    }

    @Override
    public void onError(String s, final SpeechError speechError)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                fragment.onSynthesizerError(s, speechError);
            }
        });
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

    public void loadPreferences(SharedPreferences preferences)
    {
        // 读取集合
        String toSplit = preferences.getString("elements", "");
        if (Utils.isStrNullOrEmpty(toSplit))
        {
            elements = ElementList.fromString(toSplit).toList();
        }
        // 摇晃设置
        sensitivity = Integer.parseInt(Objects.requireNonNull(preferences.getString("sensitivity", "20")));
        // 震动设置
        vibratorEnabled = preferences.getBoolean("vibrator_switch", true);
        vibrateTime = Integer.parseInt(Objects.requireNonNull(preferences.getString("vibrator_time", "300")));
        // 音效设置
        soundEnabled = preferences.getBoolean("sound_switch", true);
        // 语音设置
        voiceEnabled = preferences.getBoolean("voice_switch", true);
        synthesizerConfig.setTtsMode(preferences.getBoolean("voice_mode", false) ? TtsMode.MIX : TtsMode.ONLINE);
        Map<String, String> params = new HashMap<>();
        boolean network = preferences.getBoolean("voice_network", false);
        boolean time = preferences.getBoolean("voice_overtime", false);
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, network ?
                (time ? SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK : SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE) :
                (time ? SpeechSynthesizer.MIX_MODE_DEFAULT : SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI));
        String speakerID = Objects.requireNonNull(preferences.getString("voice_speaker", "0"));
        params.put(SpeechSynthesizer.PARAM_SPEAKER, speakerID);
        String speakerType = "";
        switch (Integer.parseInt(speakerID))
        {
            default:
            case 0: speakerType = OfflineResource.VOICE_FEMALE; break;
            case 1: speakerType = OfflineResource.VOICE_MALE; break;
            case 2: speakerType = OfflineResource.VOICE_DUXY; break;
            case 3: speakerType = OfflineResource.VOICE_DUYY; break;
        }
        offlineResource = new OfflineResource(this, speakerType);
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFileLocation());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFileLocation());
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(preferences.getInt("voice_volume", 5)));
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(preferences.getInt("voice_speed", 5)));
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(preferences.getInt("voice_pitch", 5)));
        synthesizerConfig.setParams(params);
        // 随机算法
        codeEnabled =  preferences.getBoolean("custom_code", false);
        code = CodeActivity.formatCode(FileUtils.readFile(RES_PATH + "/" + CodeActivity.FILE_NAME));
    }
}
