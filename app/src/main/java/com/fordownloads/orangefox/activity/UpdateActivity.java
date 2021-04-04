package com.fordownloads.orangefox.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.BuildConfig;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.service.DownloadService;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.thefuntasty.hauler.HaulerView;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

import static com.fordownloads.orangefox.utils.API.client;

public class UpdateActivity extends AppCompatActivity {
    ExtendedFloatingActionButton _installButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        boolean isNoUpdate = getIntent().getBooleanExtra("noUpdate", false);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle(R.string.update_changelog);
        float originalElevation = myToolbar.getElevation();

        ((HaulerView)findViewById(R.id.haulerView)).setOnDragActivityListener((offset, v1) -> {
            if (offset <= 15 && offset >= -15) {
                myToolbar.setElevation(originalElevation-(Math.abs(offset)/15*originalElevation));
                myToolbar.setAlpha(1);
            } else if (offset >= -50 && offset <= 50) {
                myToolbar.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                myToolbar.setElevation(0);
            } else {
                myToolbar.setAlpha(0);
                myToolbar.setElevation(0);
            }
        });

        if (isNoUpdate)
            ab.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_backspace_24);
        else {
            _installButton = findViewById(R.id.installButton);
            _installButton.setOnClickListener(v -> {
                if (Shell.rootAccess() || getPackageManager().canRequestPackageInstalls()) {
                    Intent intent = new Intent(this, DownloadService.class).putExtra("downloadApp", true);
                    startService(intent);
                    setResult(Activity.RESULT_OK);
                    finish();
                } else
                    startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:com.fordownloads.orangefox")));
            });
        }

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        ((HaulerView)findViewById(R.id.haulerView)).setOnDragDismissedListener(v -> finish());

        FrameLayout _loadingView = findViewById(R.id.loadingLayout);
        new Thread(() -> {
            try {
                Request request = new Request.Builder().url("https://gitlab.com/OrangeFox/misc/appdev/updates/-/raw/master/changelog.txt").build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        try {
                            ((TextView) findViewById(R.id.multiTextView)).setText(getString(R.string.update_installed_vers, BuildConfig.VERSION_NAME, response.body().string()));
                            _loadingView.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            _loadingView.setVisibility(View.GONE);
                                            if (!isNoUpdate)
                                                _installButton.show();
                                        }
                                    });
                        } catch (Exception e) {
                            findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }
            } catch (Exception ignored) { }
            findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}