package com.fordownloads.orangefox;

import android.os.Environment;

import androidx.core.view.animation.PathInterpolatorCompat;

import java.io.File;

public class consts {
    public static final android.view.animation.Interpolator intr = PathInterpolatorCompat.create(0.16f, 1, 0.3f, 1);
    public static final String ORS_FILE = "/cache/recovery/openrecoveryscript";
    public static final String DOWNLOAD_DIR = new File(Environment.getExternalStorageDirectory(), "Fox/releases").getAbsolutePath();
    public static final String CHANNEL_UPDATE = "CHANNEL_UPDATE";
    public static final String CHANNEL_DOWNLOAD = "CHANNEL_DOWNLOAD";
    public static final String CHANNEL_DOWNLOAD_STATUS = "CHANNEL_DOWNLOAD_STATUS";
    public static final int NOTIFY_NEW_UPD = 1000;
    public static final int NOTIFY_DOWNLOAD_FG = 3000;
    public static final int NOTIFY_DOWNLOAD_SAVED = 4000;
    public static final int NOTIFY_DOWNLOAD_ERROR = 5000;
    public static final int SCHEDULER_JOB_ID = 1000;
    public static final long ONE_DAY = 86400000;
}
