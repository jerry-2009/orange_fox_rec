package com.fordownloads.orangefox.ui.install;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;

import org.json.JSONException;

public class InstallFragment extends Fragment {

    private InstallViewModel installViewModel;

    TextView _ofTitle;
    Button _installButton;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(App.getContext(), R.color.fox_accent));
        View rootView = inflater.inflate(R.layout.fragment_install, container, false);

        _ofTitle = rootView.findViewById(R.id.ofTitle);
        _installButton = rootView.findViewById(R.id.installButton);


        _installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _ofTitle.setText("Test");
            }
        });

        return rootView;
    }
}