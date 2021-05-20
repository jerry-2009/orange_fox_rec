package com.fordownloads.orangefox.activity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.thefuntasty.hauler.HaulerView;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

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
            findViewById(R.id.haulerView).getRootView().setBackgroundColor(ContextCompat.getColor(this, R.color.fox_status_solid_bg));
            ab.setTitle("");
            findViewById(R.id.feedback).setVisibility(View.GONE);
        } else {
            ab.setTitle(R.string.activity_settings);
            float originalElevation = myToolbar.getElevation();

            ((HaulerView) findViewById(R.id.haulerView)).setOnDragActivityListener((offset, v1) -> {
                if (offset <= 15 && offset >= -15) {
                    myToolbar.setElevation(originalElevation - (Math.abs(offset) / 15 * originalElevation));
                    myToolbar.setAlpha(1);
                } else if (offset >= -50 && offset <= 50) {
                    myToolbar.setAlpha(1 - ((Math.abs(offset) - 25) / 25));
                    myToolbar.setElevation(0);
                } else {
                    myToolbar.setAlpha(0);
                    myToolbar.setElevation(0);
                }
            });

            findViewById(R.id.feedback).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/OrangeFoxApp")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        JobScheduler mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        prefs.edit().putBoolean(pref.UPDATES_ENABLE, mScheduler.getPendingJob(consts.SCHEDULER_JOB_ID) != null).apply();

        ((HaulerView) findViewById(R.id.haulerView)).setOnDragDismissedListener(v -> finish());
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
        Preference _device, _system, _dark, _upd_1, _upd_2, _upd_3, _upd, _pm, _mirror;
        SharedPreferences prefs;

        @Override
        public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
            if (Install.hasStoragePM(getActivity())) {
                _pm.setVisible(false);
                _upd.setEnabled(true);
            }
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
                                                 Bundle savedInstanceState) {
            RecyclerView r = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            r.setPadding(0, 0, 0, 216);
            return r;
        }

        private void updateTogglesState(boolean state) {
            _upd_1.setVisible(state);
            _upd_2.setVisible(state);
            _upd_3.setVisible(state);
        }

        private void prefSelectDialog(Preference pr, String def, int sub, int list, int list_opts) {
            Resources res = getResources();
            String[] array = res.getStringArray(list);
            List<String> array_opts = Arrays.asList(res.getStringArray(list_opts));

            pr.setSummary(array[array_opts.indexOf(prefs.getString(pr.getKey(), def))]);

            pr.setOnPreferenceClickListener((p) -> {
                String current = prefs.getString(pr.getKey(), def);
                View sheetView = getLayoutInflater().inflate(R.layout.dialog_listbox, null);
                ListView listView = sheetView.findViewById(R.id.listView);

                BottomSheetDialog dialog = Tools.initBottomSheet(requireActivity(), sheetView);
                ((TextView) sheetView.findViewById(R.id.title)).setText(pr.getTitle());
                ((TextView) sheetView.findViewById(R.id.subtitle)).setText(sub);

                listView.setAdapter(new ArrayAdapter<String>(requireContext(), R.layout.list_radio, array) {
                    @Override
                    public View getView(int pos, View convertView, ViewGroup parent) {
                        View v = convertView;
                        if (v == null) v = getLayoutInflater().inflate(R.layout.list_radio, null);
                        RadioButton r = v.findViewById(R.id.title);
                        r.setText(array[pos]);
                        r.setChecked(array_opts.get(pos).equals(current));
                        r.setOnClickListener(view -> {
                            prefs.edit().putString(pr.getKey(), array_opts.get(pos)).apply();
                            pr.setSummary(array[pos]);
                            dialog.dismiss();
                        });
                        return v;
                    }
                });

                dialog.show();
                sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);
                return true;
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            JobScheduler mScheduler = (JobScheduler) requireActivity().getSystemService(JOB_SCHEDULER_SERVICE);
            _device = findPreference("change_device");
            _system = findPreference(pref.THEME_SYSTEM);
            _dark = findPreference(pref.THEME_DARK);
            _upd_1 = findPreference(pref.UPDATES_LIMITED);
            _upd_2 = findPreference(pref.UPDATES_INSTALL);
            _upd_3 = findPreference(pref.UPDATES_BETA);
            _upd = findPreference(pref.UPDATES_ENABLE);
            _pm = findPreference("pm");
            _mirror = findPreference(pref.MIRROR);

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

            prefSelectDialog(_mirror, "DL", R.string.pref_mirror_desc, R.array.mirrors, R.array.mirrors_opts);

            _upd.setOnPreferenceChangeListener((p, val) -> {
                if ((boolean) val && !Shell.rootAccess()) {
                    _pm.setVisible(true);
                    _upd.setEnabled(false);
                    return false;
                }
                mScheduler.cancelAll();
                if ((boolean) val) {
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
            return Tools.scheduleJob(requireActivity(), mScheduler, prefs.getBoolean(pref.UPDATES_LIMITED, true) ? JobInfo.NETWORK_TYPE_NOT_ROAMING : JobInfo.NETWORK_TYPE_UNMETERED);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 202) {
                if (resultCode == RESULT_OK && data != null) {
                    _device.setSummary(data.getStringExtra("codename"));
                    requireActivity().setResult(Activity.RESULT_OK, data);
                    requireActivity().finish();
                }
            }
        }
    }
}