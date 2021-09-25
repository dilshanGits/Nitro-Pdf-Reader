package com.example.vivek.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.vivek.R;
import com.example.vivek.utils.LocaleUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_PREFS_LANGUAGE = "prefs_language";
    public static final String KEY_PREFS_REMEMBER_LAST_PAGE = "prefs_remember_last_page";
    public static final String KEY_PREFS_STAY_AWAKE = "prefs_stay_awake";
    public Context context;


    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.preferences);
        this.context = getContext();
        bindLanguagePreferenceSummaryToValue(findPreference(KEY_PREFS_LANGUAGE));
    }

    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }

    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }


    public void bindLanguagePreferenceSummaryToValue(Preference preference) {
        preference.setSummary((CharSequence) LocaleUtils.keyToLanguage(this.context, getPreferenceScreen().getSharedPreferences().getString(preference.getKey(), "en")));

    }

    OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            if (((str.hashCode() == -976476153 && str.equals(SettingsFragment.KEY_PREFS_LANGUAGE)) ? (char) 0 : 65535) == 0) {
                SettingsFragment settingsFragment = SettingsFragment.this;
                settingsFragment.bindLanguagePreferenceSummaryToValue(settingsFragment.findPreference(SettingsFragment.KEY_PREFS_LANGUAGE));
                LocaleUtils.setUpLanguage(SettingsFragment.this.context);
                ((Activity) SettingsFragment.this.context).recreate();
            }
        }
    };



}
