package com.fordownloads.orangefox.ui.nav;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.RecyclerActivity;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.vars;

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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), vars.CHANNEL_UPDATE)
                    .setSmallIcon(R.drawable.ic_fox_white)
                    .setContentTitle("My notification")
                    .setContentText("Much longer text that cannot fit one line...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Much longer text that cannot fit one line...\nALOALOWelwkeowkeowedwedwewew ewe we we we wewewewewew we wewe we weew ewe"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

            notificationManager.notify(vars.NOTIFY_NEW_UPD, builder.build());
        });
    }
}