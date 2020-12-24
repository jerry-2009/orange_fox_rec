package com.fordownloads.orangefox;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.downloader.PRDownloader;
import com.fordownloads.orangefox.ui.Tools;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.prefs.Preferences;

public class ActionReceiver extends BroadcastReceiver {
    public void onReceive (Context context , Intent intent) {
                Log.e("OFR!!", intent.getAction());

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
            case "com.fordownloads.orangefox.Update":
                try {
                    API.findUpdate(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}