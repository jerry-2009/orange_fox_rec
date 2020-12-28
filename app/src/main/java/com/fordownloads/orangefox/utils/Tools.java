package com.fordownloads.orangefox.utils;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.service.Scheduler;
import com.fordownloads.orangefox.consts;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class Tools {
    public static String getBuildType(Context c, JSONObject release) throws JSONException {
        switch (release.getString("type")) {
            case "stable":
                return c.getString(R.string.rel_stable);
            case "beta":
                return c.getString(R.string.rel_beta);
        }
        return release.getString("build_type");
    }

    public static void dialogFinish(Activity getActivity, int msg) {
        dialogFinish(getActivity, getActivity.getString(msg));
    }

    public static void dialogFinish(Activity getActivity, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity);
        builder.setMessage(msg).setPositiveButton(R.string.close, (dialog, id) -> getActivity.finish()).setOnCancelListener(dialog -> getActivity.finish());
        builder.create().show();
    }

    public static Snackbar showSnackbar(Activity activity, View view, int msg) {
        return Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(activity, R.color.fox_accent))
                .setBackgroundTint(ContextCompat.getColor(activity, R.color.fox_card))
                .setTextColor(ContextCompat.getColor(activity, R.color.white))
                .setDuration(6000)
                .setAnchorView(view);
    }

    public static String formatDate(long date) {
        return DateFormat.getDateTimeInstance().format(new Date(date*1000));
    }

    public static String formatSize(Context context, int size) {
        return context.getString(R.string.size_mb, size/1048576);
    }

    public static String buildList(JSONObject release, String name) throws JSONException {
        JSONArray array = release.getJSONArray(name);
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < array.length(); i++)
            list.append("<li>\t").append(array.getString(i)).append("</li>\n");
        return list.toString();
    }

    public static boolean scheduleJob(Context context, JobScheduler mScheduler, int network) {
        ComponentName serviceName = new ComponentName(context.getPackageName(),
                Scheduler.class.getName());

        return mScheduler.schedule(
                new JobInfo.Builder(consts.SCHEDULER_JOB_ID, serviceName)
                .setRequiredNetworkType(network)
                .setPeriodic(consts.ONE_DAY)
                .setPersisted(true)
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true).build()
        ) == JobScheduler.RESULT_SUCCESS;
    }
}
