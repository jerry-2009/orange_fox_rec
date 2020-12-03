package com.fordownloads.orangefox.ui;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.fordownloads.orangefox.R;

public class tools {
    public static void dialogFinish(Activity getActivity, int msgId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity);
        builder.setMessage(msgId).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity.finish();
            }
        });
        builder.create().show();
    }
}
