package com.fordownloads.orangefox.ui.scripts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;

public class ScriptsFragment extends Fragment {

    private ScriptsViewModel scriptsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(App.getContext(), R.color.fox_status_solid_bg));
        return inflater.inflate(R.layout.fragment_scripts, container, false);
    }
}