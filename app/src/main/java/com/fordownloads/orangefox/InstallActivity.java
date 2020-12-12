package com.fordownloads.orangefox;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class InstallActivity extends AppCompatActivity {
    boolean install = false;
    String url;
    ExtendedFloatingActionButton _cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        CoordinatorLayout layout = findViewById(R.id.instLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        install = intent.getBooleanExtra("install", false);
        _cancel = findViewById(R.id.cancelInstallation);

        _cancel.setOnClickListener(v -> {
            PRDownloader.cancelAll();
            finish();
        });

        beginDownload();
    }

    private void beginDownload(){
        ProgressBar _progressBar = findViewById(R.id.progressBar);
        TextView _progressText = findViewById(R.id.progressText);

        if (vars.updateZip.exists())
            vars.updateZip.delete();

        PRDownloader.download(url, vars.updateZip.getAbsolutePath(), "OFupdate.zip")
                .build()
                .setOnStartOrResumeListener(() -> _progressBar.setIndeterminate(false))
                .setOnProgressListener(progress -> {
                    _progressText.setText(getString(R.string.inst_progress, progress.currentBytes / 1048576, progress.totalBytes / 1048576));
                    _progressBar.setProgress((int)(progress.currentBytes * 100 / progress.totalBytes));
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        _progressBar.setIndeterminate(true);
                        _cancel.hide();
                        if (install) {
                            new Thread(()->{
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Shell.su("echo \"install /sdcard/Fox/OFupdate.zip\" > /cache/recovery/openrecoveryscript && reboot recovery").exec();
                                finish();
                            }).start();
                            _progressText.setText(R.string.inst_installing);
                        } else {
                            _progressText.setText(getString(R.string.inst_downloaded, vars.updateZip + "/OFUpdate.zip"));
                            new Thread(()->{
                                try {
                                    Thread.sleep(6000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finish();
                            }).start();
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        _progressText.setText(R.string.inst_down_err);
                    }
                });
    }

    @Override
    public void onBackPressed() {}
}