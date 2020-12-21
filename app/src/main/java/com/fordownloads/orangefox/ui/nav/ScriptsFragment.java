package com.fordownloads.orangefox.ui.nav;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

import com.fordownloads.orangefox.ActionReceiver;
import com.fordownloads.orangefox.InstallActivity;
import com.fordownloads.orangefox.MainActivity;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.RecyclerActivity;
import com.fordownloads.orangefox.UpdateReceiver;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.vars;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import static android.content.Context.ALARM_SERVICE;
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
            Intent intent = new Intent(getActivity(), UpdateReceiver.class)
                    .setAction("com.fordownloads.orangefox.Update");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0, intent, 0);

            AlarmManager alarmManager = (AlarmManager)getActivity().getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    10000, pendingIntent);

            //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(10000), pendingIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
        });
        v.findViewById(R.id.startService2).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), UpdateReceiver.class)
                    .setAction("com.fordownloads.orangefox.Update");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0, intent, 0);

            AlarmManager alarmManager = (AlarmManager)getActivity().getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        });
    }
}