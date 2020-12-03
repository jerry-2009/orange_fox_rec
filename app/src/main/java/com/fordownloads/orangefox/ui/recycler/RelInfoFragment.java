package com.fordownloads.orangefox.ui.recycler;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ReleaseActivity;
import com.fordownloads.orangefox.api;
import com.fordownloads.orangefox.ui.tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelInfoFragment extends Fragment {
    List<ItemRel> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rel_info, container, false);
        new Thread(this::getAllReleaseInfo).start();
        return rootView;
    }

    private void getAllReleaseInfo() {
        try {
            Map<String, Object> response = api.request(ReleaseActivity.apiCall);

            if (!(boolean)response.get("success")) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tools.dialogFinish((Activity) App.getContext(), R.string.err_response);
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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RecyclerView recyclerView = getActivity().findViewById(R.id.releaseRecycler);
                    ProgressBar spinner = getActivity().findViewById(R.id.spinner);
                    DataAdapterRel adapter = new DataAdapterRel(App.getContext(), items);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tools.dialogFinish((Activity)App.getContext(), R.string.err_json);
                }
            });
        }
    }
}