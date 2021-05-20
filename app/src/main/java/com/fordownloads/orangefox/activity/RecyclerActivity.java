package com.fordownloads.orangefox.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.fragments.RecyclerFragment;
import com.fordownloads.orangefox.fragments.TextFragment;
import com.fordownloads.orangefox.recycler.AdapterStorage;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.utils.API;
import com.fordownloads.orangefox.utils.APIResponse;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
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

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class RecyclerActivity extends AppCompatActivity {
    public static String intentData = null;
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

        Intent intent = getIntent();
        int type = intent.getIntExtra("type", 0);

        if (type == 3)
            setContentView(R.layout.activity_device);
        else
            setContentView(R.layout.activity_recycler);

        if (savedInstanceState != null && savedInstanceState.getBoolean("isRestarted")) {
            overridePendingTransition(0, 0);
            finish();
            startActivity(intent);
        }

        intentData = intent.getStringExtra("release");
        _loadingView = findViewById(R.id.loadingLayout);
        _fab = findViewById(R.id.installButton);

        viewPagerTab = findViewById(R.id.viewpagertab);
        CardView tabCard = findViewById(R.id.viewpagertabElevation);

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        if (intent.getBooleanExtra("arrow", false))
            ab.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_backspace_24);
        ab.setTitle(intent.getIntExtra("title", R.string.app_name));

        float originalElevation = type == 3 ? myToolbar.getElevation() : tabCard.getElevation();

        ((HaulerView)findViewById(R.id.haulerView)).setOnDragDismissedListener(v -> finish());

        if (type == 3)
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

        switch (type) {
            case 0: //release info (URL)
                new Thread(() -> getAllReleaseInfo(false, false)).start();
                break;
            case 1: //release info (JSON)
                _loadingView.setVisibility(View.GONE);
                getAllReleaseInfo(false, true);
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
            case 5: //find release by device and version
                new Thread(() -> getAllReleaseInfo(true, false)).start();
                break;
        }
    }

    public void errorHandler(int code, int customErr){
        runOnUiThread(() -> {
            findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.btnClose).setOnClickListener(v -> finish());
            TextView _text = findViewById(R.id.errorText);

            switch (code) {
                case 404:
                case 500:
                case 422:
                    _text.setText(customErr);
                    break;
                case 1000:
                    _text.setText(R.string.err_json);
                    break;
                case 0:
                    ((TextView)findViewById(R.id.errorTitle)).setText(R.string.err_card_no_internet);
                    ((ImageView)findViewById(R.id.errorIcon)).setImageResource(R.drawable.ic_round_public_off_24);
                    break;
                default:
                    _text.setText(getString(R.string.err_response, code));
                    break;
            }
        });
    }

    private void getAllReleaseInfo(boolean releaseFind, boolean releaseJSON) {
        JSONObject release;
        try {
            if (releaseFind) {
                APIResponse findResponse = API.request("releases/?device_id="+getIntent().getStringExtra("device")+"&version="+ intentData +"&sort=date_desc");
                if(!findResponse.success) {
                    Sentry.captureMessage("getAllReleaseInfo() can't find release: "+findResponse.toString(), SentryLevel.ERROR);
                    errorHandler(findResponse.code, R.string.err_no_rel);
                    return;
                }
                intentData = new JSONObject(findResponse.data).getJSONArray("data").getJSONObject(0).getString("_id");
            }

            if (releaseJSON) {
                release = new JSONObject(intentData);
            } else {
                APIResponse response = API.request("releases/get?_id=" + intentData);
                if(!response.success) {
                    Sentry.captureMessage("getAllReleaseInfo() can't find release by id: "+response.toString(), SentryLevel.ERROR);
                    errorHandler(response.code, R.string.err_no_rel);
                    return;
                }
                release = new JSONObject(response.data);
            }

            final String md5 = release.getString("md5");
            final String name = release.getString("filename");
            final String version = release.getString("version");
            final String url = release.getJSONObject("mirrors").getString("DL");
            final String stringBuildType = Tools.getBuildType(this, release);

            items.add(new RecyclerItems(getString(R.string.rel_type), stringBuildType, R.drawable.ic_outline_build_24));
            items.add(new RecyclerItems(getString(R.string.rel_vers), release.getString("version"), R.drawable.ic_outline_new_releases_24));
            items.add(new RecyclerItems(getString(R.string.rel_name), name, R.drawable.ic_outline_archive_24));
            items.add(new RecyclerItems(getString(R.string.rel_date), Tools.formatDate(release.getLong("date")), R.drawable.ic_outline_today_24));
            items.add(new RecyclerItems(getString(R.string.rel_size), Tools.formatSize(this, release.getInt("size")), R.drawable.ic_outline_sd_card_24));
            items.add(new RecyclerItems("MD5", release.getString("md5"), R.drawable.ic_outline_verified_user_24));

            runOnUiThread(() -> {
                findViewById(R.id.installButton).setOnClickListener(view -> {
                    if (((App)getApplication()).isDownloadSrvRunning())
                        Tools.showSnackbar(this, findViewById(R.id.installButton), R.string.err_service_running).show();
                    else
                        instDialog = Install.dialog(this, version, stringBuildType, url, md5, name, false, this);
                });

                FragmentPagerItems.Creator pageList = FragmentPagerItems.with(this);
                pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(this, items))));

                try {
                    if (release.has("changelog") && !release.isNull("changelog"))
                        pageList.add(R.string.rel_changes, TextFragment.class,
                                TextFragment.arguments(Tools.buildList(release, "changelog"), true));
                    if (!release.isNull("notes"))
                        pageList.add(R.string.rel_notes, TextFragment.class,
                                TextFragment.arguments(release.getString("notes"), false));
                    if (!release.isNull("bugs"))
                            pageList.add(R.string.rel_bugs, TextFragment.class,
                                    TextFragment.arguments(Tools.buildList(release, "bugs"), true));
                } catch (JSONException e) {
                    Tools.reportException(e);
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
            Tools.reportException(e);
            errorHandler(1000, 0);
        }
    }

    private void getDeviceInfo() {
        JSONObject device;//, maintainer;
        try {
            APIResponse response = API.request("devices/get?_id=" + new JSONObject(intentData).getString("_id"));
            if(!response.success) {
                errorHandler(response.code, R.string.err_ise);
                Sentry.captureMessage("getDeviceInfo() failed: "+response.toString(), SentryLevel.ERROR);
                return;
            }
            device = new JSONObject(response.data);

            /*APIResponse responseMnt = API.request("users/maintainers/get?_id=" + device.getJSONObject("maintainer").getString("_id"));

            Log.e("OFR", "response: "+responseMnt.code);

            if(!responseMnt.success) {
                errorHandler(responseMnt.code, R.string.err_ise);
                return;
            }
            maintainer = new JSONObject(responseMnt.data);*/

            items.add(new RecyclerItems(getString(R.string.dev_model), device.getString("full_name"), R.drawable.ic_device));
            items.add(new RecyclerItems(getString(R.string.dev_code), device.getString("codename"), R.drawable.ic_round_code_24));
            items.add(new RecyclerItems(getString(R.string.dev_maintainer), device.getJSONObject("maintainer").getString("username"), R.drawable.ic_outline_person_24));
//maintainer.getString("name")
            if (device.getBoolean("supported"))
                items.add(new RecyclerItems(getString(R.string.dev_status), getString(R.string.dev_maintained), R.drawable.ic_round_check_24));
            else
                items.add(new RecyclerItems(getString(R.string.dev_status), getString(R.string.dev_unmaintained), R.drawable.ic_round_close_24));
/*
            if (!maintainer.isNull("telegram") && !maintainer.getJSONObject("telegram").isNull("url"))
                items.add(new RecyclerItems(getString(R.string.dev_telegram),
                        maintainer.getJSONObject("telegram").getString("username"),
                        R.drawable.ic_tg, maintainer.getJSONObject("telegram").getString("url")));*/

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
            Tools.reportException(e);
            errorHandler(1000, 0);
        }
    }

    private void openTg(View view, List<RecyclerItems> items) {
        int itemPosition = ((RecyclerView)findViewById(R.id.releaseRecycler)).getChildLayoutPosition(view);
        String id = items.get(itemPosition).getData();
        if (id != null)
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(id)));
            } catch (Exception e) {
                Tools.reportException(e, true);
            }
    }

    private List<RecyclerItems> addReleaseItems(APIResponse response) throws JSONException {
        List<RecyclerItems> array = new ArrayList<>();
        JSONArray arrayRel = new JSONObject(response.data).getJSONArray("data");
        for (int i = 0; i < arrayRel.length(); i++) {
            JSONObject release = arrayRel.getJSONObject(i);
            array.add(new RecyclerItems(release.getString("version"),
                    Tools.formatDate(release.getLong("date")), R.drawable.ic_outline_archive_24,
                    release.getString("_id")));
        }
        return array;
    }
    private boolean parseReleaseByType(FragmentPagerItems.Creator pageList, String type, int name) throws JSONException {
        APIResponse response = API.request("releases/?type="+type+"&codename=" + intentData);
        if (response.success) {
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
        intent.putExtra("release", list.get(itemPosition).getData());
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
            APIResponse responseStable = API.request("releases/?type=stable&codename=" + intentData);

            if (parseReleaseByType(pageList, "stable", R.string.rel_stable) &
                    parseReleaseByType(pageList, "beta", R.string.rel_beta)) {
                errorHandler(responseStable.code, R.string.err_no_rels);
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
            Tools.reportException(e);
            errorHandler(1000, 0);
        }
    }

    private void getDevices() {
        try {
            APIResponse response = API.request("devices");

            if(!response.success) {
                Sentry.captureMessage("getDevices() failed: "+response.toString(), SentryLevel.ERROR);
                errorHandler(response.code, R.string.err_ise);
                return;
            }

            JSONArray devices = new JSONObject(response.data).getJSONArray("data");

            List<RecyclerItems> array = new ArrayList<>();

            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                array.add(new RecyclerItems(device.getString("full_name"), device.getString("codename"), R.drawable.ic_device));
            }
            RecyclerAdapter adapter = new RecyclerAdapter(this, array, (final View view) -> selectDevice(view, array));

            FragmentPagerItems pageList = FragmentPagerItems.with(this)
                    .add("dev", RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(adapter)))
                    .create();

            runOnUiThread(() -> {
                ViewPager pager = findViewById(R.id.viewpager);
                OverScrollDecoratorHelper.setUpOverScroll(pager);
                pager.setAdapter(new FragmentPagerItemAdapter(getSupportFragmentManager(), pageList));

                ((EditText)findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
                    @Override public void afterTextChanged(Editable s) {}
                    @Override public void beforeTextChanged(CharSequence f, int u, int c, int k) {}
                    @Override
                    public void onTextChanged(CharSequence q, int start, int before, int count) {
                        adapter.filter(q);
                    }
                });

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
            Tools.reportException(e);
            errorHandler(1000, 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}