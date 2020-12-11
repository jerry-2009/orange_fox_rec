package com.fordownloads.orangefox;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.view.animation.PathInterpolatorCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.topjohnwu.superuser.Shell;

import static com.topjohnwu.superuser.internal.Utils.getContext;

public class Install {

    public static void dialog(Activity activity, String ver, String type, String url) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.ThemeBottomSheet);

        View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_install, null);
        dialog.setContentView(sheetView);
        dialog.setDismissWithAnimation(true);
        sheetView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        ((TextView)sheetView.findViewById(R.id.installTitle)).setText(activity.getString(R.string.install_latest, ver, type));

        sheetView.findViewById(R.id.btnInstall).setOnClickListener(v -> {
            if (Shell.rootAccess()) {
                if (hasStoragePM(activity)) {
                    dialog.dismiss();
                    Intent intent = new Intent(activity, InstallActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("install", true);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Grant storage access and press button again", Toast.LENGTH_SHORT).show();
                    requestPM(activity);
                }
            } else {
                Toast.makeText(activity, "Grant root access and press button again", Toast.LENGTH_SHORT).show();
            }
        });

        sheetView.findViewById(R.id.btnDownload).setOnClickListener(v -> {
            if (Shell.rootAccess()) {
                if (hasStoragePM(activity)) {
                    dialog.dismiss();
                    Intent intent = new Intent(activity, InstallActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("install", false);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Grant storage access and press button again", Toast.LENGTH_SHORT).show();
                    requestPM(activity);
                }
            } else {
                Toast.makeText(activity, "Grant Root Access and press button again", Toast.LENGTH_SHORT).show();
            }
        });

        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        sheetView.setY(size.y);
        dialog.show();

        sheetView.animate()
                .setInterpolator(vars.intr)
                .setDuration(600)
                .setStartDelay(200)
                .setStartDelay(100)
                .translationY(0);
    }

    public static boolean hasStoragePM(Activity activity){
        return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPM(Activity activity){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            activity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
}
