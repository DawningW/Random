package io.github.dawncraft.qingchenw.random;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindAnim;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;
import static io.github.dawncraft.qingchenw.random.ListActivity.DELIMITER;
import static java.lang.System.currentTimeMillis;

/**
 * 一个摇号的小应用
 * 感谢changer0的教程https://www.jianshu.com/p/bc5298651b30
 * <p>
 * Created by QingChenW on 2018/6/25
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, SpeechSynthesizerListener
{
    // 更新地址
    public static final String UPDATE_URL = "https://raw.githubusercontent.com/DawningW/Random/master/update.json";
    // 语音合成引擎资源文件
    public static final String VOICE_RES_PATH = "BaiduTTS";
    // JavaScript网页模版
    // public static final String BASE_HTML = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><script>%s</script></head></html>";
    // 权限
    public static final int REQUEST_CODE = 0;
    public static final String PERMISSIONS[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    // 灵敏度
    public static final int SENSITIVITY = 20;

    // 元素列表
    public static List<String> elements = new ArrayList<>();
    // 文本
    public static String showtext;
    // 语音合成
    public static boolean voiceEnabled;
    public static MySynthesizer.Config synthesizerConfig = new MySynthesizer.Config(
            "11443617",
            "iNmAH7IzeHm2HT6eNmYIu5OF",
            "yv3LkAYkrc6Gw32IG1UB12WBlwY5AheX");
    public static OfflineResource offlineResource;

    // 音效
    public static boolean soundEnabled;
    // 震动
    public static boolean vibratorEnabled;
    public static long vibrateTime;
    // 随机算法
    public static boolean codeEnabled;
    public static String code;
    // 更新
    public static boolean updateEnabled;
    public static JSONObject update;
    public static boolean haveUpdate;

    // 退出计时器
    private long exitTime = 0;
    // 正在摇晃判断
    private boolean isShaking = false;

    // 应用信息
    static public PackageInfo packageInfo;
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
    // JavaScript引擎
    // public WebView webView = new WebView(this);

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
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // 申请权限
        initPermission();
        // 初始化配置
        packageInfo = Utils.getPackageInfo(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_voice, true);
        loadPreferences();
        // 初始化控件
        listText.setText(String.format(getString(R.string.list_number), String.valueOf(elements.size())));
        voiceText.setText(String.format(getString(R.string.voice_text), showtext));
        if(voiceEnabled) stateImage.setImageDrawable(greenImage);
        else stateImage.setImageDrawable(greyImage);
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
        // 获取音效
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        openAudio = soundPool.load(this, R.raw.open,1);
        closeAudio = soundPool.load(this, R.raw.close, 1);
        // 初始化语音合成引擎
        speechSynthesizer = new MySynthesizer(this);
        synthesizerConfig.setListener(MainActivity.this);
        new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                if (speechSynthesizer != null)
                {
                    speechSynthesizer.init(synthesizerConfig);
                }
            }
        }.start();
        // 检查更新
        new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                try
                {
                    update = new JSONObject(Utils.readUrl(UPDATE_URL));
                    haveUpdate = packageInfo != null && update.getInt("versionCode") > packageInfo.versionCode;
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (updateEnabled) checkUpdate();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
        }
        // 停止语音合成引擎
        if (speechSynthesizer != null)
        {
            speechSynthesizer.stop();
            speechSynthesizer.release();
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
            ActivityCompat.requestPermissions(this, toApplyList.toArray(new String[]{}), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE)
        {
            for (int i = 0; i < permissions.length; i++)
            {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]))
                        {
                            Utils.toast(this, R.string.no_storage_permission, Toast.LENGTH_LONG);
                            ActivityCompat.requestPermissions(this, new String[] {permissions[i]}, REQUEST_CODE);
                        }
                    }
                }
            }
        }
    }

    public void loadPreferences()
    {
        // 集合设置
        elements.clear();
        String toSplit = sharedPreferences.getString("elements", "");
        if (!toSplit.equals(""))
        {
            Collections.addAll(elements, toSplit.split(String.valueOf(DELIMITER)));
        }
        // 语音设置
        voiceEnabled = sharedPreferences.getBoolean("voice_switch", true);
        synthesizerConfig.setTtsMode(sharedPreferences.getBoolean("voice_mode", false) ?
                TtsMode.MIX : TtsMode.ONLINE);
        Map<String, String> params = new HashMap<>();
        boolean network = sharedPreferences.getBoolean("voice_network", false);
        boolean time = sharedPreferences.getBoolean("voice_overtime", false);
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, network ?
                (time ? SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK : SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE) :
                (time ? SpeechSynthesizer.MIX_MODE_DEFAULT : SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI));
        String speakerType = sharedPreferences.getString("voice_speaker", "0");
        params.put(SpeechSynthesizer.PARAM_SPEAKER, speakerType);
        offlineResource = new OfflineResource(this, speakerType);
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        showtext = sharedPreferences.getString("voice_text", getString(R.string.voice_text_default));
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(sharedPreferences.getInt("voice_volume", 5)));
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(sharedPreferences.getInt("voice_speed", 5)));
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(sharedPreferences.getInt("voice_pitch", 5)));
        synthesizerConfig.setParams(params);
        // 音效设置
        soundEnabled = sharedPreferences.getBoolean("sound_switch", true);
        // 震动设置
        vibratorEnabled = sharedPreferences.getBoolean("vibrator_switch", true);
        vibrateTime = sharedPreferences.getInt("vibrator_time", 300);
        // 随机算法
        codeEnabled =  sharedPreferences.getBoolean("custom_code", false);
        code = String.format(CodeActivity.BASE_CODE, Utils.readFile(VOICE_RES_PATH + "/" + CodeActivity.FILE_NAME));
        // 更新设置
        updateEnabled = sharedPreferences.getBoolean("update", true);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_code:
            {
                startActivity(new Intent(this, CodeActivity.class));
                return true;
            }
            case R.id.action_update:
            {
                checkUpdate();
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
        }
        return super.onOptionsItemSelected(item);
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
        }
    }

    public void checkUpdate()
    {
        if (haveUpdate)
        {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final TextView textView = new TextView(this);
            try
            {
                textView.setText(String.format("检查到新版本: %s\n更新内容: \n%s",
                        update.getString("versionName"), update.getString("description")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            linearLayout.addView(textView);
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText("如果取消更新,则以后不再提醒");
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    updateEnabled = !isChecked;
                    SharedPreferences.Editor editor = MainActivity.this.sharedPreferences.edit();
                    editor.putBoolean("update", updateEnabled);
                    editor.apply();
                }
            });
            linearLayout.addView(checkBox);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.have_update).setCancelable(false).setView(linearLayout);
            builder.setPositiveButton("更新", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    downLoadUpdate();
                }
            });
            builder.setNegativeButton("下次再说", null);
            AlertDialog dialog = builder.create();
//            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            dialog.show();
        }
        else
        {
            Utils.toast(this, R.string.no_update);
        }
    }

    public void downLoadUpdate()
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.download);
        dialog.setMessage(getString(R.string.download_update));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // 更新地址
                    String apkUrl = update.getString("updateUrl");
                    // 下载APK
                    File file = Utils.downloadFile(apkUrl, VOICE_RES_PATH, dialog);
                    // 安装APK
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    startActivity(intent);
                    // 关闭对话框
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }}.start();
    }

    public void start()
    {
        int size = elements.size();
        if(size > 0)
        {
            isShaking = true;
            // 生成文本
            String element = elements.get(generate(size));
            String text = String.format(showtext, element);
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
            if(soundEnabled)
                soundPool.play(openAudio, 1, 1, 0, 0, 1);
            // 发出振动
            if(vibratorEnabled)
                vibrator.vibrate(vibrateTime);
            // 播放语音
            if(voiceEnabled)
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
        if(soundEnabled)
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
        int result = rand.nextInt(range);
        if (codeEnabled)
        {
            // TODO 实现自定义JavaScript伪随机算法
        }
        return result;
    }
}
