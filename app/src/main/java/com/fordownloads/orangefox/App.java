package com.fordownloads.orangefox;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.service.DownloadService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class App extends Application {
    private DownloadService downloadSrv;
    private BottomSheetDialog dismissDialog;
    public boolean isDownloadSrvRunning() { return downloadSrv != null; }
    public void setDownloadSrv(DownloadService downloadSrv) { this.downloadSrv = downloadSrv; }
    public void setDialogToDismiss(BottomSheetDialog dialog) { this.dismissDialog = dialog; }
    public void dismissDialog() { if (this.dismissDialog != null) this.dismissDialog.dismiss(); }

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
