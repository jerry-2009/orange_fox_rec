package com.fordownloads.orangefox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.ui.recycler.AdapterStorage;
import com.fordownloads.orangefox.ui.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.ui.recycler.RecyclerItems;
import com.fordownloads.orangefox.ui.recycler.RecyclerFragment;
import com.fordownloads.orangefox.ui.recycler.TextFragment;
import com.fordownloads.orangefox.ui.Tools;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecyclerActivity extends AppCompatActivity {
    public static String releaseIntent = null;
    public static boolean releaseJSON = false;
    List<RecyclerItems> items = new ArrayList<>();
    FrameLayout _loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);

        Intent intent = getIntent();
        releaseIntent = intent.getStringExtra("release");
        _loadingView = findViewById(R.id.loadingLayout);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        switch (intent.getIntExtra("type", 0)) {
            case 0: //release info
                if (intent.hasExtra("isJSON")) {
                    releaseJSON = true;
                    _loadingView.setVisibility(View.GONE);
                    getAllReleaseInfo();
                } else {
                    new Thread(this::getAllReleaseInfo).start();
                }
                break;
            case 1: //release list
                new Thread(this::getReleases).start();
                break;
            case 2: //device selection
                break;
        }
    }

    private void getAllReleaseInfo() {
        JSONObject release;
        try {

            if (releaseJSON) {
                release = new JSONObject(releaseIntent);
            } else {
                Map<String, Object> response = API.request("releases/" + releaseIntent);
                runOnUiThread(() -> errorHandler(response));
                release = new JSONObject((String)response.get("response"));
            }

            String buildType = release.getString("build_type");
            switch(buildType){
                case "stable":
                    buildType = getString(R.string.rel_stable);
                    break;
                case "beta":
                    buildType = getString(R.string.rel_beta);
                    break;
            }

            items.add(new RecyclerItems(getString(R.string.rel_type), buildType, R.drawable.ic_outline_build_24));
            items.add(new RecyclerItems(getString(R.string.rel_vers), release.getString("version"), R.drawable.ic_outline_new_releases_24));
            items.add(new RecyclerItems(getString(R.string.rel_name), release.getString("file_name"), R.drawable.ic_outline_archive_24));
            items.add(new RecyclerItems(getString(R.string.rel_date), release.getString("date"), R.drawable.ic_outline_today_24));
            items.add(new RecyclerItems(getString(R.string.rel_size), release.getString("size_human"), R.drawable.ic_outline_sd_card_24));
            items.add(new RecyclerItems("MD5", release.getString("md5"), R.drawable.ic_outline_verified_user_24));

            final JSONObject finalRelease = release;
            runOnUiThread(() -> {
                findViewById(R.id.installThis).setOnClickListener(view -> {
                    setResult(Activity.RESULT_OK, new Intent().putExtra("release", finalRelease.toString()));
                    finish();
                });

                findViewById(R.id.installBtnLayout).setVisibility(View.VISIBLE);

                FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
                pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, items))));

                try {
                    if (finalRelease.has("changelog"))
                        pageList.add(R.string.rel_changes, TextFragment.class,
                                TextFragment.arguments(finalRelease.getString("changelog")));
                    if (finalRelease.has("notes"))
                        pageList.add(R.string.rel_notes, TextFragment.class,
                                TextFragment.arguments(finalRelease.getString("notes")));
                    if (finalRelease.has("bugs"))
                            pageList.add(R.string.rel_bugs, TextFragment.class,
                                    TextFragment.arguments(finalRelease.getString("bugs")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
                viewPagerTab.setViewPager(viewPager);

                if(!releaseJSON)
                    _loadingView.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    _loadingView.setVisibility(View.GONE);
                                }
                            });
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Tools.dialogFinish(this, R.string.err_json));
        }
    }

    private void errorHandler(Map<String, Object> response){
        if (!(boolean) response.get("success")) {
            int code = (int) response.get("code");
            switch (code) {
                case 404:
                case 500:
                    Tools.dialogFinish(this, R.string.err_no_rel);
                    break;
                case 0:
                    Tools.dialogFinish(this, R.string.err_no_internet);
                    break;
                default:
                    Tools.dialogFinish(this, getString(R.string.err_response, code));
                    break;
            }
        }
    }

    private List<RecyclerItems> addReleaseItems(String name, JSONObject node) throws JSONException {
        List<RecyclerItems> array = new ArrayList<>();
        JSONArray arrayRel = node.getJSONArray(name);
        for (int i = 0; i < arrayRel.length(); i++) {
            JSONObject release = arrayRel.getJSONObject(i);
            array.add(new RecyclerItems(release.getString("version"), release.getString("date"), R.drawable.ic_device));
        }
        return array;
    }

    private void getReleases() {
        try {
            Map<String, Object> response = API.request("device/" + releaseIntent + "/releases");
            runOnUiThread(() -> errorHandler(response));

            if(!(boolean)response.get("success"))
                return;

            JSONObject releases = new JSONObject((String)response.get("response"));

            FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);

            if (releases.has("stable"))
                pageList.add(R.string.rel_stable, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, addReleaseItems("stable", releases)))));
            if (releases.has("beta"))
                pageList.add(R.string.rel_beta, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, addReleaseItems("beta", releases)))));

            runOnUiThread(() -> {
                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
                viewPagerTab.setViewPager(viewPager);

                _loadingView.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                _loadingView.setVisibility(View.GONE);
                            }
                        });
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Tools.dialogFinish(this, R.string.err_json));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}