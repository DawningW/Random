package io.github.dawncraft.qingchenw.random;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

public class VoiceActivity extends AppCompatActivity
{
    public static boolean voiceEnabled;
    public static TtsMode ttsMode;
    public static String mixMode;
    public static String speaker;
    public static String text;
    public static int volume;
    public static int speed;
    public static int pitch;
    public static boolean soundEnabled;
    public static boolean vibratorEnabled;
    public static long vibrateTime;

    // 配置
    public SharedPreferences sharedPreferences;

    private static final Preference.OnPreferenceChangeListener preferenceBindingListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();
            if (preference instanceof ListPreference)
            {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化配置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        reloadPreferences(sharedPreferences);
    }

    @Override
    protected void onDestroy()
    {
        // 重载配置
        reloadPreferences(sharedPreferences);
        super.onDestroy();
    }

    public static void reloadPreferences(SharedPreferences preferences)
    {
        voiceEnabled = preferences.getBoolean("voice_switch", true);
        String tts = preferences.getString("voice_mode", "0");
        switch (tts)
        {
            default:
            case "0": ttsMode = TtsMode.MIX; break;
            case "1": ttsMode = TtsMode.ONLINE; break;
        }
        String net = preferences.getString("voice_network", "0");
        String time = preferences.getString("voice_overtime", "0");
        if (net.equals("0"))
        {
            if (time.equals("0"))
            {
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI;
            }
            else
            {
                mixMode = SpeechSynthesizer.MIX_MODE_DEFAULT;
            }
        }
        else
        {
            if (time.equals("0"))
            {
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE;
            }
            else
            {
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK;
            }
        }
        speaker = preferences.getString("voice_speaker", "0");
        text = preferences.getString("voice_text", "请 %s 号同学回答问题");
        volume = preferences.getInt("voice_volume", 5);
        speed = preferences.getInt("voice_speed", 5);
        pitch = preferences.getInt("voice_pitch", 5);
        soundEnabled = preferences.getBoolean("sound_switch", true);
        vibratorEnabled = preferences.getBoolean("vibrator_switch", true);
        vibrateTime = preferences.getInt("vibrator_time", 300);
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle paramBundle)
        {
            super.onCreate(paramBundle);
            addPreferencesFromResource(R.xml.pref_voice);
            findPreference("voice_mode").setOnPreferenceChangeListener(preferenceBindingListener);
            findPreference("voice_network").setOnPreferenceChangeListener(preferenceBindingListener);
            findPreference("voice_overtime").setOnPreferenceChangeListener(preferenceBindingListener);
            findPreference("voice_speaker").setOnPreferenceChangeListener(preferenceBindingListener);
            findPreference("voice_text").setOnPreferenceChangeListener(preferenceBindingListener);
            findPreference("vibrator_time").setOnPreferenceChangeListener(preferenceBindingListener);
        }
    }
}
