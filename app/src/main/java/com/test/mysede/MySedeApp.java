package com.test.mysede;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class MySedeApp extends Application {
    private static final String PREFS_NAME = "config_prefs";
    private static final String PREF_MODO_OSCURO = "modoOscuro";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean modoOscuroActivo = sharedPreferences.getBoolean(PREF_MODO_OSCURO, false);
        int modoDeseado = modoOscuroActivo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(modoDeseado);
    }
}
