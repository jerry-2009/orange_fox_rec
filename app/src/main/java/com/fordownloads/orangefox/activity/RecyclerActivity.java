package com.fordownloads.orangefox.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.utils.Tools;
import com.fordownloads.orangefox.recycler.AdapterStorage;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.fragments.RecyclerFragment;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.fragments.TextFragment;
import com.fordownloads.orangefox.utils.API;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.thefuntasty.hauler.HaulerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class RecyclerActivity extends AppCompatActivity {
    public static String releaseIntent = null;
    public static boolean releaseJSON = false;
    List<RecyclerItems> items = new ArrayList<>();
    SmartTabLayout viewPagerTab;
    FrameLayout _loadingView;
    ExtendedFloatingActionButton _fab;
    BottomSheetDialog instDialog;

    @Override
    protected void onSaveInstanceState(@NotNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("isRestarted", true);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        if (instDialog != null) instDialog.dismiss();
        ((App) getApplication()).dismissDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        Intent intent = getIntent();

        if (savedInstanceState != null && savedInstanceState.getBoolean("isRestarted")) {
            overridePendingTransition(0, 0);
            finish();
            startActivity(intent);
        }

        releaseIntent = intent.getStringExtra("release");
        _loadingView = findViewById(R.id.loadingLayout);
        _fab = findViewById(R.id.installThis);

        viewPagerTab = findViewById(R.id.viewpagertab);
        CardView tabCard = findViewById(R.id.viewpagertabElevation);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        if (intent.getBooleanExtra("arrow", false))
            ab.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_backspace_24);
        ab.setTitle(intent.getIntExtra("title", R.string.app_name));

        float originalElevation = tabCard.getElevation();

        ((HaulerView)findViewById(R.id.haulerView)).setOnDragDismissedListener(v -> finish());

        if (intent.getIntExtra("type", 0) == 3)
            ((HaulerView)findViewById(R.id.haulerView)).setOnDragActivityListener((offset, v1) -> {
                if (offset <= 15 && offset >= -15) {
                    myToolbar.setElevation(originalElevation-(Math.abs(offset)/15*originalElevation));
                    myToolbar.setAlpha(1);
                } else if (offset >= -50 && offset <= 50) {
                    myToolbar.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                    myToolbar.setElevation(0);
                } else {
                    myToolbar.setAlpha(0);
                    myToolbar.setElevation(0);
                }
            });
        else
            ((HaulerView)findViewById(R.id.haulerView)).setOnDragActivityListener((offset, v1) -> {
                if (offset <= 15 && offset >= -15) {
                    tabCard.setElevation(originalElevation-(Math.abs(offset)/15*originalElevation));
                    tabCard.setAlpha(1);
                    myToolbar.setAlpha(1);
                } else if (offset >= -50 && offset <= 50) {
                    myToolbar.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                    tabCard.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                    tabCard.setElevation(0);
                } else {
                    tabCard.setAlpha(0);
                    myToolbar.setAlpha(0);
                    tabCard.setElevation(0);
                }
            });


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
            case 3: //device list
                new Thread(this::getDevices).start();
                break;
            case 4: //device info
                new Thread(this::getDeviceInfo).start();
                break;
        }
    }

    private void getAllReleaseInfo() {
        JSONObject release;
        try {

            if (releaseJSON) {
                release = new JSONObject(releaseIntent);
            } else {
                Map<String, Object> response = API.request("releases/get?_id=" + releaseIntent);
                if(!(boolean)response.get("success")) {
                    runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_rel));
                    return;
                }
                release = new JSONObject((String)response.get("response"));
            }

            final String md5 = release.getString("md5");
            final String version = release.getString("version");
            final String url = release.getJSONObject("mirrors").getString("DL");
            final String stringBuildType = Tools.getBuildType(this, release);

            items.add(new RecyclerItems(getString(R.string.rel_type), stringBuildType, R.drawable.ic_outline_build_24));
            items.add(new RecyclerItems(getString(R.string.rel_vers), release.getString("version"), R.drawable.ic_outline_new_releases_24));

            if (release.has("filename"))
                items.add(new RecyclerItems(getString(R.string.rel_name), release.getString("filename"), R.drawable.ic_outline_archive_24));

            items.add(new RecyclerItems(getString(R.string.rel_date), Tools.formatDate(release.getLong("date")), R.drawable.ic_outline_today_24));
            items.add(new RecyclerItems(getString(R.string.rel_size), Tools.formatSize(this, release.getInt("size")), R.drawable.ic_outline_sd_card_24));
            items.add(new RecyclerItems("MD5", release.getString("md5"), R.drawable.ic_outline_verified_user_24));

            runOnUiThread(() -> {
                findViewById(R.id.installThis).setOnClickListener(view -> {
                    if (((App)getApplication()).isDownloadSrvRunning())
                        Tools.showSnackbar(this, findViewById(R.id.installThis), R.string.err_service_running).show();
                    else
                        instDialog = Install.dialog(this, version, stringBuildType, url, md5, false, this);
                });

                FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
                pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, items, null))));

                try {
                    if (release.has("changelog"))
                        pageList.add(R.string.rel_changes, TextFragment.class,
                                TextFragment.arguments(Tools.buildList(release, "changelog"), true));
                    if (!release.isNull("notes"))
                        pageList.add(R.string.rel_notes, TextFragment.class,
                                TextFragment.arguments(release.getString("notes"), false));
                    if (release.has("bugs"))
                            pageList.add(R.string.rel_bugs, TextFragment.class,
                                    TextFragment.arguments(Tools.buildList(release, "bugs"), true));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                OverScrollDecoratorHelper.setUpOverScroll(viewPager);
                viewPagerTab.setViewPager(viewPager);

                if(releaseJSON)
                    _fab.show();
                else
                    _loadingView.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    _loadingView.setVisibility(View.GONE);
                                    _fab.show();
                                }
                            });
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Tools.dialogFinish(this, R.string.err_json));
        }
    }

    private void getDeviceInfo() {
        JSONObject device, maintainer;
        try {
            Map<String, Object> response = API.request("devices/get?_id=" + new JSONObject(releaseIntent).getString("_id"));
            if(!(boolean)response.get("success")) {
                runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_dev));
                return;
            }
            device = new JSONObject((String)response.get("response"));

            Map<String, Object> responseMnt = API.request("users/maintainers/get?_id=" + device.getJSONObject("maintainer").getString("_id"));
            if(!(boolean)responseMnt.get("success")) {
                runOnUiThread(() -> API.errorHandler(this, responseMnt, R.string.err_no_dev));
                return;
            }
            maintainer = new JSONObject((String)responseMnt.get("response"));

            items.add(new RecyclerItems(getString(R.string.dev_model), device.getString("full_name"), R.drawable.ic_device));
            items.add(new RecyclerItems(getString(R.string.dev_code), device.getString("codename"), R.drawable.ic_round_code_24));
            items.add(new RecyclerItems(getString(R.string.dev_maintainer), maintainer.getString("name"), R.drawable.ic_outline_person_24));

            items.add(new RecyclerItems(getString(R.string.dev_status), getString(device.getBoolean("supported") ?
                    R.string.dev_maintained : R.string.dev_unmaintained), R.drawable.ic_round_check_24));

            if (!maintainer.isNull("telegram") && !maintainer.getJSONObject("telegram").isNull("url"))
                items.add(new RecyclerItems(getString(R.string.dev_telegram),
                        maintainer.getJSONObject("telegram").getString("username"),
                        R.drawable.ic_tg, maintainer.getJSONObject("telegram").getString("url")));

            FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
            pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, items, (final View view) -> openTg(view, items)))));
            if (!device.isNull("notes"))
                pageList.add(R.string.rel_notes, TextFragment.class,
                        TextFragment.arguments(device.getString("notes"), false));

            runOnUiThread(() -> {
                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                OverScrollDecoratorHelper.setUpOverScroll(viewPager);
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

    private void openTg(View view, List<RecyclerItems> items) {
        int itemPosition = ((RecyclerView)findViewById(R.id.releaseRecycler)).getChildLayoutPosition(view);
        String id = items.get(itemPosition).getId();
        if (id != null)
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(id)));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private List<RecyclerItems> addReleaseItems(Map<String, Object> response) throws JSONException {
        List<RecyclerItems> array = new ArrayList<>();
        JSONArray arrayRel = new JSONObject((String)response.get("response")).getJSONArray("data");
        for (int i = 0; i < arrayRel.length(); i++) {
            JSONObject release = arrayRel.getJSONObject(i);
            array.add(new RecyclerItems(release.getString("version"),
                    Tools.formatDate(release.getLong("date")), R.drawable.ic_outline_archive_24,
                    release.getString("_id")));
        }
        return array;
    }
    private boolean parseReleaseByType(FragmentPagerItems.Creator pageList, String type, int name) throws JSONException {
        Map<String, Object> response = API.request("releases/?type="+type+"&codename=" + releaseIntent);
        if ((boolean) response.get("success")) {
            List<RecyclerItems> list = addReleaseItems(response);
            pageList.add(name, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(
                    new RecyclerAdapter(this, list, (final View view) -> selectRelease(view, list)
                    ))));
            return false;
        } else {
            return true;
        }
    }

    private void selectRelease(final View view, List<RecyclerItems> list) {
        int itemPosition = ((RecyclerView)findViewById(R.id.releaseRecycler)).getChildLayoutPosition(view);
        Intent intent = new Intent(this, RecyclerActivity.class);
        intent.putExtra("release", list.get(itemPosition).getId());
        intent.putExtra("type", 0);
        intent.putExtra("arrow", true);
        intent.putExtra("title", R.string.rel_activity);
        startActivityForResult(intent, 201);
    }

    private void selectDevice(final View view, List<RecyclerItems> list) {
        int itemPosition = ((RecyclerView)findViewById(R.id.releaseRecycler)).getChildLayoutPosition(view);
        setResult(Activity.RESULT_OK, new Intent().putExtra("codename", list.get(itemPosition).getSubtitle()));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 201 && resultCode == RESULT_OK)
            finish();
    }

    private void getReleases() {
        try {
            FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
            Map<String, Object> responseStable = API.request("releases/?type=stable&codename=" + releaseIntent);

            if (parseReleaseByType(pageList, "stable", R.string.rel_stable) &
                    parseReleaseByType(pageList, "beta", R.string.rel_beta)) {
                runOnUiThread(() -> API.errorHandler(this, responseStable, R.string.err_no_rel));
                return;
            }

            runOnUiThread(() -> {
                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                OverScrollDecoratorHelper.setUpOverScroll(viewPager);
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

    private void getDevices() {
        try {
            Map<String, Object> response = API.request("devices");

            if(!(boolean)response.get("success")) {
                runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_device));
                return;
            }

            JSONArray devices = new JSONObject((String)response.get("response")).getJSONArray("data");

            FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
            List<RecyclerItems> array = new ArrayList<>();

            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                array.add(new RecyclerItems(device.getString("full_name"), device.getString("codename"), R.drawable.ic_device));
            }

            pageList.add("dev", RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(
                    new RecyclerAdapter(this, array, (final View view) -> selectDevice(view, array)
                    ))));

            runOnUiThread(() -> {
                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager pager = findViewById(R.id.viewpager);
                OverScrollDecoratorHelper.setUpOverScroll(pager);
                pager.setAdapter(fragAdapter);
                ViewPager.MarginLayoutParams p = (ViewPager.MarginLayoutParams) pager.getLayoutParams();
                p.setMargins(0, (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 56,
                        getResources().getDisplayMetrics()),0,0);
                pager.requestLayout();

                findViewById(R.id.viewpagerlayout).setVisibility(View.GONE);
                getSupportActionBar().setElevation(getResources().getDimension(R.dimen.elevation_medium));

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