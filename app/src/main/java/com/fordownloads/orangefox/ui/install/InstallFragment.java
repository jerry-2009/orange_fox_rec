package com.fordownloads.orangefox.ui.install;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ReleaseActivity;

import static android.app.Activity.RESULT_OK;

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
            intent.putExtra("release", "{\"_id\":\"5f2081a094a8ed5b894cae9f\",\"bugs\":\"* Aroma Gapps not working\",\"build_type\":\"stable\",\"changelog\":\"* Fixed critical bug with mounting system on Android Pie\\r\\n* Bug fixes and improvements when working with files\",\"codename\":\"x00t\",\"date\":\"Tue, 28 Jul 2020 19:50:03 GMT\",\"file_name\":\"OrangeFox-R11.0_1-Stable-X00T.zip\",\"md5\":\"f69c18140d164c5ef936eeb0f4be91fb\",\"notes\":\"This build will decrypt all ROMs starting from April (maybe later) to July. Tested on Android 10 and 9 ROMs.\\r\\nThanks to @SaurabhCharde for his device tree\",\"size_bytes\":46464080,\"size_human\":\"44MB\",\"unixtime\":1595965803,\"url\":\"https://files.orangefox.tech/OrangeFox-Stable/x00t/OrangeFox-R11.0_1-Stable-X00T.zip\",\"version\":\"R11.0_1\"}");
            intent.putExtra("isJSON", true);
            intent.putExtra("type", 0);
            startActivityForResult(intent, 200);
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Toast.makeText(getActivity().getApplicationContext(), "releaseId" +
                    data.getStringExtra("release"),
                    Toast.LENGTH_LONG).show();
        }
    }
}