package com.fordownloads.orangefox.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ui.recycler.RecyclerItems;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                .setDuration(8000)
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
        String list = "";
        for (int i = 0; i < array.length(); i++)
            list += "<li>\t" + array.getString(i) + "</li>\n";
        return list;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
