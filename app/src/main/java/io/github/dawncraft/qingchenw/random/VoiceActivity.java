package io.github.dawncraft.qingchenw.random;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class VoiceActivity extends AppCompatPreferenceActivity
{
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
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.pref_voice);
        bindPreferenceSummaryToStrValue(findPreference("voice_speaker"));
        bindPreferenceSummaryToStrValue(findPreference("voice_text"));
        bindPreferenceSummaryToIntValue(findPreference("vibrator_time"));
        bindPreferenceSummaryToIntValue(findPreference("sensitivity"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // 返回键的id
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void bindPreferenceSummaryToStrValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(preferenceBindingListener);
        // Trigger the listener immediately with the preference's current value.
        preferenceBindingListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    public static void bindPreferenceSummaryToIntValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(preferenceBindingListener);
        // Trigger the listener immediately with the preference's current value.
        preferenceBindingListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), 0));
    }
}
