package com.fordownloads.orangefox;

import android.app.Application;

public class App extends Application {

    private DownloadService downloadSrv;

    public boolean isDownloadSrvRunning() {
        return downloadSrv != null;
    }
    public DownloadService getDownloadSrv() {
        return downloadSrv;
    }

    public void setDownloadSrv(DownloadService downloadSrv) {
        this.downloadSrv = downloadSrv;
    }
}
