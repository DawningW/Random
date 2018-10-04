package io.github.dawncraft.qingchenw.random;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindAnim;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;
import static java.lang.System.currentTimeMillis;

/**
 * 一个摇号的小应用
 * 感谢changer0的教程https://www.jianshu.com/p/bc5298651b30
 * <p>
 * Created by QingChenW on 2018/6/25
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, SpeechSynthesizerListener
{
    // 权限
    public static final String PERMISSIONS[] = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };
    // 灵敏度
    public static final int SENSITIVITY = 20;

    // 退出计时器
    private long exitTime = 0;
    // 正在摇晃判断
    private boolean isShaking = false;

    // 配置
    public SharedPreferences sharedPreferences;
    // 传感器
    public SensorManager sensorManager;
    // 振动
    public Vibrator vibrator;
    // 音效池
    public SoundPool soundPool;
    // 百度语音合成引擎
    public MySynthesizer speechSynthesizer;

    // 顶部布局
    @BindView(R.id.topLayout)
    public ConstraintLayout topLayout;
    @BindView(R.id.listText)
    public TextView listText;
    @BindView(R.id.voiceText)
    public TextView voiceText;
    @BindView(R.id.topDivider)
    public View topDivider;
    // 中部按钮
    @BindView(R.id.outputText)
    public TextView outputText;
    @BindView(R.id.imageButton)
    public ImageButton imageButton;
    // 底部布局
    @BindView(R.id.bottomLayout)
    public ConstraintLayout bottomLayout;
    @BindView(R.id.stateImage)
    public ImageView stateImage;
    @BindView(R.id.bottomDivider)
    public View bottomDivider;

    // 动画
    @BindAnim(R.anim.anim_go_up)
    public Animation goUpAnim;
    @BindAnim(R.anim.anim_go_down)
    public Animation goDownAnim;
    @BindAnim(R.anim.anim_back_up)
    public Animation backUpAnim;
    @BindAnim(R.anim.anim_back_down)
    public Animation backDownAnim;

    // 图片
    @BindDrawable(R.drawable.ic_circle_grey_24dp)
    public Drawable greyImage;
    @BindDrawable(R.drawable.ic_circle_green_24dp)
    public Drawable greenImage;
    @BindDrawable(R.drawable.ic_circle_yellow_24dp)
    public Drawable yellowImage;
    @BindDrawable(R.drawable.ic_circle_red_24dp)
    public Drawable redImage;

    // 音效
    public int openAudio;
    public int closeAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        // 初始化ButterKnife
        ButterKnife.bind(this);
        // 初始化配置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_voice, true);
        ListActivity.loadPreferences(sharedPreferences);
        VoiceActivity.reloadPreferences(sharedPreferences);
        // 初始化控件
        listText.setText(String.format(getString(R.string.list_number),
                String.valueOf(ListActivity.elements.size())));
        voiceText.setText(String.format(getString(R.string.voice_text),
                VoiceActivity.text));
        stateImage.setImageDrawable(greenImage); // TODO 语音合成引擎状态显示
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 申请权限
        initPermission();
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
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // 初始化音效
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        openAudio = soundPool.load(this, R.raw.open,1);
        closeAudio = soundPool.load(this, R.raw.close, 1);
        // 初始化语音合成引擎
        speechSynthesizer = new MySynthesizer(this);
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                Map<String, String> params = new HashMap<>();
                params.put(SpeechSynthesizer.PARAM_MIX_MODE, VoiceActivity.mixMode);
                params.put(SpeechSynthesizer.PARAM_SPEAKER, VoiceActivity.speaker);
                params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(VoiceActivity.volume));
                params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(VoiceActivity.speed));
                params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(VoiceActivity.pitch));
                MySynthesizer.Config synthesizerConfig = new MySynthesizer.Config("11443617",
                        "iNmAH7IzeHm2HT6eNmYIu5OF",
                        "yv3LkAYkrc6Gw32IG1UB12WBlwY5AheX",
                        TtsMode.MIX, params, MainActivity.this);
                speechSynthesizer.init(synthesizerConfig);
            }
        };
        thread.start();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // 停止动画
        if(isShaking)
        {
            stop();
        }
        // 注销传感器
        if (sensorManager != null)
        {
            // 注销传感器监听器
            // 否则界面退出后摇一摇依旧生效
            sensorManager.unregisterListener(this);
        }
        // 停止语音合成引擎
        if (speechSynthesizer != null)
        {
            speechSynthesizer.stop();
            // speechSynthesizer.release(); // FIXME 有时会在start前被调用
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        int type = sensorEvent.sensor.getType();
        // 加速度传感器
        if (type == Sensor.TYPE_ACCELEROMETER && !isShaking)
        {
            // 获取三个方向值
            for (float value : sensorEvent.values)
            {
                if (Math.abs(value) > SENSITIVITY)
                {
                    start();
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
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {}

    @Override
    public void onSynthesizeFinish(String s) {}

    @Override
    public void onSpeechStart(String s)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                stateImage.setImageDrawable(yellowImage);
            }
        });
    }

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
                stateImage.setImageDrawable(greenImage);
                if(isShaking)
                {
                    stop();
                }
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
                stateImage.setImageDrawable(redImage);
                Utils.toast(MainActivity.this,
                        "合成语音播放错误: " + speechError.toString(), Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission()
    {
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String permission : PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                toApplyList.add(permission);
            }
        }
        if (!toApplyList.isEmpty())
        {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(new String[]{}), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {}

    @Override
    public void onBackPressed()
    {
        if (isShaking)
        {
            stop();
        }
        else if ((currentTimeMillis() - exitTime) > 2000)
        {
            Utils.toast(this, R.string.exit);
            exitTime = currentTimeMillis();
        }
        else
        {
            super.onBackPressed();
            killProcess(myPid());
        }
    }

    public void onClicked(View view)
    {
        switch(view.getId())
        {
            case R.id.imageButton:
            {
                if (!isShaking)
                    start();
                break;
            }
            case R.id.listButton:
            {
                startActivity(new Intent(this, ListActivity.class));
                break;
            }
            case R.id.voiceButton:
            {
                startActivity(new Intent(this, VoiceActivity.class));
                break;
            }
            case R.id.settingButton:
            {
                startActivity(new Intent(this, CodeActivity.class));
                break;
            }
            case R.id.helpButton:
            {
                startActivity(new Intent(this, AboutActivity.class));
                break;
            }
        }
    }

    public void start()
    {
        int size = ListActivity.elements.size();
        if(size > 0)
        {
            isShaking = true;
            // 生成文本
            String element = ListActivity.elements.get(generate(size));
            String text = String.format(VoiceActivity.text, element);
            // 设置中心文本
            outputText.setText(text);
            // 隐藏中心按钮
            imageButton.setVisibility(View.GONE);
            // 显示这两条线
            topDivider.setVisibility(View.VISIBLE);
            bottomDivider.setVisibility(View.VISIBLE);
            // 播放动画
            topLayout.startAnimation(goUpAnim);
            bottomLayout.startAnimation(goDownAnim);
            // 播放提示音
            if(VoiceActivity.soundEnabled)
                soundPool.play(openAudio, 1, 1, 0, 0, 1);
            // 发出振动
            if(VoiceActivity.vibratorEnabled)
                vibrator.vibrate(VoiceActivity.vibrateTime);
            // 播放语音
            if(VoiceActivity.voiceEnabled)
                speechSynthesizer.speak(text, "");
        }
        else
        {
            Utils.toast(this, R.string.list_blank);
        }
    }

    public void stop()
    {
        isShaking = false;
        // 停止语音
        speechSynthesizer.stop();
        // 播放动画
        topLayout.startAnimation(backUpAnim);
        bottomLayout.startAnimation(backDownAnim);
        // 播放提示音
        if(VoiceActivity.soundEnabled)
            soundPool.play(closeAudio, 1, 1, 0, 0, 1);
        // 延时执行
        imageButton.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // 显示中心按钮
                imageButton.setVisibility(View.VISIBLE);
                // 隐藏这两条线
                topDivider.setVisibility(View.GONE);
                bottomDivider.setVisibility(View.GONE);
            }
        }, 300);
    }

    public Random rand = new Random();
    public int generate(int range)
    {
        return rand.nextInt(range);
    }
}
