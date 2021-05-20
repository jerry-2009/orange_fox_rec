package com.fordownloads.orangefox.utils;

import android.app.PendingIntent;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.activity.RecyclerActivity;
import com.fordownloads.orangefox.service.DownloadService;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.consts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class API {
    public static OkHttpClient client = new OkHttpClient();
    public static APIResponse request(String reqUrl) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.orangefox.download/v3/" + reqUrl)
                    .build();
            Response response = client.newCall(request).execute();
            return new APIResponse(reqUrl, response.isSuccessful(), response.code(), response.body().string());
        } catch (UnknownHostException e) {
            return new APIResponse(reqUrl);
        } catch (IOException e) {
            Tools.reportException(e);
            return new APIResponse(reqUrl);
        }
    }

    public static void findUpdate(JobService context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            APIResponse responseLast = API.request("releases/?limit=1&codename=" + prefs.getString(pref.DEVICE_CODE, "err"));

            String id;
            if (responseLast.code == 200)
                id = new JSONObject(responseLast.data).getJSONArray("data").getJSONObject(0).getString("_id");
            else {
                Log.e("OFR-JOB", "Server error");
                return;
            }

            if (id == null || id.equals(prefs.getString(pref.RELEASE_ID, "err"))) {
                Log.e("OFR-JOB", "No id/no new versions");
                return;
            }

            APIResponse response = API.request("releases/get?_id=" + id);
            if (!responseLast.success) {
                Log.e("OFR-JOB", "Can't get release info");
                return;
            }

            JSONObject release = new JSONObject(response.data);

            prefs.edit().putString(pref.CACHE_RELEASE, release.toString()).putString(pref.RELEASE_ID, id).apply();

            if (!prefs.getBoolean(pref.UPDATES_BETA, true) && release.getString("type").equals("beta")) {
                Log.e("OFR-JOB", "Beta versions not allowed");
                return;
            }

            Log.i("OFR-JOB", "Starting installation/notification...");
            String text = context.getApplicationContext().getString(R.string.notif_new_ver_sub,
                    release.getString("version"),
                    Tools.getBuildType(context, release));
            Intent instIntent = new Intent(context, DownloadService.class)
                    .putExtra("version", release.getString("version"))
                    .putExtra("url", Tools.getUrlMirror(prefs, release.getJSONObject("mirrors")))
                    .putExtra("md5", release.getString("md5"))
                    .putExtra("name", release.getString("filename"))
                    .putExtra("install", true);

            if (prefs.getBoolean(pref.UPDATES_INSTALL, true)) {
                Log.i("OFR-JOB", "Starting installation...");
                context.startForegroundService(instIntent);
            } else {
                Log.i("OFR-JOB", "Starting notification...");
                Intent intentChangeLog = new Intent(context, RecyclerActivity.class)
                        .putExtra("release", prefs.getString(pref.CACHE_RELEASE, "no_cache_release"))
                        .putExtra("type", 1).putExtra("title", R.string.rel_activity);
                NotificationManagerCompat.from(context).notify(consts.NOTIFY_NEW_UPD,
                        new NotificationCompat.Builder(context, consts.CHANNEL_UPDATE)
                                .setContentTitle(context.getApplicationContext().getString(R.string.notif_new_ver))
                                .setContentText(text)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                .setSmallIcon(R.drawable.ic_outline_new_releases_24)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setColor(ContextCompat.getColor(context, R.color.fox_notify))
                                .addAction(R.drawable.ic_round_check_24, context.getString(R.string.install),
                                        PendingIntent.getService(context, 0, instIntent, 0))
                                .addAction(R.drawable.ic_round_check_24, context.getString(R.string.rel_changes),
                                        PendingIntent.getActivity(context, 0, intentChangeLog, 0))
                                .build());
            }
        } catch (JSONException e) {
            Tools.reportException(e);
        }
        Log.i("OFR-JOB", "Job finished successfully");
    }
}
