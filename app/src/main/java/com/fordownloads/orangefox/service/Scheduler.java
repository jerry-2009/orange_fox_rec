package com.fordownloads.orangefox.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.fordownloads.orangefox.utils.API;

public class Scheduler extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("OFR-JOB", "Job checking updates in background...");
        new Thread(() -> API.findUpdate(this)).start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
