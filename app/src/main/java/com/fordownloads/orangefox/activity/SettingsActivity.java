package com.fordownloads.orangefox.activity;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
import com.thefuntasty.hauler.HaulerView;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        boolean isAbout = getIntent().getBooleanExtra("about", false);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsFragment,
                            isAbout ? new AboutFragment() : new SettingsFragment())
                    .commit();
        }

        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        if (isAbout) {
            ab.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_backspace_24);
            myToolbar.setElevation(0);
            myToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.fox_status_solid_bg));
            ((HaulerView)findViewById(R.id.haulerView)).getRootView().setBackgroundColor(ContextCompat.getColor(this, R.color.fox_status_solid_bg));
            ab.setTitle("");
        } else {
            ab.setTitle(R.string.activity_settings);
            float originalElevation = myToolbar.getElevation();

            ((HaulerView)findViewById(R.id.haulerView)).setOnDragActivityListener((offset, v1) -> {
                if (offset <= 15 && offset >= -15) {
                    myToolbar.setElevation(originalElevation-(Math.abs(offset)/15*originalElevation));
                    myToolbar.setAlpha(1);
                } else if (offset >= -50 && offset <= 50) {
                    myToolbar.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                    myToolbar.setElevation(0);
                } else {
                    myToolbar.setAlpha(0);
                    myToolbar.setElevation(0);
                }
            });
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        JobScheduler mScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        prefs.edit().putBoolean(pref.UPDATES_ENABLE, mScheduler.getPendingJob(consts.SCHEDULER_JOB_ID) != null).apply();

        ((HaulerView)findViewById(R.id.haulerView)).setOnDragDismissedListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class AboutFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.about, rootKey);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        Preference _device, _system, _dark, _upd_1, _upd_2, _upd_3, _upd, _pm;

        @Override
        public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
            if (Install.hasStoragePM(getActivity())) {
                _pm.setVisible(false);
                _upd.setEnabled(true);
            }
        }

        public void updateTogglesState(boolean state) {
            _upd_1.setVisible(state);
            _upd_2.setVisible(state);
            _upd_3.setVisible(state);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            JobScheduler mScheduler = (JobScheduler)getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
            _device = findPreference("change_device");
            _system = findPreference(pref.THEME_SYSTEM);
            _dark = findPreference(pref.THEME_DARK);
            _upd_1 = findPreference(pref.UPDATES_LIMITED);
            _upd_2 = findPreference(pref.UPDATES_INSTALL);
            _upd_3 = findPreference(pref.UPDATES_BETA);
            _upd = findPreference(pref.UPDATES_ENABLE);
            _pm = findPreference("pm");

            _dark.setVisible(!prefs.getBoolean(pref.THEME_SYSTEM, true));
            updateTogglesState(prefs.getBoolean(pref.UPDATES_ENABLE, false));

            if (Install.hasStoragePM(getActivity()))
                _pm.setVisible(false);
            else
                _upd.setEnabled(false);

            if (prefs.contains(pref.DEVICE_CODE))
                _device.setSummary(prefs.getString(pref.DEVICE_CODE, "Error"));
            else {
                _upd.setVisible(false);
                _pm.setVisible(false);
            }

            _pm.setOnPreferenceClickListener((p) -> {
                if (Shell.rootAccess())
                    if (Install.hasStoragePM(getActivity())) {
                        _pm.setVisible(false);
                        _upd.setEnabled(true);
                    } else
                        Install.requestPM(this);
                else
                    Toast.makeText(getActivity(), R.string.err_no_pm_root, Toast.LENGTH_LONG).show();
                return true;
            });

            _device.setOnPreferenceClickListener((p) -> {
                Intent intent = new Intent(getContext(), RecyclerActivity.class);
                intent.putExtra("type", 3);
                intent.putExtra("arrow", true);
                intent.putExtra("title", R.string.dev_activity);
                startActivityForResult(intent, 202);
                return true;
            });

            _upd.setOnPreferenceChangeListener((p, val) -> {
                if ((boolean)val && !Shell.rootAccess()) {
                    _pm.setVisible(true);
                    _upd.setEnabled(false);
                    return false;
                }
                mScheduler.cancelAll();
                if ((boolean)val) {
                    if (jobSchedule(prefs, mScheduler))
                        updateTogglesState(true);
                    else
                        return false;
                } else
                    updateTogglesState(false);
                return true;
            });

            Preference.OnPreferenceClickListener theme = (p) -> {
                if (prefs.getBoolean(pref.THEME_SYSTEM, true)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    _dark.setVisible(false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(
                            prefs.getBoolean(pref.THEME_DARK, false) ?
                                    AppCompatDelegate.MODE_NIGHT_YES :
                                    AppCompatDelegate.MODE_NIGHT_NO);
                    _dark.setVisible(true);
                }
                return true;
            };

            _system.setOnPreferenceClickListener(theme);
            _dark.setOnPreferenceClickListener(theme);

            findPreference(pref.UPDATES_LIMITED).setOnPreferenceClickListener((p) -> {
                mScheduler.cancelAll();
                jobSchedule(prefs, mScheduler);
                return true;
            });
        }

        private boolean jobSchedule(SharedPreferences prefs, JobScheduler mScheduler) {
            return Tools.scheduleJob(getActivity(), mScheduler, prefs.getBoolean(pref.UPDATES_LIMITED, true) ? JobInfo.NETWORK_TYPE_NOT_ROAMING : JobInfo.NETWORK_TYPE_UNMETERED);
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