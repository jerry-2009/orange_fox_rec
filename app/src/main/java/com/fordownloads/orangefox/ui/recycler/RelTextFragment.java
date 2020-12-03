package com.fordownloads.orangefox.ui.recycler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.R;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;

public class RelTextFragment extends Fragment {
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
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String param = getArguments().getString(KEY_PARAM);

        TextView pageText = (TextView) view.findViewById(R.id.multiTextView);
        pageText.setText(param);

    }
}