package com.fordownloads.orangefox.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.utils.MD5;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.utils.Tools;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class DownloadService extends Service {
    boolean install = false, downloadApp = false;
    String url, version, expectedMD5, fileName;
    NotificationManagerCompat notifyMan;
    NotificationCompat.Builder progressNotify;
    String downloadDir;

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("STOP".equals(intent.getAction())) {
            Log.d("OFR","called to cancel service");
            PRDownloader.cancelAll();
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        ((App) this.getApplication()).setDownloadSrv(this);
        Intent stopSelf = new Intent(this, DownloadService.class);
        stopSelf.setAction("STOP");
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_CANCEL_CURRENT);
        notifyMan = NotificationManagerCompat.from(this);

        if (intent.getBooleanExtra("downloadApp", false)) {
            url = "https://gitlab.com/OrangeFox/misc/appdev/updates/-/raw/master/app-release.apk";
            fileName = "app.apk";
            downloadDir = consts.FOX_DIR;
            downloadApp = true;
        } else {
            expectedMD5 = intent.getStringExtra("md5");
            url = intent.getStringExtra("url");
            version = intent.getStringExtra("version");
            fileName = intent.getStringExtra("name");
            install = intent.getBooleanExtra("install", false);
            downloadDir = consts.DOWNLOAD_DIR;
        }

        progressNotify = new NotificationCompat.Builder(this, consts.CHANNEL_DOWNLOAD)
                .setOngoing(true)
                .setContentTitle(downloadApp ? getString(R.string.notif_downloading_app) : getString(R.string.notif_downloading, version))
                .setContentText(getString(R.string.preparing))
                .setSmallIcon(R.drawable.ic_round_get_app_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setProgress(0, 0, true)
                .addAction(R.drawable.ic_round_check_24, this.getString(R.string.inst_cancel), pStopSelf);
        startForeground(consts.NOTIFY_DOWNLOAD_FG, progressNotify.build());
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
        File finalFile = new File(downloadDir, fileName);

        //Prepare notifications
        notifyMan.cancel(consts.NOTIFY_NEW_UPD);
        notifyMan.cancel(consts.NOTIFY_DOWNLOAD_ERROR);
        notifyMan.cancel(consts.NOTIFY_DOWNLOAD_SAVED);
        //

        //Prepare intents for notifications
        Intent openIntent;
        if (downloadApp) {
            Uri fileUri = FileProvider.getUriForFile(this, "com.fordownloads.orangefox.fileprovider", new File(consts.FOX_DIR, "app.apk"));
            openIntent = new Intent(Intent.ACTION_VIEW, fileUri)
                    .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    .setDataAndType(fileUri, "application/vnd.android.package-archive")
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else
            openIntent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(finalFile), "application/zip")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //

        //Show & build notification
        notifyMan.notify(consts.NOTIFY_DOWNLOAD_FG, progressNotify.build());

        NotificationCompat.Builder completeNotify = new NotificationCompat.Builder(this, consts.CHANNEL_DOWNLOAD_STATUS)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.fox_notify))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        //

        DownloadRequest builder = PRDownloader.download(url, downloadDir, fileName)
                .build()
                .setOnCancelListener(() -> Log.i("OFRService", "Download cancelled"))
                .setOnProgressListener(new OnProgressListener() {
                    byte currPercent = 0, lastPercent = -1, skipMB = 0;

                    @Override
                    public void onProgress(Progress progress) {
                        currPercent = (byte) (progress.currentBytes * 100 / progress.totalBytes);
                        if (lastPercent != currPercent) {
                            if (skipMB++ > 5) {
                                notifyMan.notify(consts.NOTIFY_DOWNLOAD_FG, progressNotify
                                        .setProgress(100, currPercent, false)
                                        .setContentText(getString(R.string.inst_progress, progress.currentBytes / 1048576, progress.totalBytes / 1048576))
                                        .build());
                                skipMB = 0;
                            }
                            lastPercent = currPercent;
                        }
                    }
                });

        if (downloadApp)
            builder.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    String text = getString(R.string.inst_downloaded, finalFile);

                    if (!Shell.rootAccess() || !Shell.su("pm install -g /sdcard/Fox/app.apk && am start -n \"com.fordownloads.orangefox/com.fordownloads.orangefox.activity.MainActivity\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER").exec().isSuccess())
                        notifyMan.notify(consts.NOTIFY_DOWNLOAD_SAVED, completeNotify
                                .setSmallIcon(R.drawable.ic_round_check_24)
                                .setContentTitle(getString(R.string.notif_downloaded_app))
                                .setContentText(text)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openIntent, 0))
                                .build());

                    stopForeground(true);
                    stopSelf();
                }

                @Override
                public void onError(Error error) {
                    errorNotify(notifyMan, completeNotify, getString(R.string.err_check_pm_inernet));
                }
            });
        else
            builder.start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        notifyMan.notify(consts.NOTIFY_DOWNLOAD_FG, progressNotify.setContentText(getString(R.string.md5_check))
                                .setProgress(0, 0, true)
                                .build());

                        if (!MD5.checkMD5(expectedMD5, finalFile))
                            errorNotify(notifyMan, completeNotify, getString(R.string.err_md5_wrong));

                        if (install) {
                            if(!Shell.su(
                                    "echo \"install /sdcard/Fox/releases/" + fileName + "\" > " + Tools.getORS())
                                    .exec().isSuccess())
                                errorNotify(notifyMan, completeNotify, getString(R.string.err_ors, finalFile));

                            String text = getString(R.string.notify_pending_reboot_sub, finalFile);
                            notifyMan.notify(consts.NOTIFY_DOWNLOAD_SAVED, completeNotify
                                    .setSmallIcon(R.drawable.ic_round_system_update_24)
                                    .setContentTitle(getString(R.string.notify_pending_reboot, version))
                                    .setContentText(text)
                                    .addAction(R.drawable.ic_round_check_24, getString(R.string.reboot),
                                            PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ActionReceiver.class).setAction("com.fordownloads.orangefox.Reboot"), 0))
                                    .addAction(R.drawable.ic_round_check_24, getString(R.string.inst_cancel),
                                            PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ActionReceiver.class).setAction("com.fordownloads.orangefox.ORS"), 0))
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .build());
                        } else {
                            String text = getString(R.string.inst_downloaded, finalFile);

                            notifyMan.notify(consts.NOTIFY_DOWNLOAD_SAVED, completeNotify
                                    .setSmallIcon(R.drawable.ic_round_check_24)
                                    .setContentTitle(getString(R.string.notif_download_complete, version))
                                    .setContentText(text)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openIntent, 0))
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
        notifyMan.notify(consts.NOTIFY_DOWNLOAD_ERROR, completeNotify
                .setSmallIcon(R.drawable.ic_round_warning_24)
                .setContentTitle(getString(R.string.notif_download_failed, version))
                .setContentText(err)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(err))
                .build());
        stopForeground(true);
        stopSelf();
    }
}