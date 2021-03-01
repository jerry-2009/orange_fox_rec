package com.fordownloads.orangefox.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.utils.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LogViewActivity extends AppCompatActivity {
    TextView _textView;
    NestedScrollView _scroll;
    String fileName;
    File log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        fileName = getIntent().getStringExtra("file_name");
        log = new File(Environment.getExternalStorageDirectory(), "Fox/logs/" + fileName);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(fileName.split("\\.")[0]);

        float originalElevation = ab.getElevation();
        _textView = findViewById(R.id.log);
        _scroll = findViewById(R.id.scroll);

        findViewById(R.id.updownFAB).setOnClickListener(this::scroll);
        new Thread(this::showLog).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete)
            if (!log.delete()) {
                Tools.showSnackbar(this, findViewById(R.id.updownFAB), R.string.err_file_delete).show();
            } else {
                setResult(Activity.RESULT_OK);
                finish();
            }
        else if (item.getItemId() == R.id.share)
            Tools.share(this, fileName, log);
        return super.onOptionsItemSelected(item);
    }

    private void scroll(View v) {
        if (_scroll.getScrollY() == 0)
            _scroll.smoothScrollTo(0, _textView.getHeight(), 500);
        else
            _scroll.smoothScrollTo(0, 0, 500);
    }

    private void showLog() {
        if (!log.exists()) {
            setText(getString(R.string.err_file_notfound));
            return;
        }

        try {
            if (log.getAbsolutePath().endsWith(".log"))
                setText(String.join("\n", Files.readAllLines(log.toPath())) + "\n");
            else if (log.getAbsolutePath().endsWith(".zip"))
                try (ZipFile zipFile = new ZipFile(log)) {
                    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    final ZipEntry entry = entries.nextElement();
                    InputStream input = zipFile.getInputStream(entry);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                        String line = reader.readLine();
                        StringBuilder finalStr = new StringBuilder();
                        while (line != null) {
                            finalStr.append(line).append("\n");
                            line = reader.readLine();
                        }
                        setText(finalStr.toString());
                    }
                }
            else
                setText(getString(R.string.err_file_format));
        } catch (Exception e) {
            setText(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void setText(String text) {
        FrameLayout _loadingView = findViewById(R.id.loadingLayout);

        runOnUiThread(() -> {
            _loadingView.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            _loadingView.setVisibility(View.GONE);
                        }
                    });
            _textView.setText(text);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}