package io.github.dawncraft.qingchenw.random;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.util.HashMap;
import java.util.Random;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 一个摇号的小应用
 * 感谢changer0的教程https://www.jianshu.com/p/bc5298651b30
 * <p>
 * Created by QingChenW on 2018/6/25
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, SpeechSynthesizerListener
{
    private static HashMap<String, String> params = new HashMap<>();
    static
    {
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
    }
    public static int range = 20;
    private int number = 50;
    private String speech = "请 %s 号同学回答问题";

    private long exitTime = 0;
    private boolean isShaking = false;

    public SensorManager sensorManager;
    public Vibrator vibrator;
    public SoundPool soundPool;
    public MySynthesizer.Config synthesizerConfig;
    public MySynthesizer speechSynthesizer;

    @BindView(R.id.topLayout)
    public LinearLayout topLayout;
    @BindView(R.id.number)
    public TextView numberText;
    @BindView(R.id.topDivider)
    public View topDivider;
    @BindView(R.id.imageButton)
    public ImageButton imageButton;
    @BindView(R.id.output)
    public TextView outputText;
    @BindView(R.id.bottomLayout)
    public LinearLayout bottomLayout;
    @BindView(R.id.bottomDivider)
    public View bottomDivider;

    @BindAnim(R.anim.anim_go_up)
    public Animation goUpAnim;
    @BindAnim(R.anim.anim_go_down)
    public Animation goDownAnim;
    @BindAnim(R.anim.anim_back_up)
    public Animation backUpAnim;
    @BindAnim(R.anim.anim_back_down)
    public Animation backDownAnim;

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
        // 初始化Views
        numberText.setText(String.valueOf(number));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
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
        openAudio = soundPool.load(this, R.raw.open, 1);
        closeAudio = soundPool.load(this, R.raw.close, 1);
        // 初始化语音合成引擎
        synthesizerConfig = new MySynthesizer.Config("11443617",
                "iNmAH7IzeHm2HT6eNmYIu5OF",
                "yv3LkAYkrc6Gw32IG1UB12WBlwY5AheX",
                TtsMode.MIX, params, MainActivity.this);
        speechSynthesizer = new MySynthesizer(this);
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                speechSynthesizer.init(synthesizerConfig);
                // 测试
                speechSynthesizer.speak("语音合成引擎已成功加载");
            }
        };
        thread.start();
    }

    @Override
    protected void onPause()
    {
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
            speechSynthesizer.release();
        }
        super.onPause();
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
                if (Math.abs(value) > range)
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
    public void onSpeechStart(String s) {}

    @Override
    public void onSpeechProgressChanged(String s, int i) {}

    @Override
    public void onSpeechFinish(String s)
    {
        if(s.equals("number"))
        {
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    stop();
                }
            });
        }
    }

    @Override
    public void onError(String s, SpeechError speechError)
    {
        toast("合成语音播放错误: " + speechError.toString());
    }

    @Override
    public void onBackPressed()
    {
        if ((System.currentTimeMillis() - exitTime) > 2000)
        {
            toast(R.string.exit);
            exitTime = System.currentTimeMillis();
        }
        else
        {
            super.onBackPressed();
            android.os.Process.killProcess(android.os.Process.myPid());
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
            case R.id.numberButton:
            {
                final EditText editText = new EditText(this);
                editText.setText(String.valueOf(number));
                editText.setHint("请输入大于0的整数");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setSelection(editText.getText().length());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("输入人数").setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int num = Integer.valueOf(editText.getText().toString());
                        if(num > 0 && num < Integer.MAX_VALUE)
                        {
                            number = num;
                            numberText.setText(String.valueOf(num));
                        }
                        else
                        {
                            toast("无效参数, 范围为: " + 0 + " ~ " + Integer.MAX_VALUE);
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                dialog.show();
                break;
            }
        }
    }

    public void start()
    {
        isShaking = true;
        // 生成文本
        String text = String.format(speech, String.valueOf(generate(number)));
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
        // 发出振动
        vibrator.vibrate(300);
        // 播放提示音
        soundPool.play(openAudio, 1, 1, 0, 0, 1);
        // 播放语音
        speechSynthesizer.speak(text, "number");
    }

    public void stop()
    {
        // 播放动画
        topLayout.startAnimation(backUpAnim);
        bottomLayout.startAnimation(backDownAnim);
        // 播放提示音
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
                isShaking = false;
            }
        }, 300);
    }

    public Random rand = new Random();
    public int generate(int range)
    {
        return rand.nextInt(range) + 1;
    }

    public void toast(@StringRes int msgId)
    {
        toast(getString(msgId));
    }

    public void toast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
