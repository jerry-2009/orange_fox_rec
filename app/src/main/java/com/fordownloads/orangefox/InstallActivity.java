package com.fordownloads.orangefox;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.fordownloads.orangefox.utils.MD5;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class InstallActivity extends AppCompatActivity {
    boolean install = false, bg = false;
    String url, version, expectedMD5;
    ExtendedFloatingActionButton _cancel;
    Thread installThread;

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
        expectedMD5 = intent.getStringExtra("md5");
        url = intent.getStringExtra("url");
        version = intent.getStringExtra("version");
        install = intent.getBooleanExtra("install", false);
        bg = intent.getBooleanExtra("bg", false);
        moveTaskToBack(bg);

        _cancel = findViewById(R.id.cancelInstallation);

        _cancel.setOnClickListener(v -> PRDownloader.cancelAll());

        beginDownload();
    }

    private void beginDownload(){
        String fileName = URLUtil.guessFileName(url, null, "application/zip");
        File finalFile = new File(vars.DOWNLOAD_DIR, fileName);

        //Find views
        ProgressBar _progressBar = findViewById(R.id.progressBar);
        TextView _progressText = findViewById(R.id.progressText);
        //

        //Prepare notifications
        NotificationManagerCompat notifyMan = NotificationManagerCompat.from(this);

        notifyMan.cancel(vars.NOTIFY_NEW_UPD);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_FG);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_ERROR);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_SAVED);
        //

        //Prepare intents for notifications
        Intent openAppIntent = new Intent(this, InstallActivity.class);
        Intent openZIPIntent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(finalFile), "application/zip")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent cancelIntent = new Intent(this, ActionReceiver.class)
                .setAction("com.fordownloads.orangefox.Notification")
                .putExtra("type", 0);
        Intent rebootIntent = new Intent(this, ActionReceiver.class)
                .setAction("com.fordownloads.orangefox.Notification")
                .putExtra("type", 1);
        Intent deleteIntent = new Intent(this, ActionReceiver.class)
                .setAction("com.fordownloads.orangefox.Notification")
                .putExtra("type", 2);
        //

        //Show & build notification
        NotificationCompat.Builder progressNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD)
                .setOngoing(true)
                .setContentTitle(getString(R.string.notif_downloading, version))
                .setContentText(getString(R.string.preparing))
                .setSmallIcon(R.drawable.ic_round_get_app_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setProgress(0, 0, true)
                .setContentIntent(PendingIntent.getActivity(this, 0, openAppIntent, 0))
                .addAction(R.drawable.ic_round_check_24, getString(R.string.inst_cancel),
                        PendingIntent.getBroadcast(this, 0, cancelIntent, 0));

        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.build());

        NotificationCompat.Builder completeNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD_STATUS)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        //

        PRDownloader.download(url, vars.DOWNLOAD_DIR, fileName)
                .build()
                .setOnStartOrResumeListener(() -> _progressBar.setIndeterminate(false))
                .setOnProgressListener(new OnProgressListener() {
                    byte currPercent = 0, lastPercent = -1, skipMB = 0;

                    @Override
                    public void onProgress(Progress progress) {
                        currPercent = (byte) (progress.currentBytes * 100 / progress.totalBytes);
                        if (lastPercent != currPercent) { // update progress every MB
                            String status = getString(R.string.inst_progress, progress.currentBytes / 1048576, progress.totalBytes / 1048576);
                            _progressText.setText(status);
                            _progressBar.setProgress(currPercent);
                            if (skipMB++ > 2) { // update notification every 2MB, because too frequent updates may hang up app
                                notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify
                                        .setProgress(100, currPercent, false)
                                        .setContentText(status)
                                        .build());
                                skipMB = 0;
                            }
                            lastPercent = currPercent;
                        }
                    }
                })
                .setOnCancelListener(() -> {
                    notifyMan.cancel(vars.NOTIFY_DOWNLOAD_FG);
                    finish();
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        _progressBar.setIndeterminate(true);
                        _cancel.hide();

                        _progressText.setText(R.string.md5_check);
                        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.setContentText(getString(R.string.md5_check))
                                .setProgress(0, 0, true)
                                .build());

                        if (!MD5.checkMD5(expectedMD5, finalFile)) {
                            _progressText.setText(R.string.err_md5_wrong_short);
                            errorNotify(notifyMan, completeNotify, getString(R.string.err_md5_wrong));
                            return;
                        }

                        if (install) {
                            installThread = new Thread(() -> {
                                try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

                                if(!Shell.su(
                                        "echo \"install /sdcard/Fox/releases/" + fileName + "\" > " + vars.ORS_FILE)
                                        .exec().isSuccess()) {
                                    _progressText.setText(getString(R.string.err_ors, finalFile));
                                    errorNotify(notifyMan, completeNotify, getString(R.string.err_ors, finalFile));
                                    if (bg) {
                                        closeActivity();
                                        return;
                                    }
                                }

                                if (bg) {
                                    String text = getString(R.string.notify_pending_reboot_sub, finalFile);
                                    notifyMan.cancel(vars.NOTIFY_DOWNLOAD_FG);
                                    notifyMan.notify(vars.NOTIFY_DOWNLOAD_SAVED, completeNotify.setSmallIcon(R.drawable.ic_round_system_update_24)
                                            .setContentTitle(getString(R.string.notify_pending_reboot, version))
                                            .setContentText(text)
                                            .addAction(R.drawable.ic_round_check_24, getString(R.string.reboot_recovery),
                                                    PendingIntent.getBroadcast(getApplicationContext(), 0, rebootIntent, 0))
                                            .addAction(R.drawable.ic_round_check_24, getString(R.string.inst_cancel),
                                                    PendingIntent.getBroadcast(getApplicationContext(), 0, deleteIntent, 0))
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                            .build());
                                    finish();
                                    return;
                                }

                                if(!Shell.su(
                                        "reboot recovery")
                                        .exec().isSuccess()) {
                                    _progressText.setText(R.string.err_reboot);
                                    errorNotify(notifyMan, completeNotify, getString(R.string.err_reboot));
                                }

                                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
                                _progressText.setText(R.string.err_where_reboot);
                            });

                            installThread.start();

                            if (!bg)    {
                                _progressText.setText(R.string.inst_installing);
                                notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.setContentText(getString(R.string.notif_rebooting))
                                        .setProgress(0, 0, true)
                                        .build());
                            }
                        } else {
                            String text = getString(R.string.inst_downloaded, finalFile);
                            _progressText.setText(text);

                            notifyMan.notify(vars.NOTIFY_DOWNLOAD_SAVED, completeNotify.setSmallIcon(R.drawable.ic_round_check_24)
                                    .setContentTitle(getString(R.string.notif_download_complete, version))
                                    .setContentText(text)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openZIPIntent, 0))
                                    .build());

                            closeActivity();
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        _progressText.setText(R.string.inst_down_err);
                        errorNotify(notifyMan, completeNotify, getString(R.string.err_check_pm_inernet));
                    }
                });
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (installThread != null)
            installThread.interrupt();
    }

    public void errorNotify(NotificationManagerCompat notifyMan, NotificationCompat.Builder completeNotify, String err) {
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_FG);
        notifyMan.notify(vars.NOTIFY_DOWNLOAD_ERROR, completeNotify.setSmallIcon(R.drawable.ic_round_warning_24)
                .setContentTitle(getString(R.string.notif_download_failed, version))
                .setContentText(err)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(err))
                .build());
        closeActivity();
    }

    public void closeActivity() {
        NotificationManagerCompat.from(this).cancel(vars.NOTIFY_DOWNLOAD_FG);
        new Thread(() -> {
            try { Thread.sleep(6000); } catch (InterruptedException e) { e.printStackTrace(); }
            finish();
        }).start();
    }
}