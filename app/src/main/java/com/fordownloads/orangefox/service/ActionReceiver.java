package com.fordownloads.orangefox.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.vars;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

public class ActionReceiver extends BroadcastReceiver {
    public void onReceive (Context context , Intent intent) {
                Log.i("OFR-Action", intent.getAction());

        switch (intent.getAction()) {
            case "com.fordownloads.orangefox.Reboot":
                if(!Shell.su("reboot recovery").exec().isSuccess())
                    Toast.makeText(context, R.string.err_reboot_notify, Toast.LENGTH_LONG).show();
                break;
            case "com.fordownloads.orangefox.ORS":
                if(new SuFile(vars.ORS_FILE).delete())
                    NotificationManagerCompat.from(context).cancel(vars.NOTIFY_DOWNLOAD_SAVED);
                else
                    Toast.makeText(context, R.string.err_ors_delete, Toast.LENGTH_LONG).show();
                break;
        }
    }
}