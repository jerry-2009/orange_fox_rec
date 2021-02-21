package com.fordownloads.orangefox.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.fragments.InstallFragment;
import com.fordownloads.orangefox.fragments.LogsFragment;
import com.fordownloads.orangefox.fragments.ScriptsFragment;
import com.fordownloads.orangefox.fragments.BackupsFragment;
import com.fordownloads.orangefox.consts;
import com.topjohnwu.superuser.Shell;

public class MainActivity extends AppCompatActivity {
    LogsFragment logs;
    ScriptsFragment scripts;
    InstallFragment install;

    PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .setReadTimeout(30_000)
            .setConnectTimeout(30_000)
            .build();

    static {
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
        try {
            StrictMode.class.getMethod("disableDeathOnFileUriExposure").invoke(null);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PRDownloader.initialize(getApplicationContext(), config);
        createNotificationChannel();
        prepareBottomNav();

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.fox_status_solid_bg));
        getWindow().setBackgroundDrawableResource(R.color.fox_background);
    }

    private void createNotificationChannel() {
        NotificationManager notifyMan = getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(consts.CHANNEL_UPDATE, getString(R.string.notif_ch_update), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(getString(R.string.notif_ch_update_desc));
        notifyMan.createNotificationChannel(channel);

        channel = new NotificationChannel(consts.CHANNEL_DOWNLOAD, getString(R.string.notif_ch_download), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notif_ch_download_desc));
        notifyMan.createNotificationChannel(channel);

        channel = new NotificationChannel(consts.CHANNEL_DOWNLOAD_STATUS, getString(R.string.notif_ch_download_status), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(getString(R.string.notif_ch_download_status_desc));
        notifyMan.createNotificationChannel(channel);
    }

    protected void prepareBottomNav() {
        AHBottomNavigation bn = findViewById(R.id.bottom_navigation);
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_install, R.drawable.ic_round_save_alt_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_scripts, R.drawable.ic_outline_receipt_long_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_logs, R.drawable.ic_commit, R.color.fox_accent));

        bn.setBehaviorTranslationEnabled(false);
        bn.setUseElevation(true);

        bn.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.fox_title_solid_bg));
        bn.setAccentColor(ContextCompat.getColor(this, R.color.fox_accent));
        bn.setInactiveColor(ContextCompat.getColor(this, R.color.google_gray));

        bn.setTitleTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.euclid_flex));
        bn.setTitleTextSizeInSp(14, 12);
        bn.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bn.setOnTabSelectedListener(this::switchPages);

        FragmentManager fm = getSupportFragmentManager();
        if (install == null)
            fm.beginTransaction().add(R.id.nav_frame, (install = new InstallFragment())).commit();
        if (scripts == null)
            fm.beginTransaction().add(R.id.nav_frame, (scripts = new ScriptsFragment())).hide(scripts).commit();
        if (logs == null)
            fm.beginTransaction().add(R.id.nav_frame, (logs = new LogsFragment())).hide(logs).commit();
    }

    private boolean switchPages(int position, boolean wasSelected) {
        if (wasSelected) return false;
        FragmentTransaction tsa = getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.scale, 0);
        switch (position) {
            case 1:  tsa.show(scripts).hide(install).hide(logs).commit(); return true;
            case 2:  tsa.show(logs).hide(install).hide(scripts).commit(); return true;
            default: tsa.show(install).hide(scripts).hide(logs).commit(); return true;
        }
    }
}