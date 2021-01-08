package com.fordownloads.orangefox.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.thefuntasty.hauler.HaulerView;

import java.util.List;

public class PatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        setSupportActionBar(findViewById(R.id.appToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.mode3).setOnClickListener(v -> dots(3));
        findViewById(R.id.mode4).setOnClickListener(v -> dots(4));
        findViewById(R.id.mode5).setOnClickListener(v -> dots(5));

        dots(3);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // we need to recreate pattern because it can't dynamically change dot count
    public void dots(int dots) {
        LinearLayout patternPlace = findViewById(R.id.patternPlace);
        patternPlace.removeAllViews();

        PatternLockView decryptPattern = new PatternLockView(this);
        decryptPattern.setDotCount(dots);

        decryptPattern.addPatternLockListener(new PatternLockViewListener() {
            @Override public void onStarted() { }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) { }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                int patternSize = pattern.size();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < patternSize; i++) {
                    PatternLockView.Dot dot = pattern.get(i);
                    stringBuilder.append(consts.PATTERN_SYMBOLS[(dot.getRow() * decryptPattern.getDotCount() + dot.getColumn())]);
                }

                setResult(Activity.RESULT_OK, new Intent().putExtra("pass", stringBuilder.toString()));
                finish();
            }

            @Override
            public void onCleared() { }
        });

        patternPlace.addView(decryptPattern);
    }
}