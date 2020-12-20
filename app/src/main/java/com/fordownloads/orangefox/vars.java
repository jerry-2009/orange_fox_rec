package com.fordownloads.orangefox;

import android.os.Environment;

import androidx.core.view.animation.PathInterpolatorCompat;

import java.io.File;

public class vars {
    public static final android.view.animation.Interpolator intr = PathInterpolatorCompat.create(0.16f, 1, 0.3f, 1);
    public static final File updateZip = new File(Environment.getExternalStorageDirectory(), "Fox");
    public static final String CHANNEL_UPDATE = "CHANNEL_UPDATE";
    public static final String CHANNEL_DOWNLOAD = "CHANNEL_DOWNLOAD";
    public static final int NOTIFY_NEW_UPD = 1000;
    public static final int NOTIFY_DOWNLOAD_BG = 2000;
    public static final int NOTIFY_DOWNLOAD_FG = 3000;
    public static final int NOTIFY_DOWNLOAD_SAVED = 4000;
    public static final int NOTIFY_REBOOT_PENDING = 5000;
    public static final int NOTIFY_DOWNLOAD_ERROR = 6000;
}
