package com.fordownloads.orangefox;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.fordownloads.orangefox.ui.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.topjohnwu.superuser.Shell;


public class Install {

    public static void dialog(Activity activity, String ver, String type, String url, String md5) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.ThemeBottomSheet);

        View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_install, null);
        dialog.setContentView(sheetView);
        dialog.setDismissWithAnimation(true);
        sheetView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        ((TextView)sheetView.findViewById(R.id.installTitle)).setText(activity.getString(R.string.install_latest, ver, type));

        View.OnClickListener proceed = v -> {
            if (v.getId() == R.id.btnDownload || Shell.rootAccess())
                if (hasStoragePM(activity)) {
                    dialog.dismiss();
                    Intent intent = new Intent(activity, InstallActivity.class)
                        .putExtra("md5", md5)
                        .putExtra("url", url)
                        .putExtra("version", ver)
                        .putExtra("install", v.getId() == R.id.btnInstall);
                    activity.startActivity(intent);
                } else {
                    Tools.showSnackbar(activity, sheetView, R.string.err_no_pm_storage)
                            .setAction(R.string.setup, view -> requestPM(activity)).show();
                }
            else
                Tools.showSnackbar(activity, sheetView, R.string.err_no_pm_root).show();
        };

        sheetView.findViewById(R.id.btnInstall).setOnClickListener(proceed);
        sheetView.findViewById(R.id.btnDownload).setOnClickListener(proceed);

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
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            activity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            Toast.makeText(activity, R.string.help_android11_pm, Toast.LENGTH_LONG).show();
        }
    }
}
