package com.fordownloads.orangefox;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Interpolator;
import android.graphics.Point;
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
            dialog.dismiss();
            Intent intent = new Intent(activity, InstallActivity.class);
            activity.startActivity(intent);
        });

        sheetView.findViewById(R.id.btnDownload).setOnClickListener(v -> {

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

    public static void permissions(Activity activity) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.ThemeBottomSheet);

        View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_permissions, null);
        dialog.setContentView(sheetView);
        dialog.setDismissWithAnimation(true);

        CheckBox _root = sheetView.findViewById(R.id.checkRoot);
        CheckBox _storage = sheetView.findViewById(R.id.checkStorage);

        if (Shell.rootAccess())
            _root.setChecked(true);
        else
            _root.setOnClickListener(v -> {
                Shell.su("echo root").submit(res -> {
                    if (res.isSuccess()) {
                        _root.setChecked(true);
                        _root.setClickable(false);
                        if (_storage.isChecked())
                            dialog.dismiss();
                    } else {
                        Toast.makeText(activity, "Root request failed; Check Magisk Manager", Toast.LENGTH_LONG).show();
                    }
                });
            });



        if (hasStoragePM(activity))
            _storage.setChecked(true);
        else
            _storage.setOnClickListener(v -> {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    if (hasStoragePM(activity)) {
                        _storage.setChecked(true);
                        _storage.setClickable(false);
                        if (_root.isChecked())
                            dialog.dismiss();
                    } else {
                        Toast.makeText(activity, "Storage request failed; Check Settings", Toast.LENGTH_LONG).show();
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
        return activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                activity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
