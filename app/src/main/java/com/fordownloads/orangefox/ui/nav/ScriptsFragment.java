package com.fordownloads.orangefox.ui.nav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.InstallActivity;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.RecyclerActivity;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.vars;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ScriptsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scripts, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.bnav_scripts);
        testing(rootView);
        return rootView;
    }

    public void testing(View v) {

        v.findViewById(R.id.startService).setOnClickListener(view -> {
        });
    }
}