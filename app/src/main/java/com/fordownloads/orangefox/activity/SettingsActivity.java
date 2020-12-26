package com.fordownloads.orangefox.activity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.activity.RecyclerActivity;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.ui.Tools;
import com.fordownloads.orangefox.vars;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsFragment, new SettingsFragment())
                    .commit();
        }
        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_settings);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        Preference _device, _updater;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            JobScheduler mScheduler = (JobScheduler)getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
            _device = findPreference("change_device");

            prefs.edit().putBoolean(pref.UPDATES_ENABLE, mScheduler.getPendingJob(vars.SCHEDULER_JOB_ID) != null).apply();

            if (prefs.contains(pref.DEVICE_CODE))
                _device.setSummary(prefs.getString(pref.DEVICE_CODE, "Error"));

            _device.setOnPreferenceClickListener((p) -> {
                Intent intent = new Intent(getContext(), RecyclerActivity.class);
                intent.putExtra("type", 3);
                intent.putExtra("title", R.string.dev_activity);
                startActivityForResult(intent, 202);
                return true;
            });

            findPreference(pref.UPDATES_ENABLE).setOnPreferenceClickListener((p) -> {
                if (prefs.getBoolean(pref.UPDATES_ENABLE, false)) {
                    jobSchedule(prefs, mScheduler);
                } else {
                    mScheduler.cancelAll();
                }
                return true;
            });

            findPreference(pref.UPDATES_LIMITED).setOnPreferenceClickListener((p) -> {
                mScheduler.cancelAll();
                jobSchedule(prefs, mScheduler);
                return true;
            });
        }

        private void jobSchedule(SharedPreferences prefs, JobScheduler mScheduler) {
            if (!Tools.scheduleJob(getActivity(), mScheduler, prefs.getBoolean(pref.UPDATES_LIMITED, true) ? JobInfo.NETWORK_TYPE_NOT_ROAMING : JobInfo.NETWORK_TYPE_UNMETERED))
                prefs.edit().putBoolean(pref.UPDATES_ENABLE, false).apply();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 202) {
                if (resultCode == RESULT_OK && data != null) {
                    _device.setSummary(data.getStringExtra("codename"));
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }
            }
        }
    }
}