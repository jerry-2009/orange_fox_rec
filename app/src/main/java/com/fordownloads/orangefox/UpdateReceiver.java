package com.fordownloads.orangefox;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.downloader.PRDownloader;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

public class UpdateReceiver extends BroadcastReceiver {
    public void onReceive (Context context , Intent intent) {
        String text = context.getApplicationContext().getString(R.string.notif_new_ver_sub, "R11.0_2", "Stable");
        Intent instIntent = new Intent(context, ActionReceiver.class)
                .setAction("com.fordownloads.orangefox.Start")
                .putExtra("ver", "R11.0_2")
                .putExtra("url", "https://files.orangefox.tech/OrangeFox-Stable/x00t/OrangeFox-R11.0_2-Stable-X00T.zip")
                .putExtra("md5", "2793969f67c6228d6436915ad7757898");
        NotificationManagerCompat.from(context).notify(vars.NOTIFY_NEW_UPD,
                new NotificationCompat.Builder(context, vars.CHANNEL_UPDATE)
                        .setContentTitle(context.getApplicationContext().getString(R.string.notif_new_ver))
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setSmallIcon(R.drawable.ic_outline_new_releases_24)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setColor(ContextCompat.getColor(context, R.color.fox_notify))
                        .addAction(R.drawable.ic_round_check_24, context.getApplicationContext().getString(R.string.install),
                                PendingIntent.getBroadcast(context, 0, instIntent, 0))
                .build());
    }
}