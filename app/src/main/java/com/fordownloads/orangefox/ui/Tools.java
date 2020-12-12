package com.fordownloads.orangefox.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fordownloads.orangefox.R;
import com.google.android.material.snackbar.Snackbar;

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
}
