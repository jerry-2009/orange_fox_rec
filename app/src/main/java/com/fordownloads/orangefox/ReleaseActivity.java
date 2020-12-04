package com.fordownloads.orangefox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.ui.recycler.DataAdapterRel;
import com.fordownloads.orangefox.ui.recycler.ItemRel;
import com.fordownloads.orangefox.ui.recycler.RelInfoFragment;
import com.fordownloads.orangefox.ui.recycler.RelTextFragment;
import com.fordownloads.orangefox.ui.tools;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fordownloads.orangefox.App.setDataAdapterInfo;

public class ReleaseActivity extends AppCompatActivity {
    public static String releaseId = null;
    List<ItemRel> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        App.setContext(this);

        Intent intent = getIntent();
        releaseId = intent.getStringExtra("releaseId");

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.installThis).setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("releaseId", releaseId);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        new Thread(this::getAllReleaseInfo).start();
    }

    private void getAllReleaseInfo() {
        try {
            Map<String, Object> response = api.request("releases/" + releaseId);

            if (!(boolean)response.get("success")) {
                int code = (int)response.get("code");
                switch (code){
                    case 404:
                    case 500:
                        runOnUiThread(() -> tools.dialogFinish((Activity) App.getContext(), R.string.err_no_rel));
                        break;
                    case 0:
                        runOnUiThread(() -> tools.dialogFinish((Activity) App.getContext(), R.string.err_no_internet));
                        break;
                    default:
                        runOnUiThread(() -> tools.dialogFinish((Activity) App.getContext(), getString(R.string.err_response, code)));
                        break;
                }
                return;
            }
            JSONObject release = new JSONObject((String)response.get("response"));
            String buildType = release.getString("build_type");
            switch(buildType){
                case "stable":
                    buildType = getString(R.string.rel_stable);
                    break;
                case "beta":
                    buildType = getString(R.string.rel_beta);
                    break;
            }

            items.add(new ItemRel (getString(R.string.rel_type), buildType, R.drawable.ic_outline_build_24));
            items.add(new ItemRel (getString(R.string.rel_vers), release.getString("version"), R.drawable.ic_outline_new_releases_24));
            items.add(new ItemRel (getString(R.string.rel_name), release.getString("file_name"), R.drawable.ic_outline_archive_24));
            items.add(new ItemRel (getString(R.string.rel_date), release.getString("date"), R.drawable.ic_outline_today_24));
            items.add(new ItemRel (getString(R.string.rel_size), release.getString("size_human"), R.drawable.ic_outline_sd_card_24));
            items.add(new ItemRel ("MD5", release.getString("md5"), R.drawable.ic_outline_verified_user_24));

            runOnUiThread(() -> {
                FragmentPagerItems.Creator pageList = FragmentPagerItems.with(App.getContext());

                DataAdapterRel adapter = new DataAdapterRel(App.getContext(), items);
                setDataAdapterInfo(adapter); //см. App.java

                pageList.add(R.string.rel_info, RelInfoFragment.class);

                try {
                    if (release.has("changelog"))
                        pageList.add(R.string.rel_changes, RelTextFragment.class,
                                RelTextFragment.arguments(release.getString("changelog")));
                    if (release.has("notes"))
                        pageList.add(R.string.rel_notes, RelTextFragment.class,
                                RelTextFragment.arguments(release.getString("notes")));
                    if (release.has("bugs"))
                            pageList.add(R.string.rel_bugs, RelTextFragment.class,
                                    RelTextFragment.arguments(release.getString("bugs")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                FragmentPagerItemAdapter fragAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), pageList.create());

                ViewPager viewPager = findViewById(R.id.viewpager);
                viewPager.setAdapter(fragAdapter);

                SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
                viewPagerTab.setViewPager(viewPager);


                FrameLayout loadingView = findViewById(R.id.loadingLayout);
                loadingView.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingView.setVisibility(View.GONE);
                            }
                        });
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> tools.dialogFinish((Activity)App.getContext(), R.string.err_json));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}