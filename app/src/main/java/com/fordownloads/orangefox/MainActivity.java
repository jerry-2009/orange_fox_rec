package com.fordownloads.orangefox;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10));
    }


    String appDir = "";
    int progrTest = 0;


    FloatingActionButton _download;
    TextView _deviceName;
    View _progressLayout;
    ProgressBar _progressBar;
    TextView _progressText;
    Button _test;
    String guessCodename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        /* Init basic things */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.setContext(this);

        /* Init variables */
        appDir = getApplicationInfo().dataDir;

        /* Init widgets */
        _download = findViewById(R.id.download);
        _deviceName = findViewById(R.id.deviceName);
        _progressLayout = findViewById(R.id.progressLayout);
        _progressBar = findViewById(R.id.progressBar);
        _progressText = findViewById(R.id.progressText);
        _test = findViewById(R.id.test);

        /* Etc */
        String buildDevice = Build.DEVICE.toLowerCase();

        if(!su("echo hello")) {
            _progressLayout.setVisibility(View.VISIBLE);
            _download.setVisibility(View.GONE);
            _progressText.setText("Root error");
        }

        try {
            JSONArray devices = new JSONArray(api.request("device"));
            for (int i=0; i < devices.length(); i++)
            {
                    JSONObject device = devices.getJSONObject(i);
                    String checkDevice = device.getString("codename");
                    if (checkDevice.toLowerCase().contains(buildDevice)) {
                        _deviceName.setText(device.getString("fullname"));
                        guessCodename = checkDevice;
                        _download.setVisibility(View.VISIBLE);
                    }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_LONG).show();
        }

        /* Init PRDownloader */
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        /* Add click listeners */
        _download.setOnClickListener(view -> {
            try {
                downloadFile();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        _test.setOnClickListener(view -> {
            progrTest += 10;
            _progressText.setText(progrTest + "%");
            _progressBar.setProgress(progrTest);
            _progressLayout.setVisibility(View.VISIBLE);

        });
    }

    public boolean su(String cmd){
        return Shell.su(cmd).exec().isSuccess();
    }

    public void downloadFile() throws JSONException {
        View rootView = findViewById(android.R.id.content).getRootView();
/*
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(rootView, "Okok", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            Snackbar.make(rootView, "Error", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }*/

        JSONObject release = new JSONObject(api.request("device/"+ guessCodename +"/releases/last"));

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_NEGATIVE)
                return;

            _progressLayout.setVisibility(View.VISIBLE);
            _download.setVisibility(View.GONE);
            String downloadUrl = null;
            String sizeHuman = "";
            try {
                downloadUrl = release.getString("url");
                sizeHuman = release.getString("size_human");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String finalSizeHuman = sizeHuman;
            int downloadId = PRDownloader.download(downloadUrl, appDir, "OFupdate.zip")
                    .build()
                    .setOnProgressListener(progress -> {
                        _progressBar.setMax((int)progress.totalBytes);
                        _progressText.setText(progress.currentBytes / 1048576  + "MB of " + finalSizeHuman);
                        _progressBar.setProgress((int)progress.currentBytes);
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            su("cp \"" + appDir + "/OFupdate.zip\" /sdcard/OFupdate.zip && echo \"install /sdcard/OFupdate.zip\" > /cache/recovery/openrecoveryscript && reboot recovery");
                            _progressLayout.setVisibility(View.GONE);
                            _download.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Error error) {
                            Snackbar.make(rootView, "Download error", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Install " + release.getString("version") + " for " + release.getString("codename")).setPositiveButton("Yep", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}