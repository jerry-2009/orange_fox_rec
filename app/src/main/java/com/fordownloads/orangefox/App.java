package com.fordownloads.orangefox;

import android.app.Application;

import com.fordownloads.orangefox.service.DownloadService;

public class App extends Application {
    private DownloadService downloadSrv;
    public boolean isDownloadSrvRunning() {
        return downloadSrv != null;
    }
    public void setDownloadSrv(DownloadService downloadSrv) {
        this.downloadSrv = downloadSrv;
    }
}
