package com.fordownloads.orangefox.ui.nav;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.ActionReceiver;

import static android.content.Context.ALARM_SERVICE;

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
            Intent intent = new Intent(getActivity(), ActionReceiver.class)
                    .setAction("com.fordownloads.orangefox.Update");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0, intent, 0);

            AlarmManager alarmManager = (AlarmManager)getActivity().getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    10000, pendingIntent);
        });
        v.findViewById(R.id.startService2).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ActionReceiver.class)
                    .setAction("com.fordownloads.orangefox.Update");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0, intent, 0);

            AlarmManager alarmManager = (AlarmManager)getActivity().getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        });
    }
}