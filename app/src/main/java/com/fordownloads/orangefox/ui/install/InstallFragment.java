package com.fordownloads.orangefox.ui.install;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ReleaseActivity;

public class InstallFragment extends Fragment {
    TextView _ofTitle;
    Button _installButton, _releaseInfo;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_install, container, false);

        _ofTitle = rootView.findViewById(R.id.ofTitle);
        _installButton = rootView.findViewById(R.id.installButton);
        _releaseInfo = rootView.findViewById(R.id.releaseInfo);

        _releaseInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ReleaseActivity.class);
            intent.putExtra("apiCall", "releases/5f2081a094a8ed5b894cae9f");
            startActivity(intent);
        });

        return rootView;
    }
}