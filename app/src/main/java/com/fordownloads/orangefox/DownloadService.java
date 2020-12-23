package com.fordownloads.orangefox;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.fordownloads.orangefox.utils.MD5;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class DownloadService extends Service {
    boolean install = false;
    String url, version, expectedMD5;
    NotificationManagerCompat notifyMan;
    NotificationCompat.Builder progressNotify;

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("STOP".equals(intent.getAction())) {
            Log.d("OFR","called to cancel service");
            PRDownloader.cancelAll();
            stopForeground(true);
            stopSelf();
            return 0;
        }

        ((App) this.getApplication()).setDownloadSrv(this);
        Intent stopSelf = new Intent(this, DownloadService.class);
        stopSelf.setAction("STOP");
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_CANCEL_CURRENT);

        expectedMD5 = intent.getStringExtra("md5");
        url = intent.getStringExtra("url");
        version = intent.getStringExtra("version");
        install = intent.getBooleanExtra("install", false);
        notifyMan = NotificationManagerCompat.from(this);

        progressNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD)
                .setOngoing(true)
                .setContentTitle(getString(R.string.notif_downloading, version))
                .setContentText(getString(R.string.preparing))
                .setSmallIcon(R.drawable.ic_round_get_app_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setProgress(0, 0, true)
                .addAction(R.drawable.ic_round_check_24, this.getString(R.string.inst_cancel), pStopSelf);
        startForeground(vars.NOTIFY_DOWNLOAD_FG, progressNotify.build());
        beginDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((App)this.getApplication()).setDownloadSrv(null);
    }

    private void beginDownload(){
        String fileName = URLUtil.guessFileName(url, null, "application/zip");
        File finalFile = new File(vars.DOWNLOAD_DIR, fileName);

        //Prepare notifications
        notifyMan.cancel(vars.NOTIFY_NEW_UPD);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_ERROR);
        notifyMan.cancel(vars.NOTIFY_DOWNLOAD_SAVED);
        //

        //Prepare intents for notifications
        Intent openZIPIntent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(finalFile), "application/zip")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent actionIntent = new Intent(this, ActionReceiver.class);
        //

        //Show & build notification
        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.build());

        NotificationCompat.Builder completeNotify = new NotificationCompat.Builder(this, vars.CHANNEL_DOWNLOAD_STATUS)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        //

        PRDownloader.download(url, vars.DOWNLOAD_DIR, fileName)
                .build()
                .setOnCancelListener(() -> Log.e("OFRService", "Download cancelled"))
                .setOnProgressListener(new OnProgressListener() {
                    byte currPercent = 0, lastPercent = -1, skipMB = 0;

                    @Override
                    public void onProgress(Progress progress) {
                        currPercent = (byte) (progress.currentBytes * 100 / progress.totalBytes);
                        if (lastPercent != currPercent) {
                            if (skipMB++ > 5) {
                                notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify
                                        .setProgress(100, currPercent, false)
                                        .setContentText(getString(R.string.inst_progress, progress.currentBytes / 1048576, progress.totalBytes / 1048576))
                                        .build());
                                skipMB = 0;
                            }
                            lastPercent = currPercent;
                        }
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        notifyMan.notify(vars.NOTIFY_DOWNLOAD_FG, progressNotify.setContentText(getString(R.string.md5_check))
                                .setProgress(0, 0, true)
                                .build());

                        if (!MD5.checkMD5(expectedMD5, finalFile))
                            errorNotify(notifyMan, completeNotify, getString(R.string.err_md5_wrong));

                        if (install) {
                            if(!Shell.su(
                                    "echo \"install /sdcard/Fox/releases/" + fileName + "\" > " + vars.ORS_FILE)
                                    .exec().isSuccess())
                                errorNotify(notifyMan, completeNotify, getString(R.string.err_ors, finalFile));

                            String text = getString(R.string.notify_pending_reboot_sub, finalFile);
                            notifyMan.notify(vars.NOTIFY_DOWNLOAD_SAVED, completeNotify
                                    .setSmallIcon(R.drawable.ic_round_system_update_24)
                                    .setContentTitle(getString(R.string.notify_pending_reboot, version))
                                    .setContentText(text)
                                    .addAction(R.drawable.ic_round_check_24, getString(R.string.reboot_recovery),
                                            PendingIntent.getBroadcast(getApplicationContext(), 0, actionIntent.setAction("com.fordownloads.orangefox.Reboot"), 0))
                                    .addAction(R.drawable.ic_round_check_24, getString(R.string.inst_cancel),
                                            PendingIntent.getBroadcast(getApplicationContext(), 0, actionIntent.setAction("com.fordownloads.orangefox.ORS"), 0))
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .build());
                        } else {
                            String text = getString(R.string.inst_downloaded, finalFile);

                            notifyMan.notify(vars.NOTIFY_DOWNLOAD_SAVED, completeNotify
                                    .setSmallIcon(R.drawable.ic_round_check_24)
                                    .setContentTitle(getString(R.string.notif_download_complete, version))
                                    .setContentText(text)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openZIPIntent, 0))
                                    .build());
                        }
                        stopForeground(true);
                        stopSelf();
                    }

                    @Override
                    public void onError(Error error) {
                        errorNotify(notifyMan, completeNotify, getString(R.string.err_check_pm_inernet));
                    }
                });
    }

    public void errorNotify(NotificationManagerCompat notifyMan, NotificationCompat.Builder completeNotify, String err) {
        notifyMan.notify(vars.NOTIFY_DOWNLOAD_ERROR, completeNotify
                .setSmallIcon(R.drawable.ic_round_warning_24)
                .setContentTitle(getString(R.string.notif_download_failed, version))
                .setContentText(err)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(err))
                .build());
        stopForeground(true);
        stopSelf();
    }
}