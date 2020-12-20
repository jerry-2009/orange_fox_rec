package com.fordownloads.orangefox;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.topjohnwu.superuser.Shell;

public class InstallActivity extends AppCompatActivity {
    boolean install = false;
    String url;
    ExtendedFloatingActionButton _cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        CoordinatorLayout layout = findViewById(R.id.instLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        install = intent.getBooleanExtra("install", false);
        _cancel = findViewById(R.id.cancelInstallation);

        _cancel.setOnClickListener(v -> {
            PRDownloader.cancelAll();
            NotificationManagerCompat.from(this).cancel(vars.NOTIFY_DOWNLOAD_FG);
            finish();
        });

        beginDownload();
    }

    private void beginDownload(){
        ProgressBar _progressBar = findViewById(R.id.progressBar);
        TextView _progressText = findViewById(R.id.progressText);

        NotificationManagerCompat notifyMan = NotificationManagerCompat.from(this);

        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_FG);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_ERROR);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_SAVED);

        Intent cancelIntent = new Intent(this, ActionReceiver.class)
            .setAction("com.fordownloads.orangefox.Notification")
            .putExtra("type", 0);

        NotificationCompat.Builder progressNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD)
                .setContentTitle(getString(R.string.notif_downloading))
                .setContentText(getString(R.string.preparing))
                .setSmallIcon(R.drawable.ic_round_get_app_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_round_check_24, getString(R.string.inst_cancel),
                        PendingIntent.getBroadcast(this, 0, cancelIntent, 0));
        progressNotify.setProgress(0, 0, true);
        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.build());

        NotificationCompat.Builder completeNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (vars.updateZip.exists())
            vars.updateZip.delete();

            PRDownloader.download(url, vars.updateZip.getAbsolutePath(), "OFupdate.zip")
                    .build()
                    .setOnStartOrResumeListener(() -> _progressBar.setIndeterminate(false))
                    .setOnProgressListener(progress -> {
                        int currPercent = (int) (progress.currentBytes * 100 / progress.totalBytes);
                        String status = getString(R.string.inst_progress, progress.currentBytes / 1048576, progress.totalBytes / 1048576);
                        _progressText.setText(status);
                        _progressBar.setProgress(currPercent);

                        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify
                                .setProgress(100, currPercent, false)
                                .setContentText(status)
                                .build());
                    })
                    .setOnCancelListener(() -> {
                        Toast.makeText(this, "Finally Cancelled", Toast.LENGTH_SHORT).show();
                        NotificationManagerCompat.from(getApplicationContext()).cancel(vars.NOTIFY_DOWNLOAD_FG);
                        finish();
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            _progressBar.setIndeterminate(true);
                            _cancel.hide();

                            if (install) {
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    Shell.su("echo \"install /sdcard/Fox/OFupdate.zip\" > /cache/recovery/openrecoveryscript && reboot recovery").exec();
                                }).start();

                                _progressText.setText(R.string.inst_installing);

                                notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.setContentText(getString(R.string.notif_download_complete))
                                        .setProgress(0, 0, true)
                                        .build());
                            } else {
                                _progressText.setText(getString(R.string.inst_downloaded, vars.updateZip + "/OFUpdate.zip"));

                                notifyMan.notify(vars.NOTIFY_DOWNLOAD_SAVED, completeNotify.setSmallIcon(R.drawable.ic_round_check_24)
                                        .setContentTitle(getString(R.string.notif_download_complete))
                                        .setContentText(getString(R.string.inst_downloaded, vars.updateZip + "/OFUpdate.zip"))
                                        .build());

                                closeActivity();
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            _progressText.setText(R.string.inst_down_err);

                            notifyMan.notify(vars.NOTIFY_DOWNLOAD_ERROR, completeNotify.setSmallIcon(R.drawable.ic_round_warning_24)
                                    .setContentTitle(getString(R.string.notif_download_failed))
                                    .setContentText(getString(R.string.err_check_pm_inernet))
                                    .build());

                            closeActivity();
                        }
                    });
    }

    @Override
    public void onBackPressed() {}

    public void closeActivity() {
        NotificationManagerCompat.from(this).cancel(vars.NOTIFY_DOWNLOAD_FG);
        new Thread(() -> {
            try { Thread.sleep(6000); } catch (InterruptedException e) { e.printStackTrace(); }
            finish();
        }).start();
    }
}