package com.fordownloads.orangefox.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.fordownloads.orangefox.R;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;
import org.jetbrains.annotations.NotNull;

public class TextFragment extends Fragment {
    public static Bundle arguments(String param) {
        return new Bundler().putString("text", param).get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView)view.findViewById(R.id.multiTextView)).setText(Html.fromHtml(getArguments().getString("text"), Html.FROM_HTML_MODE_COMPACT));
    }
}