package com.fordownloads.orangefox.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.service.DownloadService;
import com.fordownloads.orangefox.consts;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class Install {

    public static BottomSheetDialog dialog(Activity activity, String ver, String type, String url, String md5, String name, boolean noExistsCheck, Activity actToFinish) {
        String fileName = URLUtil.guessFileName(url, null, "application/zip");
        File finalFile = new File(consts.DOWNLOAD_DIR, fileName);
        boolean exist = !noExistsCheck && finalFile.exists() && hasStoragePM(activity);

        View sheetView = activity.getLayoutInflater().inflate(exist ? R.layout.dialog_exists : R.layout.dialog_install, null);
        ((TextView)sheetView.findViewById(R.id.installTitle)).setText(activity.getString(R.string.install_latest, ver, type));

        BottomSheetDialog dialog = Tools.initBottomSheet(activity, sheetView);

        if (exist) {
            sheetView.findViewById(R.id.btnInstall).setOnClickListener(v -> {
                if (Shell.rootAccess())
                    if (MD5.checkMD5(md5, finalFile)) {
                        if (!Shell.su(
                                "echo \"install /sdcard/Fox/releases/" + fileName + "\" > " + Tools.getORS())
                                .exec().isSuccess()) {
                            Tools.showSnackbar(activity, null, R.string.err_ors_short, dialog).show();
                            return;
                        }
                        if (!Shell.su("reboot recovery").exec().isSuccess())
                            Tools.showSnackbar(activity, null, R.string.err_reboot_notify, dialog).show();
                    } else {
                        Tools.showSnackbar(activity, null, R.string.err_md5_wrong_exists, dialog).show();
                    }
                else
                    Tools.showSnackbar(activity, null, R.string.err_no_pm_root, dialog).show();
            });
            sheetView.findViewById(R.id.btnDownload).setOnClickListener (v -> {
                dialog.dismiss();
                ((App) activity.getApplication()).setDialogToDismiss(dialog(activity, ver, type, url, md5, name, true, actToFinish));
            });
            sheetView.findViewById(R.id.btnDelete).setOnClickListener (v -> {
                if (!finalFile.delete())
                    Tools.showSnackbar(activity, null, R.string.err_file_delete, dialog).show();
                else
                    dialog.dismiss();
            });
        } else {
            View.OnClickListener proceed = v -> {
                if (v.getId() == R.id.btnDownload || Shell.rootAccess())
                    if (hasStoragePM(activity)) {
                        if (((App)activity.getApplication()).isDownloadSrvRunning())
                            Tools.showSnackbar(activity, null, R.string.err_service_running, dialog).show();
                        else {
                            Intent intent = new Intent(activity, DownloadService.class)
                                    .putExtra("md5", md5)
                                    .putExtra("url", url)
                                    .putExtra("version", ver)
                                    .putExtra("name", name)
                                    .putExtra("install", v.getId() == R.id.btnInstall);
                            finishIfNotNull(actToFinish);
                            Tools.showSnackbar(activity, null, R.string.inst_bg_download, dialog).show();
                            activity.startService(intent);
                        }
                    } else {
                        Tools.showSnackbar(activity, null, R.string.err_no_pm_storage, dialog)
                                .setAction(R.string.setup, view -> requestPM(activity)).show();
                    }
                else
                    Tools.showSnackbar(activity, null, R.string.err_no_pm_root, dialog).show();
            };

            sheetView.findViewById(R.id.btnInstall).setOnClickListener(proceed);
            sheetView.findViewById(R.id.btnDownload).setOnClickListener(proceed);
        }

        dialog.show();
        sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);

        return dialog;
    }

    public static  void finishIfNotNull(Activity actToFinish){
        if (actToFinish != null) {
            actToFinish.setResult(Activity.RESULT_OK, null);
            actToFinish.finish();
        }
    }

    public static boolean hasStoragePM(Activity activity){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    Environment.isExternalStorageManager();
        }
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

    public static void requestPM(Fragment fragment){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            fragment.requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            fragment.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            Toast.makeText(fragment.getActivity(), R.string.help_android11_pm, Toast.LENGTH_LONG).show();
        }
    }
}
