package com.fordownloads.orangefox.utils;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class API {
    public static Map<String, Object> request(String reqUrl) {
        Map<String, Object> map = new HashMap<>();

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.orangefox.download/v3/" + reqUrl)
                    .build();
            Response response = client.newCall(request).execute();

            map.put("success", response.isSuccessful());
            map.put("code", response.code());
            map.put("response", response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", 0);
            map.put("success", false);
        }

        return map;
    }

    public static void findUpdate(JobService context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Map<String, Object> responseLast = API.request("releases/?limit=1&codename=" + prefs.getString(pref.DEVICE_CODE, "err"));

            String id;
            if ((int)responseLast.get("code") == 200)
                id = new JSONObject((String) responseLast.get("response")).getJSONArray("data").getJSONObject(0).getString("_id");
            else {
                Log.e("OFR-JOB", "Server error");
                return;
            }

            if (id == null || id.equals(prefs.getString(pref.RELEASE_ID, "err"))) {
                Log.e("OFR-JOB", "No id/no new versions");
                return;
            }

            Map<String, Object> response = API.request("releases/get?_id=" + id);
            if (!(boolean) responseLast.get("success")) {
                Log.e("OFR-JOB", "Can't get release info");
                return;
            }

            JSONObject release = new JSONObject((String) response.get("response"));

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
                    .putExtra("url", release.getJSONObject("mirrors").getString("DL"))
                    .putExtra("md5", release.getString("md5"))
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
            e.printStackTrace();
        }
        Log.i("OFR-JOB", "Job finished successfully");
    }
}
