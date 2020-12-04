package com.fordownloads.orangefox.ui;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import com.fordownloads.orangefox.R;

public class tools {
    public static void dialogFinish(Activity getActivity, int msg) {
        dialogFinish(getActivity, getActivity.getString(msg));
    }

    public static void dialogFinish(Activity getActivity, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity);
        builder.setMessage(msg).setPositiveButton(R.string.close, (dialog, id) -> getActivity.finish()).setOnCancelListener(dialog -> getActivity.finish());
        builder.create().show();
    }
}
