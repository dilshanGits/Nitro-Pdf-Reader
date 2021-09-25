package com.example.vivek.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.example.vivek.fragments.SettingsFragment;

import java.util.Locale;

public class LocaleUtils {

    public static void setUpLanguage(Context context) {
        String strLanguage = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en");
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = new Locale(strLanguage);
        Locale.setDefault(locale);
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }


    public static String keyToLanguage(Context r2, String r3) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(r2);
        SharedPreferences.Editor edit = sharedPreferences.edit();
//        edit.putBoolean(SettingsFragment.KEY_PREFS_LANGUAGE, true);
        edit.putString(SettingsFragment.KEY_PREFS_LANGUAGE, r3);
        edit.apply();


        return null;
    }
}
