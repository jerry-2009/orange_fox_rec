package com.fordownloads.orangefox;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.service.DownloadService;

public class App extends Application {
    private DownloadService downloadSrv;
    public boolean isDownloadSrvRunning() {
        return downloadSrv != null;
    }
    public void setDownloadSrv(DownloadService downloadSrv) {
        this.downloadSrv = downloadSrv;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(pref.THEME_SYSTEM, true)) {
            AppCompatDelegate.setDefaultNightMode(
                    prefs.getBoolean(pref.THEME_DARK, false) ?
                            AppCompatDelegate.MODE_NIGHT_YES :
                            AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
