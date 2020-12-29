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
    static {
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10));
        try {
            StrictMode.class.getMethod("disableDeathOnFileUriExposure").invoke(null);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        prepareBottomNav();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    protected void prepareBottomNav() {
        AHBottomNavigation bn = findViewById(R.id.bottom_navigation);
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_install, R.drawable.ic_round_save_alt_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_scripts, R.drawable.ic_outline_receipt_long_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_backups, R.drawable.ic_outline_cloud_download_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_logs, R.drawable.ic_commit, R.color.fox_accent));

        bn.setBehaviorTranslationEnabled(false);
        bn.setUseElevation(true);

        bn.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.fox_title_solid_bg));
        bn.setAccentColor(ContextCompat.getColor(this, R.color.fox_accent));
        bn.setInactiveColor(ContextCompat.getColor(this, R.color.google_gray));

        bn.setTitleTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.euclid_flex));
        bn.setTitleTextSizeInSp(14, 12);
        bn.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        if (getSupportFragmentManager().findFragmentByTag("install") == null)
            getSupportFragmentManager().beginTransaction().add(R.id.nav_frame, new InstallFragment(), "install").commit();

        bn.setOnTabSelectedListener((position, wasSelected) -> {
            if (wasSelected)
                return true;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction tsa = fm.beginTransaction();
            tsa.setCustomAnimations(R.anim.scale, R.anim.scale);

            switch (position) {
                case 3:
                    if(fm.findFragmentByTag("logs") != null) tsa.show(fm.findFragmentByTag("logs")).commit();
                    else tsa.add(R.id.nav_frame, new LogsFragment(), "logs").commit();

                    if(fm.findFragmentByTag("scripts") != null) fm.beginTransaction().hide(fm.findFragmentByTag("scripts")).commit();
                    if(fm.findFragmentByTag("install") != null) fm.beginTransaction().hide(fm.findFragmentByTag("install")).commit();
                    if(fm.findFragmentByTag("backups") != null) fm.beginTransaction().hide(fm.findFragmentByTag("backups")).commit();

                    break;
                case 2:
                    if(fm.findFragmentByTag("backups") != null) tsa.show(fm.findFragmentByTag("backups")).commit();
                    else tsa.add(R.id.nav_frame, new BackupsFragment(), "backups").commit();

                    if(fm.findFragmentByTag("scripts") != null) fm.beginTransaction().hide(fm.findFragmentByTag("scripts")).commit();
                    if(fm.findFragmentByTag("install") != null) fm.beginTransaction().hide(fm.findFragmentByTag("install")).commit();
                    if(fm.findFragmentByTag("logs") != null) fm.beginTransaction().hide(fm.findFragmentByTag("logs")).commit();

                    break;
                case 1:
                    if(fm.findFragmentByTag("scripts") != null) tsa.show(fm.findFragmentByTag("scripts")).commit();
                    else tsa.add (R.id.nav_frame, new ScriptsFragment(), "scripts").commit();

                    if(fm.findFragmentByTag("install") != null) fm.beginTransaction().hide(fm.findFragmentByTag("install")).commit();
                    if(fm.findFragmentByTag("backups") != null) fm.beginTransaction().hide(fm.findFragmentByTag("backups")).commit();
                    if(fm.findFragmentByTag("logs") != null) fm.beginTransaction().hide(fm.findFragmentByTag("logs")).commit();

                    break;
                default:
                    if(fm.findFragmentByTag("install") != null) tsa.show(fm.findFragmentByTag("install")).commit();
                    else tsa.add (R.id.nav_frame, new InstallFragment(), "install").commit();

                    if(fm.findFragmentByTag("scripts") != null) fm.beginTransaction().hide(fm.findFragmentByTag("scripts")).commit();
                    if(fm.findFragmentByTag("backups") != null) fm.beginTransaction().hide(fm.findFragmentByTag("backups")).commit();
                    if(fm.findFragmentByTag("logs") != null) fm.beginTransaction().hide(fm.findFragmentByTag("logs")).commit();

                    break;
            }
            return true;
        });
    }

}