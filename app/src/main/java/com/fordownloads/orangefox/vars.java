package com.fordownloads.orangefox;

import android.os.Environment;

import androidx.core.view.animation.PathInterpolatorCompat;

import java.io.File;

public class vars {
    public static final android.view.animation.Interpolator intr = PathInterpolatorCompat.create(0.16f, 1, 0.3f, 1);
    public static final String updateZip = new File(Environment.getExternalStorageDirectory(), "Fox/OFupdate.zip").getAbsolutePath();
}
