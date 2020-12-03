package com.fordownloads.orangefox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
import com.fordownloads.orangefox.ui.tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReleaseActivity extends AppCompatActivity {
    List<ItemRel> items = new ArrayList<>();

    String apiCall = null;

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

        new Thread(this::getAllReleaseInfo).start();
    }

    private void getAllReleaseInfo() {
        try {
            Map<String, Object> response = api.request(apiCall);

            if (!(boolean)response.get("success")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tools.dialogFinish((Activity)App.getContext(), R.string.err_response);
                    }
                });
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RecyclerView recyclerView = findViewById(R.id.releaseRecycler);
                    ProgressBar spinner = findViewById(R.id.spinner);
                    DataAdapterRel adapter = new DataAdapterRel(App.getContext(), items);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tools.dialogFinish((Activity)App.getContext(), R.string.err_json);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}