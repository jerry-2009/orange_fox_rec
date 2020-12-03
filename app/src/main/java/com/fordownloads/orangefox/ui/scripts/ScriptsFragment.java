package com.fordownloads.orangefox.ui.scripts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.R;

public class ScriptsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scripts, container, false);
    }
}