package com.fordownloads.orangefox.ui.recycler;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ReleaseActivity;
import com.fordownloads.orangefox.api;
import com.fordownloads.orangefox.ui.tools;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelInfoFragment extends Fragment {
    private static final String KEY_PARAM = "key_param";

    public static RelTextFragment newInstance(String param) {
        RelTextFragment f = new RelTextFragment();
        f.setArguments(arguments(param));
        return f;
    }

    public static Bundle arguments(String param) {
        return new Bundler()
                .putString(KEY_PARAM, param)
                .get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rel_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
/*
        String param = getArguments().getString(KEY_PARAM);

        TextView pageText = (TextView) view.findViewById(R.id.multiTextView);
        pageText.setText(param);*/

    }

}