package io.github.dawncraft.qingchenw.random;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.baidu.tts.client.SpeechSynthesizer;

import java.util.HashMap;

public class VoiceActivity extends PreferenceActivity
{
    public static HashMap<String, String> params = new HashMap<>();
    public static String text = "";
    static
    {
        // 设置在线发声音人： 0 普通女声(默认) 1 普通男声 2 特别男声 3 情感男声 4 情感儿童声
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
    }

    public SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_voice);
        if (getActionBar() != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // 初始化配置
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadConfig();
    }

    @Override
    protected void onDestroy()
    {
        loadConfig();
        super.onDestroy();
    }

    private void loadConfig()
    {
        loadConfig(preferences);
    }

    private void saveConfig()
    {
        saveConfig(preferences);
    }

    public static void loadConfig(SharedPreferences preferences)
    {
        text = preferences.getString("voice_text", "请 %s 号同学回答问题");
    }

    public static void saveConfig(SharedPreferences preferences)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("voice_text", text);
        editor.apply();
    }
}
