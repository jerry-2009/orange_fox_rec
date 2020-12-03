package com.fordownloads.orangefox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fordownloads.orangefox.ui.install.InstallFragment;
import com.fordownloads.orangefox.ui.recycler.DataAdapterRel;
import com.fordownloads.orangefox.ui.recycler.ItemRel;
import com.fordownloads.orangefox.ui.recycler.RelInfoFragment;
import com.fordownloads.orangefox.ui.scripts.ScriptsFragment;
import com.fordownloads.orangefox.ui.settings.SettingsFragment;
import com.fordownloads.orangefox.ui.tools;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReleaseActivity extends AppCompatActivity {
    public static String apiCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        App.setContext(this);

        Intent intent = getIntent();
        apiCall = intent.getStringExtra("apiCall");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.rel_changes, RelInfoFragment.class)
                .add(R.string.rel_notes, ScriptsFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}