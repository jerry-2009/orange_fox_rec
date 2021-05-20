package com.fordownloads.orangefox.utils;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.BuildConfig;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.service.Scheduler;
import com.fordownloads.orangefox.consts;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.io.SuFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.sentry.Sentry;

public class Tools {
    public static void reportException(Exception e, boolean notImportant) {
        if (!BuildConfig.DEBUG && !notImportant)
            Sentry.captureException(e);
    }
    public static void reportException(Exception e) {
        reportException(e, false);
    }

    public static String getBuildType(Context c, JSONObject release) throws JSONException {
        switch (release.getString("type")) {
            case "stable":
            case "Stable":
                return c.getString(R.string.rel_stable);
            case "beta":
            case "Beta":
                return c.getString(R.string.rel_beta);
        }
        return release.getString("type");
    }

    public static String cap(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String getORS() {
        File f = new File("/cache");
        return (f.exists() && f.isDirectory()) ? consts.ORS_FILE : "/data" + consts.ORS_FILE;
    }

    public static Snackbar showSnackbar(Activity activity, View view, int msg) {
        return showSnackbar(activity, view, msg, null);
    }

    public static String getBackupFileName() {
        return new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss_").format(new Date()) + Build.DEVICE;
    }

    public static Snackbar showSnackbar(Activity activity, View view, int msg, BottomSheetDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            view = activity.findViewById(R.id.installButton);
        }

        return Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAnchorView(view)
                .setActionTextColor(ContextCompat.getColor(activity, R.color.fox_accent))
                .setBackgroundTint(ContextCompat.getColor(activity, R.color.fox_card))
                .setTextColor(ContextCompat.getColor(activity, R.color.white))
                .setDuration(6000);
    }

    public static String formatDate(long date) {
        return DateFormat.getDateTimeInstance().format(new Date(date*1000));
    }

    public static String formatDate(String dateStr) {
        try {
            DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
            return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(format.parse(dateStr)));
        } catch (Exception ignored) { }
        return dateStr;
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

    public static int[] getScreenSize(Activity context) {
        int[] sizes = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect r = context.getWindowManager().getCurrentWindowMetrics().getBounds();
            sizes[0] = r.width();
            sizes[1] = r.height();
        } else {
            Point size = new Point();
            context.getWindowManager().getDefaultDisplay().getSize(size);
            sizes[0] = size.x;
            sizes[1] = size.y;
        }
        return sizes;
    }

    public static void share(Context context, String fileName, File log) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(pref.SHARE_DIALOG_SHOWN, false)) {
            shareNoDialog(context, fileName, log);
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.share_help, null);
            alert.setView(view);
            alert.setPositiveButton(R.string.btn_continue, (dlg, num) -> {
                prefs.edit().putBoolean(pref.SHARE_DIALOG_SHOWN, true).apply();
                shareNoDialog(context, fileName, log);
            });

            alert.show().getWindow().setLayout(1080, 1260);
        }
    }

    public static void shareNoDialog(Context context, String fileName, File log) {
        String mime;
        if (fileName.endsWith(".zip"))
            mime = "application/zip";
        else
            mime = "text/plain";
        try {
            Uri uri = FileProvider.getUriForFile(context, "com.fordownloads.orangefox.fileprovider", log);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mime);
            intent.setClipData(new ClipData(fileName, new String[] { mime }, new ClipData.Item(uri)));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
        } catch (Exception e) {
            Tools.reportException(e, true);
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isLandscape(Configuration config, int[] sizes) {
        return config.orientation == Configuration.ORIENTATION_LANDSCAPE && (float)(sizes[0] / sizes[1]) > 1.6;
    }

    public static BottomSheetDialog initBottomSheet(Activity activity, View sheetView) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.ThemeBottomSheet);

        dialog.setContentView(sheetView);
        dialog.setDismissWithAnimation(true);

        int[] sizes = getScreenSize(activity);

        dialog.setOnShowListener(d -> BottomSheetBehavior.from(dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet))
                    .setPeekHeight(sheetView.getHeight()));

        View card = sheetView.findViewById(R.id.cardDialog);
        ViewGroup.LayoutParams layoutParams = card.getLayoutParams();
        layoutParams.width = Math.min(sizes[0], sizes[1]) - (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 48 : 0);
        card.setLayoutParams(layoutParams);

        sheetView.setY(sizes[1]);

        return dialog;
    }

    public static String getFileFromFilePicker(Intent resultData) {
        File file = new File(resultData.getData().getPath());
        String path = file.getAbsolutePath();
        if (file.exists())
            return path;

        String[] uri = path.split(":");
        uri[0] = uri[0].replace("/document/", "");
        if (uri.length != 2 || uri[0].equals("msf"))
            return null;
        else if (uri[0].equals("primary") && new File("/sdcard/" + uri[1]).exists())
            return "/sdcard/" + uri[1];
        else if (new File("/storage/" + uri[0] + "/" + uri[1]).exists())
            return "/external_sd/" + uri[1];
        else if (new SuFile("/mnt/media_rw/" + uri[0] + "/" + uri[1]).exists())
            return "/usb_otg/" + uri[1];
        return null;
    }

    public static void createTable(Context context, TableLayout table, Map<Integer, String> content) {
        table.removeAllViews();
        for (Map.Entry<Integer, String> e : content.entrySet()) {
            TableRow row = new TableRow(context);
            TextView tv = new TextView(new ContextThemeWrapper(context, R.style.ThemeTextGray));
            tv.setText(e.getKey());
            row.addView(tv);
            tv = new TextView(new ContextThemeWrapper(context, R.style.ThemeTextBold));
            tv.setText(e.getValue());
            row.addView(tv);
            table.addView(row);
        }
    }
}
