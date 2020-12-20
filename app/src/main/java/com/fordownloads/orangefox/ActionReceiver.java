package com.fordownloads.orangefox;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.downloader.PRDownloader;

public class ActionReceiver extends BroadcastReceiver {
    public void onReceive (Context context , Intent intent) {
        switch (intent.getIntExtra("type", -1)) {
            case 0:
                PRDownloader.cancelAll();
                break;
            case 1:
                Intent retryIntent = new Intent(context, InstallActivity.class)
                    .putExtra("url", intent.getStringExtra("url"))
                    .putExtra("version",  intent.getStringExtra("version"))
                    .putExtra("md5",  intent.getStringExtra("md5"))
                    .putExtra("install", intent.getStringExtra("install"));
                context.startActivity(retryIntent);
                break;
        }
    }
}