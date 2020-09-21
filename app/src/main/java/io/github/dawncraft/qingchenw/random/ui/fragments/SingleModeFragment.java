package io.github.dawncraft.qingchenw.random.ui.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindAnim;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.tts.MySynthesizer;
import io.github.dawncraft.qingchenw.random.tts.OfflineResource;
import io.github.dawncraft.qingchenw.random.ui.AboutActivity;
import io.github.dawncraft.qingchenw.random.ui.CodeActivity;
import io.github.dawncraft.qingchenw.random.ui.FlashlightActivity;
import io.github.dawncraft.qingchenw.random.ui.HelpActivity;
import io.github.dawncraft.qingchenw.random.ui.ListActivity;
import io.github.dawncraft.qingchenw.random.ui.MainActivity;
import io.github.dawncraft.qingchenw.random.ui.VoiceActivity;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;
import io.github.dawncraft.qingchenw.random.utils.RandomEngine;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;
import io.github.dawncraft.qingchenw.random.utils.WebUtils;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;
import static io.github.dawncraft.qingchenw.random.ui.ListActivity.DELIMITER;
import static java.lang.System.currentTimeMillis;

public class SingleModeFragment extends BaseModeFragment
{
    // 文本
    public static String showText;


    // 音效
    public static boolean soundEnabled;
    // 震动
    public static boolean vibratorEnabled;
    public static long vibrateTime;
    // 灵敏度
    public static int sensitivity;
    // 随机算法
    public static boolean codeEnabled;
    public static String code;




    // 正在摇晃判断
    private boolean isShaking = false;


    // 传感器
    public SensorManager sensorManager;
    // 振动
    public Vibrator vibrator;
    // 音效池
    public SoundPool soundPool;


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
    @BindDrawable(R.drawable.ic_circle_red_24dp)
    public Drawable redImage;

    // 音效
    public int openAudio;
    public int closeAudio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // 初始化ButterKnife
        ButterKnife.bind(this.requireActivity());
        // 初始化布局
        View root = inflater.inflate(R.layout.fragment_single, container);
        return root;
    }

    @Override
    public void onStart()
    {
        super.onStart();


        // 初始化控件
        listText.setText(String.format(getString(R.string.list_number), String.valueOf(elements.size())));
        voiceText.setText(String.format(getString(R.string.voice_text), showText));
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
            openAudio = soundPool.load(this, R.raw.open,1);
            closeAudio = soundPool.load(this, R.raw.close, 1);
        }
        // 初始化语音合成引擎
        if (voiceEnabled)
        {
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
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                stateImage.setImageDrawable(speechSynthesizer.isInited() ? greenImage : redImage);
                            }
                        });
                    }
                }
            }.start();
        }
        // 初始化随机数生成引擎
        randomEngine = new RandomEngine<>();
        if (!elements.isEmpty()) randomEngine.setElementList(elements);
        if (codeEnabled)
        {
            randomEngine.initJSEngine();
            randomEngine.setScript(code);
        }
        // 检查更新
        if (updateEnabled) checkUpdate();
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
            stateImage.setImageDrawable(greyImage);
        }
        // 释放随机数生成引擎
        if (randomEngine != null)
        {
            randomEngine.release();
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
                if (Math.abs(value) > sensitivity)
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
                if (isShaking)
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
                SystemUtils.toast(MainActivity.this,
                        String.format(getString(R.string.synthesize_error), speechError.toString()),
                        Toast.LENGTH_LONG);
            }
        });
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
                            SystemUtils.toast(this, R.string.no_storage_permission, Toast.LENGTH_LONG);
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
        if (toSplit != null && !toSplit.isEmpty())
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
        String speakerID = sharedPreferences.getString("voice_speaker", "0");
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
        showText = sharedPreferences.getString("voice_text", getString(R.string.voice_text_default));
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(sharedPreferences.getInt("voice_volume", 5)));
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(sharedPreferences.getInt("voice_speed", 5)));
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(sharedPreferences.getInt("voice_pitch", 5)));
        synthesizerConfig.setParams(params);
        // 音效设置
        soundEnabled = sharedPreferences.getBoolean("sound_switch", true);
        // 震动设置
        vibratorEnabled = sharedPreferences.getBoolean("vibrator_switch", true);
        vibrateTime = Integer.parseInt(sharedPreferences.getString("vibrator_time", "300"));
        // 摇晃设置
        sensitivity = Integer.parseInt(sharedPreferences.getString("sensitivity", "20"));
        // 随机算法
        codeEnabled =  sharedPreferences.getBoolean("custom_code", false);
        code = CodeActivity.formatCode(FileUtils.readFile(RES_PATH + "/" + CodeActivity.FILE_NAME));
        // 更新设置
        updateEnabled = sharedPreferences.getBoolean("update", true);
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
        if (hasNetwork)
        {
            haveUpdate = false;
            new Thread()
            {
                @Override
                public void run()
                {
                    super.run();
                    try
                    {
                        if (update == null)
                        {
                            update = new JSONObject(WebUtils.readUrl(UPDATE_URL));
                        }
                        haveUpdate = packageInfo != null && update.getInt("versionCode") > packageInfo.versionCode;
                    } catch (JSONException e) {
                        update = null;
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (haveUpdate) displayUpdate();
                            else SystemUtils.toast(MainActivity.this, R.string.no_update);
                        }
                    });
                }
            }.start();
        }
        else
        {
            SystemUtils.toast(this, R.string.cannot_update);
        }
    }

    public void displayUpdate()
    {
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final TextView textView = new TextView(MainActivity.this);
        try
        {
            textView.setText(String.format(getString(R.string.update_content),
                    update.getString("versionName"), update.getString("description")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        linearLayout.addView(textView);
        final CheckBox checkBox = new CheckBox(MainActivity.this);
        checkBox.setText(R.string.do_not_remind);
        checkBox.setChecked(!updateEnabled);
        linearLayout.addView(checkBox);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.have_update).setCancelable(false).setView(linearLayout);
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                downLoadUpdate();
            }
        });
        builder.setNegativeButton(R.string.update_later, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (checkBox.isChecked())
                {
                    updateEnabled = false;
                    SharedPreferences.Editor editor = MainActivity.this.sharedPreferences.edit();
                    editor.putBoolean("update", updateEnabled);
                    editor.apply();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked) dialog.getButton(-2).setText(R.string.do_not_remind);
                else dialog.getButton(-2).setText(R.string.update_later);
            }
        });
        dialog.show();
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
                    File file = new File(RES_PATH, "update.apk");
                    if (!file.exists() || !Objects.equals(FileUtils.fileToMD5(file), update.getString("md5")))
                    {
                        // 下载APK
                        WebUtils.downloadFile(apkUrl, file, dialog);
                    }
                    // 安装APK
                    SystemUtils.installApk(MainActivity.this, file);
                    // 关闭对话框
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void start()
    {
        if (!voiceEnabled || speechSynthesizer.isInited())
        {
            if (randomEngine.hasElement())
            {
                isShaking = true;
                // 生成文本
                String element = "";
                try
                {
                    element = randomEngine.generate();
                }
                catch (RandomEngine.InvalidCodeException e)
                {
                    e.printStackTrace();
                    SystemUtils.toast(this, R.string.code_invalid1);
                    element = elements.get(e.getResult());
                }
                String text = String.format(showText, element);
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
                if (soundEnabled)
                    soundPool.play(openAudio, 1, 1, 0, 0, 1);
                // 发出振动
                if (vibratorEnabled)
                    vibrator.vibrate(vibrateTime);
                // 播放语音
                if (voiceEnabled)
                    speechSynthesizer.speak(text, "");
            }
            else
            {
                SystemUtils.toast(this, R.string.list_blank);
            }
        }
        else
        {
            SystemUtils.toast(this, R.string.synthesize_initializing);
        }
    }

    public void stop()
    {
        isShaking = false;
        // 停止语音
        if (voiceEnabled)
            speechSynthesizer.stop();
        // 播放动画
        topLayout.startAnimation(backUpAnim);
        bottomLayout.startAnimation(backDownAnim);
        // 播放提示音
        if (soundEnabled)
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

}
