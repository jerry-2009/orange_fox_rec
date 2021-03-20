package com.fordownloads.orangefox.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.activity.RecyclerActivity;
import com.fordownloads.orangefox.activity.SettingsActivity;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.utils.API;
import com.fordownloads.orangefox.utils.APIResponse;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

public class InstallFragment extends Fragment {
    TextView _errorText, _errorTitle;
    SharedPreferences prefs;
    ExtendedFloatingActionButton _installButton;
    View rootView, _shimmer, _shimmer2, _annoyCard;
    CardView _errorLayout, _cardInfo, _cardRelease;
    ImageView _errorIcon;
    LinearLayout _cards;
    SwipeRefreshLayout _refreshLayout;
    BottomSheetDialog devDialog = null, instDialog = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_install, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        _installButton = rootView.findViewById(R.id.installButton);

        _errorLayout = rootView.findViewById(R.id.errorLayout);
        _cardInfo = rootView.findViewById(R.id.cardInfo);
        _cardRelease = rootView.findViewById(R.id.cardRelease);

        _errorIcon = rootView.findViewById(R.id.errorIcon);
        _errorText = rootView.findViewById(R.id.errorText);
        _errorTitle = rootView.findViewById(R.id.errorTitle);
        _shimmer = rootView.findViewById(R.id.shimmer);
        _shimmer2 = rootView.findViewById(R.id.shimmer2);

        _annoyCard = rootView.findViewById(R.id.swipeableLayout);

        _installButton.hide();

        rootView.findViewById(R.id.settingsOpen).setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), SettingsActivity.class), 300));

        rootView.findViewById(R.id.releaseInfo).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.CACHE_RELEASE, "no_cache_release"));
            intent.putExtra("type", 1);
            intent.putExtra("title", R.string.rel_activity);
            startActivityForResult(intent, 200);
        });

        rootView.findViewById(R.id.deviceInfo).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.DEVICE, "no_cache_device"));
            intent.putExtra("type", 4);
            intent.putExtra("title", R.string.dev_info);
            startActivity(intent);
        });

        rootView.findViewById(R.id.oldReleases).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.DEVICE_CODE, "no_device_code"));
            intent.putExtra("type", 2);
            intent.putExtra("title", R.string.rels_activity);
            startActivityForResult(intent, 200);
        });

        rootView.findViewById(R.id.btnRefresh).setOnClickListener(view -> {
            _errorLayout.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _shimmer2.setVisibility(View.VISIBLE);
            _installButton.hide();
            prepareDevice(true);
        });

        _refreshLayout = rootView.findViewById(R.id.refreshLayout);
        _refreshLayout.setOnRefreshListener(() -> {
            _errorLayout.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _shimmer2.setVisibility(View.VISIBLE);
            _installButton.hide();
            new Thread(() -> prepareDevice( true)).start();
        });
        _refreshLayout.setEnabled(false);
        _refreshLayout.setColorSchemeResources(R.color.fox_accent);
        _refreshLayout.setProgressViewOffset(true, 64, 288);

        _cards = rootView.findViewById(R.id.cards);

        rotateUI(getResources().getConfiguration());

        prepareDevice(false);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 202) {
            if (resultCode == RESULT_OK && data != null)
                new Thread(() -> setDevice(data.getStringExtra("codename"), data.getStringExtra("full_name"), true, false)).start();
            else
                errorCard(404, R.string.err_dev_not_selected);
        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            _errorLayout.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _shimmer2.setVisibility(View.VISIBLE);
            _installButton.hide();
            new Thread(() -> setDevice(data.getStringExtra("codename"), data.getStringExtra("full_name"), true, true)).start();
        }
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        rotateUI(config);
    }

    private void rotateUI(Configuration config) {
        if (_cards == null) return;

        int _16dip = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());

        if (Tools.isLandscape(getActivity(), config, Tools.getScreenSize(getActivity()))){
            _cards.setOrientation(LinearLayout.HORIZONTAL);
            _cards.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END | LinearLayout.SHOW_DIVIDER_MIDDLE);
            _annoyCard.setPadding(_16dip, 0, _16dip, _16dip);
        } else if (requireActivity().isInMultiWindowMode() || config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            _cards.setOrientation(LinearLayout.VERTICAL);
            _cards.setShowDividers(LinearLayout.SHOW_DIVIDER_END | LinearLayout.SHOW_DIVIDER_MIDDLE);
            _annoyCard.setPadding(0, 0, 0, 0);
        }

        //Dismiss dialogs on device rotate because they has separate layouts for land/port
        if (devDialog != null) devDialog.dismiss();
        if (instDialog != null) instDialog.dismiss();

        ((App) requireActivity().getApplication()).dismissDialog();
    }

    private void prepareDevice(boolean force) {
        if (prefs.contains(pref.DEVICE) && prefs.contains(pref.DEVICE_CODE))
            new Thread(() -> parseRelease(null, force)).start();
        else
            new Thread(() -> setDevice(null, Build.MODEL,false, false)).start();
    }

    private boolean abortDevice(APIResponse response, BottomSheetDialog dialog) {
        if (!response.success) {
            if (dialog != null) dialog.dismiss();
            errorCard(response.code, R.string.err_no_rels);
            if (response.code == 404 || response.code == 500)
                prefs.edit().remove(pref.DEVICE).remove(pref.DEVICE_CODE).apply();
            return true;
        }
        return false;
    }

    private void parseRelease(BottomSheetDialog dialog, boolean force) {
        try {
            JSONObject release;
            APIResponse responseLast = API.request("releases/?limit=1&codename=" + prefs.getString(pref.DEVICE_CODE, "err"));
            if (!prefs.contains(pref.RELEASE_ID) || !responseLast.success && responseLast.code != 0)
                if (abortDevice(responseLast, dialog)) return; //no internet/cached_id

            String id = null;
            boolean useCached;
            if (responseLast.code == 200) {
                id = new JSONObject(responseLast.data)
                        .getJSONArray("data").getJSONObject(0).getString("_id");
                useCached = !force && id.equals(prefs.getString(pref.RELEASE_ID, "err"));
            } else {
                useCached = true;
            }

            if (!force && useCached && prefs.contains(pref.CACHE_RELEASE)) {
                release = new JSONObject(prefs.getString(pref.CACHE_RELEASE, null));
            } else {
                APIResponse response = API.request("releases/get?_id=" + id);
                if (abortDevice(response, dialog)) return;
                release = new JSONObject(response.data);
            }

            final String name = release.getString("filename");
            final String version = release.getString("version");
            final String url = release.getJSONObject("mirrors").getString("DL");
            final String stringBuildType = Tools.getBuildType(getActivity(), release);
            final String md5 = release.getString("md5");

            final String date = Tools.formatDate(release.getLong("date"));
            final String size = Tools.formatSize(requireActivity(), release.getInt("size"));
            JSONObject device = new JSONObject(prefs.getString(pref.DEVICE, "{}"));
            final String codename = device.getString("codename");
            final String full_name = device.getString("full_name");
            final int supported = device.getBoolean("supported") ? R.string.dev_maintained : R.string.dev_unmaintained;

            _installButton.setOnClickListener(view -> {
                if (((App) requireActivity().getApplication()).isDownloadSrvRunning())
                    Tools.showSnackbar(getActivity(), _installButton, R.string.err_service_running).show();
                else
                    instDialog = Install.dialog(requireActivity(), version, stringBuildType, url, md5, name, false, null);
            });

            requireActivity().runOnUiThread(() -> {
                ((TextView) rootView.findViewById(R.id.relType)).setText(stringBuildType);
                ((TextView) rootView.findViewById(R.id.relVers)).setText(version);
                ((TextView) rootView.findViewById(R.id.relDate)).setText(date);
                ((TextView) rootView.findViewById(R.id.relSize)).setText(size);

                ((TextView) rootView.findViewById(R.id.devCode)).setText(codename);
                ((TextView) rootView.findViewById(R.id.devModel)).setText(full_name);
                ((TextView) rootView.findViewById(R.id.devStatus)).setText(supported);
                ((TextView) rootView.findViewById(R.id.devPatch)).setText(Build.VERSION.SECURITY_PATCH);

                _installButton.setText(getString(R.string.install_latest, version, stringBuildType));
                _errorLayout.setVisibility(View.GONE);
                _shimmer.setVisibility(View.GONE);
                _shimmer2.setVisibility(View.GONE);
                _cardInfo.setVisibility(View.VISIBLE);
                _cardRelease.setVisibility(View.VISIBLE);
                _installButton.show();
                _refreshLayout.setEnabled(true);
                _refreshLayout.setRefreshing(false);

                setAnnoyCard(rootView);
            });

            if (force || !useCached || !prefs.contains(pref.CACHE_RELEASE)) {
                prefs.edit().putString(pref.CACHE_RELEASE, release.toString())
                            .putString(pref.RELEASE_ID, release.getString("_id")).apply();
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            errorCard(1000, 0);
        }
        if (dialog != null) dialog.dismiss();
    }

    private void setDevice(String codename, String deviceName, boolean skipDialog, boolean force) {
        if (codename == null)
            codename = findDevice();
        if (codename == null) {
            showDeviceDialog(Build.DEVICE, Build.MODEL, true, null);
            return;
        }
        if (codename.equals("no_internet_error"))
            return;
        APIResponse response = API.request("devices/get?codename=" + codename);
        if (!response.success) {
            errorCard(response.code, R.string.err_no_device);
            return;
        }
        if (skipDialog){
            prefs.edit().putString(pref.DEVICE, response.data).putString(pref.DEVICE_CODE, codename).apply();
            new Thread(() -> parseRelease(null, force)).start();
        } else
            showDeviceDialog(codename, deviceName, false, response.data);
    }

    protected void showDeviceDialog(String device, String deviceName, boolean fail, String cache) {
        requireActivity().runOnUiThread(() -> {
            ((AHBottomNavigation) requireActivity().findViewById(R.id.bottom_navigation)).setCurrentItem(0);

            View sheetView = getLayoutInflater().inflate(R.layout.dialog_device, null);

            devDialog = Tools.initBottomSheet(getActivity(), sheetView);

            Button gSelect = sheetView.findViewById(R.id.guessSelect);
            Button gRight = sheetView.findViewById(R.id.btnInstall);
            Button gWrong = sheetView.findViewById(R.id.btnCancel);
            TextView gCode = sheetView.findViewById(R.id.guessDeviceCode);
            TextView gName = sheetView.findViewById(R.id.guessDeviceName);
            TextView gBottom = sheetView.findViewById(R.id.guessBottomText);
            ProgressBar gProgress = sheetView.findViewById(R.id.setupProgress);

            gCode.setText(device.toUpperCase());
            gName.setText(deviceName);

            if (fail) {
                gRight.setVisibility(View.GONE);
                gWrong.setVisibility(View.GONE);
                gBottom.setText(R.string.guess_fail);
            } else
                gSelect.setVisibility(View.GONE);

            View.OnClickListener onSelectDevice = v -> {
                devDialog.setOnDismissListener(null);
                devDialog.dismiss();
                Intent intent = new Intent(getContext(), RecyclerActivity.class);
                intent.putExtra("type", 3);
                intent.putExtra("title", R.string.dev_activity);
                startActivityForResult(intent, 202);
            };

            gRight.setOnClickListener(v -> {
                devDialog.setOnDismissListener(null);
                gRight.setVisibility(View.GONE);
                gWrong.setVisibility(View.GONE);
                gProgress.setVisibility(View.VISIBLE);
                prefs.edit().putString(pref.DEVICE, cache).putString(pref.DEVICE_CODE, device).apply();
                new Thread(() -> parseRelease(devDialog, false)).start();
            });
            gWrong.setOnClickListener(onSelectDevice);
            gSelect.setOnClickListener(onSelectDevice);

            devDialog.setOnDismissListener(v -> errorCard(404, R.string.err_dev_not_selected));

            devDialog.show();
            sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);
        });
    }

    protected String findDevice() {
        String chk2 = Build.DEVICE.toLowerCase();
        String chk3 = Build.MODEL.toLowerCase();
        String chk4 = Build.PRODUCT.toLowerCase();

        if (chk2.equals("curtana") || chk2.equals("joyeuse") || chk2.equals("gram") || chk2.equals("excalibur"))
            chk2 = "miatoll";

        try {
            APIResponse response = API.request("devices");
            if(!response.success) {
                errorCard(response.code, R.string.err_ise);
                return "no_internet_error";
            }
            JSONArray devices = new JSONObject(response.data).getJSONArray("data");
            for (int i = 0; i < devices.length(); i++)
            {
                JSONObject device = devices.getJSONObject(i);
                String dbDev = device.getString("codename").toLowerCase();
                if (dbDev.contains(chk2) || dbDev.contains(chk3) || dbDev.contains(chk4))
                    return device.getString("codename");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorCard(1000, 0);
        }
        return null;
    }

    protected void errorCard(int errorCode, int customErr) {
        String text;
        switch (errorCode) {
            case 404:
            case 500:
            case 422:
                text = getString(customErr);
                break;
            case 0:
                text = getString(R.string.err_no_internet_short);
                break;
            case 1000:
                text = getString(R.string.err_json);
                break;
            default:
                text = getString(R.string.err_response, errorCode);
                break;
        }

        boolean isInternet = errorCode == 0;

        requireActivity().runOnUiThread(() -> {
            _errorTitle.setText(isInternet ? R.string.err_card_no_internet : R.string.err_card_error);
            _errorText.setText(text);
            _errorIcon.setImageResource(isInternet ? R.drawable.ic_round_public_off_24 : R.drawable.ic_round_warning_24);

            _errorLayout.setVisibility(View.VISIBLE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.GONE);
            _shimmer2.setVisibility(View.GONE);
            _refreshLayout.setRefreshing(false);
            _refreshLayout.setEnabled(true);
        });
    }

    public void setAnnoyCard(View rootView) {
        if (!prefs.getBoolean(pref.MIUI_DARK_MODE_WAS_SET, false)) {
            if (System.getProperty("ro.miui.ui.version.name", "").equals(""))
                prefs.edit().putBoolean(pref.MIUI_DARK_MODE_WAS_SET, true).apply();
            else {
                ((TextView) rootView.findViewById(R.id.annoyTitle)).setText(R.string.MIUI_dark_mode);
                ((TextView) rootView.findViewById(R.id.annoyText)).setText(R.string.MIUI_dark_mode_tap);
                rootView.findViewById(R.id.swipeCard).setOnClickListener(v -> {

                    AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());
                    alert.setTitle(R.string.MIUI_dark_mode);
                    alert.setMessage(R.string.MIUI_dark_mode_desc);
                    alert.setPositiveButton(R.string.go_settings, (dlg, num) -> {
                        _annoyCard.setVisibility(View.GONE);
                        prefs.edit().putBoolean(pref.MIUI_DARK_MODE_WAS_SET, true).apply();
                        startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        dlg.dismiss();
                    });

                    alert.setNegativeButton(R.string.show_me, (dlg, num) -> {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=DXyz9RdYvAs")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        dlg.dismiss();
                    });
                    alert.show().getWindow().setLayout(1080, 772);
                });
                _annoyCard.setVisibility(View.VISIBLE);
                return;
            }
        }

        if (!prefs.getBoolean(pref.ANNOY_ENABLE, true))
            return;

        int id;
        String[] names = getResources().getStringArray(R.array.annoy_list);

        if (Install.hasStoragePM(getActivity()) && !consts.FOXFILES_CHECK.exists() && consts.LAST_LOG.exists()) {
            id = 1;
        } else if (!prefs.getBoolean(pref.UPDATES_ENABLE, false)) {
            id = 0;
        } else {
            id = prefs.getInt(pref.DISMISSED, 1) + 1;
            if (id >= names.length) {
                _annoyCard.setVisibility(View.GONE);
                return;
            }
        }

        String[] descs = getResources().getStringArray(R.array.annoy_list_desc);
        String[] urls = getResources().getStringArray(R.array.annoy_list_url);

        ((TextView)rootView.findViewById(R.id.annoyTitle)).setText(names[id]);
        ((TextView)rootView.findViewById(R.id.annoyText)).setText(descs[id]);

        int finalId = id;
        rootView.findViewById(R.id.swipeCard).setOnClickListener(v -> {
            switch (urls[finalId]) {
                case "null":
                    break;
                case "settings":
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    break;
                default:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urls[finalId])));
                    break;
            }
            _annoyCard.setVisibility(View.GONE);
            prefs.edit().putInt(pref.DISMISSED, finalId).apply();
        });

        SwipeDismissBehavior<CardView> dismiss = new SwipeDismissBehavior<>();
        dismiss.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
        dismiss.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                _annoyCard.setVisibility(View.GONE);
                prefs.edit().putInt(pref.DISMISSED, finalId).apply();
            }
            @Override public void onDragStateChanged(int state) {}
        });

        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) rootView.findViewById(R.id.swipeCard).getLayoutParams();
        layoutParams.setBehavior(dismiss);
        _annoyCard.setVisibility(View.VISIBLE);
    }
}