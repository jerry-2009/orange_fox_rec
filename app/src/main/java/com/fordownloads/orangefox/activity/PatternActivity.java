package com.fordownloads.orangefox.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.script_decrypt_pattern);


        findViewById(R.id.mode3).setOnClickListener(v -> setPattern(View.VISIBLE, View.GONE, View.GONE));
        findViewById(R.id.mode4).setOnClickListener(v -> setPattern(View.GONE, View.VISIBLE, View.GONE));
        findViewById(R.id.mode5).setOnClickListener(v -> setPattern(View.GONE, View.GONE, View.VISIBLE));

        PatternLockViewListener listener = new PatternLockViewListener() {
            @Override public void onStarted() { }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) { }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                int patternSize = pattern.size();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < patternSize; i++) {
                    PatternLockView.Dot dot = pattern.get(i);
                    stringBuilder.append(consts.PATTERN_SYMBOLS[(dot.getRow() * 3 + dot.getColumn())]);
                }

                setResult(Activity.RESULT_OK, new Intent().putExtra("pass", stringBuilder.toString()));
                finish();
            }

            @Override
            public void onCleared() { }
        };
        ((PatternLockView)findViewById(R.id.pattern3)).addPatternLockListener(listener);
        ((PatternLockView)findViewById(R.id.pattern4)).addPatternLockListener(listener);
        ((PatternLockView)findViewById(R.id.pattern5)).addPatternLockListener(listener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void setPattern(int three, int four, int five) {
        findViewById(R.id.pattern3).setVisibility(three);
        findViewById(R.id.pattern4).setVisibility(four);
        findViewById(R.id.pattern5).setVisibility(five);
    }
}