package com.fordownloads.orangefox.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.fordownloads.orangefox.ActionReceiver;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.vars;
import com.google.android.material.snackbar.Snackbar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {
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
