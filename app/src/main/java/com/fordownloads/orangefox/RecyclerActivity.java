package com.fordownloads.orangefox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecyclerActivity extends AppCompatActivity {
    public static String releaseIntent = null;
    public static boolean releaseJSON = false;
    List<RecyclerItems> items = new ArrayList<>();
    FrameLayout _loadingView;

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("isRestarted", true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);

        Intent intent = getIntent();

        // Т.к. я заебался и не знаю, как сохранить RecyclerView, то просто уничтожаем активити и создаем новую
        // recreate() все равно сохраняет состояние, поэтому исп. это:
        if (savedInstanceState != null && savedInstanceState.getBoolean("isRestarted")) {
            overridePendingTransition(0, 0);
            finish();
            startActivity(intent);
        }

        releaseIntent = intent.getStringExtra("release");
        _loadingView = findViewById(R.id.loadingLayout);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(intent.getIntExtra("title", R.string.app_name));

        switch (intent.getIntExtra("type", 0)) {
            case 0: //release info (URL)
                releaseJSON = false;
                new Thread(this::getAllReleaseInfo).start();
                break;
            case 1: //release info (JSON)
                releaseJSON = true;
                _loadingView.setVisibility(View.GONE);
                getAllReleaseInfo();
                break;
            case 2: //release list
                new Thread(this::getReleases).start();
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
                runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_rel));
                if(!(boolean)response.get("success"))
                    return;
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

                findViewById(R.id.installThis).setVisibility(View.VISIBLE);

                FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
                pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, items, null))));

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

    private List<RecyclerItems> addReleaseItems(String name, JSONObject node) throws JSONException {
        List<RecyclerItems> array = new ArrayList<>();
        JSONArray arrayRel = node.getJSONArray(name);
        for (int i = 0; i < arrayRel.length(); i++) {
            JSONObject release = arrayRel.getJSONObject(i);
            array.add(new RecyclerItems(release.getString("version"), release.getString("date"), R.drawable.ic_outline_archive_24, release.getString("_id")));
        }
        return array;
    }

    private void selectRelease(final View view, List<RecyclerItems> list) {
        int itemPosition = ((RecyclerView)findViewById(R.id.releaseRecycler)).getChildLayoutPosition(view);
        RecyclerItems item = list.get(itemPosition);
        Intent intent = new Intent(this, RecyclerActivity.class);
        intent.putExtra("release", list.get(itemPosition).getId());
        intent.putExtra("type", 0);
        intent.putExtra("title", R.string.rel_activity);
        startActivityForResult(intent, 201);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 201 && resultCode == RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, new Intent().putExtra("release", data.getStringExtra("release")));
            finish();
        }
    }

    private void getReleases() {
        try {
            Map<String, Object> response = API.request("device/" + releaseIntent + "/releases");
            runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_rel));

            if(!(boolean)response.get("success"))
                return;

            JSONObject releases = new JSONObject((String)response.get("response"));

            FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);

            if (releases.has("stable")) {
                List<RecyclerItems> list = addReleaseItems("stable", releases);
                pageList.add(R.string.rel_stable, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(
                        new RecyclerAdapter(this, list, (final View view) -> selectRelease(view, list)
                ))));
            }
            if (releases.has("beta")) {
                List<RecyclerItems> list = addReleaseItems("beta", releases);
                pageList.add(R.string.rel_beta, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(
                        new RecyclerAdapter(this, list, (final View view) -> selectRelease(view, list)
                ))));
            }

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