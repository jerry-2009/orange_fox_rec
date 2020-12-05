package com.fordownloads.orangefox.ui.recycler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;

import org.jetbrains.annotations.NotNull;

public class RelListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rel_info, container, false);
    }

    private static final String KEY_PARAM = "key_param";

    public static Bundle arguments(String param) {
        return new Bundler()
                .putString(KEY_PARAM, param)
                .get();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String param = getArguments().getString(KEY_PARAM);

        switch (param) {
            case "stable":
                ((RecyclerView)view.findViewById(R.id.releaseRecycler)).setAdapter(App.getDataAdapterStable());
                break;
            case "beta":
                ((RecyclerView)view.findViewById(R.id.releaseRecycler)).setAdapter(App.getDataAdapterBeta());
                break;
        }

    }
}