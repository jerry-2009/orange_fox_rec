package com.fordownloads.orangefox.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.fragments.InstallFragment;
import com.fordownloads.orangefox.fragments.LogsFragment;
import com.fordownloads.orangefox.fragments.ScriptsFragment;
import com.fordownloads.orangefox.consts;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    LogsFragment logs;
    ScriptsFragment scripts;
    InstallFragment install;
    View _toolbarWrapper, _statusBarFill;
    AHBottomNavigation bn;
    boolean shortcutMode = false;

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
    public void onSaveInstanceState(@NotNull Bundle state) {
        super.onSaveInstanceState(state);
        if (!shortcutMode && bn != null)
            state.putInt("selected", bn.getCurrentItem());
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.appToolbar));
        _toolbarWrapper = findViewById(R.id.toolbarWrapper);
        _statusBarFill = findViewById(R.id.statusBarFill);
        createNotificationChannel();

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.fox_status_solid_bg));
        getWindow().setBackgroundDrawableResource(R.color.fox_background);

        Intent intent = getIntent();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fmt = fm.beginTransaction();
        if (intent.hasExtra("shortcut")) {
            _toolbarWrapper.setVisibility(View.VISIBLE);
            _statusBarFill.setVisibility(View.VISIBLE);
            findViewById(R.id.nav_frame).setLayoutParams(new androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams(androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.MATCH_PARENT, androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.MATCH_PARENT));

            switch (intent.getStringExtra("shortcut")) {
                case "logs":
                    fmt.add(R.id.nav_frame, (logs = new LogsFragment()), "logs");
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.bnav_logs);
                    break;
                case "scripts":
                    fmt.add(R.id.nav_frame, (scripts = new ScriptsFragment()), "scripts");
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.bnav_scripts);
                    break;
            }
            fmt.commit();
            shortcutMode = true;
            return;
        }

        bn = findViewById(R.id.bottom_navigation);
        PRDownloader.initialize(getApplicationContext(), config);
        prepareBottomNav();

        if (bundle != null)
            switchPages(bundle.getInt("selected"), false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!intent.hasExtra("shortcut") && shortcutMode) {
            setIntent(intent);
            recreate();
        }
        super.onNewIntent(intent);
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
        FragmentTransaction fmt = fm.beginTransaction();

        if ((install = (InstallFragment)fm.findFragmentByTag("install")) == null)
            fmt.add(R.id.nav_frame, (install =  new InstallFragment()), "install");
        if ((scripts = (ScriptsFragment)fm.findFragmentByTag("scripts")) == null)
            fmt.add(R.id.nav_frame, (scripts = new ScriptsFragment()), "scripts").hide(scripts);
        if ((logs = (LogsFragment)fm.findFragmentByTag("logs")) == null)
            fmt.add(R.id.nav_frame, (logs = new LogsFragment()), "logs").hide(logs);
        fmt.commit();
    }

    int prevPos = 0;

    private boolean switchPages(int position, boolean wasSelected) {
        if (wasSelected) return false;
        FragmentTransaction fmt = getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.scale, 0);
        if (position > 0 && prevPos == 0) {
            _toolbarWrapper.setVisibility(View.VISIBLE);
            _statusBarFill.setVisibility(View.VISIBLE);
            _toolbarWrapper.setAlpha(0);
            _statusBarFill.setAlpha(0);
            _toolbarWrapper.setTranslationY(-24);
            _statusBarFill.setTranslationY(-24);
            _toolbarWrapper.animate().alpha(1f).translationY(0).setDuration(250);
            _statusBarFill.animate().alpha(1f).translationY(0).setDuration(250);
        } else if (position == 0) {
            _toolbarWrapper.setVisibility(View.GONE);
            _statusBarFill.setVisibility(View.GONE);
        }
        prevPos = position;
        switch (position) {
            case 1:  fmt.show(scripts).hide(install).hide(logs).commit();
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.bnav_scripts);
                return true;
            case 2:  fmt.show(logs).hide(install).hide(scripts).commit();
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.bnav_logs);
                return true;
            default: fmt.show(install).hide(scripts).hide(logs).commit();
                return true;
        }
    }
}