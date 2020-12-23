package com.fordownloads.orangefox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.downloader.PRDownloader;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

public class ActionReceiver extends BroadcastReceiver {
    public void onReceive (Context context , Intent intent) {
        //Bundle bundle = intent.getExtras();
        //if (bundle != null)
        //    for (String key : bundle.keySet())
        //        Log.e("OFR!!", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));

        switch (intent.getAction()) {
            case "com.fordownloads.orangefox.Cancel":
                PRDownloader.cancelAll();
                break;
            case "com.fordownloads.orangefox.Reboot":
                if(!Shell.su("reboot recovery").exec().isSuccess())
                    Toast.makeText(context, R.string.err_reboot_notify, Toast.LENGTH_LONG).show();
                break;
            case "com.fordownloads.orangefox.ORS":
                if(!new SuFile(vars.ORS_FILE).delete())
                    Toast.makeText(context, R.string.err_ors_delete, Toast.LENGTH_LONG).show();
                break;
            case "com.fordownloads.orangefox.Start":
                context.startActivity(new Intent(context, InstallActivity.class)
                        .putExtra("bg", true)
                        .putExtra("md5", intent.getStringExtra("md5"))
                        .putExtra("url", intent.getStringExtra("url"))
                        .putExtra("version", intent.getStringExtra("ver"))
                        .putExtra("install", true)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
        }
    }
}