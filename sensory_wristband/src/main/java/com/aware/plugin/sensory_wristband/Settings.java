package com.aware.plugin.sensory_wristband;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Plugin settings in XML @xml/preferences
    public static final String STATUS_PLUGIN_SENSORY_WRISTBAND = "status_plugin_sensory_wristband";
    public static final String FREQUENCY_HEART_RATE = "frequency_heart_rate";
    public static final String FREQUENCY_BASIC_DATA = "frequency_basic_data";

    //Plugin settings UI elements
    private static CheckBoxPreference status;
    private static ListPreference heartRateFrequencyList;
    private static ListPreference basicDataFrequencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_SENSORY_WRISTBAND);
        if( Aware.getSetting(this, STATUS_PLUGIN_SENSORY_WRISTBAND).length() == 0 ) {
            Aware.setSetting( this, STATUS_PLUGIN_SENSORY_WRISTBAND, true ); //by default, the setting is true on install
        }
        status.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_SENSORY_WRISTBAND).equals("true"));

        heartRateFrequencyList = (ListPreference) findPreference(FREQUENCY_HEART_RATE);
        if (Aware.getSetting(this, FREQUENCY_HEART_RATE).length() == 0)
            Aware.setSetting(this, FREQUENCY_HEART_RATE, "30000");
        heartRateFrequencyList.setSummary(Aware.getSetting(this, FREQUENCY_HEART_RATE));

        basicDataFrequencyList = (ListPreference) findPreference(FREQUENCY_BASIC_DATA);
        if (Aware.getSetting(this, FREQUENCY_BASIC_DATA).length() == 0)
            Aware.setSetting(this, FREQUENCY_BASIC_DATA, "10000");
        basicDataFrequencyList.setSummary(Aware.getSetting(this, FREQUENCY_BASIC_DATA));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference.getKey().equals(STATUS_PLUGIN_SENSORY_WRISTBAND) ) {
            Aware.setSetting(this, key, sharedPreferences.getBoolean(key, false));
            status.setChecked(sharedPreferences.getBoolean(key, false));
        }

        if (Aware.getSetting(this, STATUS_PLUGIN_SENSORY_WRISTBAND).equals("true")) {
            Aware.startPlugin(getApplicationContext(), "com.aware.plugin.sensory_wristband");
        } else {
            Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.sensory_wristband");
        }

        if (preference.getKey().equals(FREQUENCY_HEART_RATE)){
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "30000"));
            preference.setSummary(Aware.getSetting(this, FREQUENCY_HEART_RATE));
        }

        if (preference.getKey().equals(FREQUENCY_BASIC_DATA)){
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "10000"));
            preference.setSummary(Aware.getSetting(this, FREQUENCY_BASIC_DATA));
        }
    }
}
