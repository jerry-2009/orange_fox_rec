package com.fordownloads.orangefox.ui;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.fordownloads.orangefox.R;

public class tools {
    public static void dialogFinish(Activity getActivity, int msgId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity);
        builder.setMessage(msgId).setPositiveButton(R.string.close, (dialog, id) -> getActivity.finish());
        builder.create().show();
    }
}
