package com.fordownloads.orangefox.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class InstallFragment extends Fragment {
    TextView _errorText, _errorTitle;
    SharedPreferences prefs;
    ExtendedFloatingActionButton _installButton;
    View rootView, _shimmer, _shimmer2;
    CardView _errorLayout, _cardInfo, _cardRelease;
    ImageView _errorIcon;
    SwipeRefreshLayout _refreshLayout;
    BottomSheetDialog devDialog = null, instDialog = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_install, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        _installButton = rootView.findViewById(R.id.installButton);

        _errorLayout = rootView.findViewById(R.id.errorLayout);
        _cardInfo = rootView.findViewById(R.id.cardInfo);
        _cardRelease = rootView.findViewById(R.id.cardRelease);

        _errorIcon = rootView.findViewById(R.id.errorIcon);
        _errorText = rootView.findViewById(R.id.errorText);
        _errorTitle = rootView.findViewById(R.id.errorTitle);
        _shimmer = rootView.findViewById(R.id.shimmer);
        _shimmer2 = rootView.findViewById(R.id.shimmer2);

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
            prepareDevice(true);
        });

        _refreshLayout = rootView.findViewById(R.id.refreshLayout);
        _refreshLayout.setOnRefreshListener(() -> {
            _errorLayout.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _shimmer2.setVisibility(View.VISIBLE);
            new Thread(() -> prepareDevice( true)).start();
        });
        _refreshLayout.setEnabled(false);
        _refreshLayout.setColorSchemeResources(R.color.fox_accent);
        _refreshLayout.setProgressViewOffset(true, 64, 288);

        rotateUI(rootView.findViewById(R.id.cards), getResources().getConfiguration());

        prepareDevice(false);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 202) {
            if (resultCode == RESULT_OK && data != null)
                new Thread(() -> setDevice(data.getStringExtra("codename"), true, false)).start();
            else
                errorCard(404, R.string.err_dev_not_selected);
        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            _errorLayout.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _shimmer2.setVisibility(View.VISIBLE);
            _installButton.hide();
            new Thread(() -> setDevice(data.getStringExtra("codename"), true, true)).start();
        }
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        rotateUI(getActivity().findViewById(R.id.cards), config);
    }

    private void rotateUI(LinearLayout cards, Configuration config) {
        if (cards == null) return;

        if (Tools.isLandscape(getActivity(), config, Tools.getScreenSize(getActivity()))){
            cards.setOrientation(LinearLayout.HORIZONTAL);
            cards.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END | LinearLayout.SHOW_DIVIDER_MIDDLE);
        } else if (getActivity().isInMultiWindowMode() || config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            cards.setOrientation(LinearLayout.VERTICAL);
            cards.setShowDividers(LinearLayout.SHOW_DIVIDER_END | LinearLayout.SHOW_DIVIDER_MIDDLE);
        }

        //Dismiss dialogs on device rotate because they has separate layouts for land/port
        if (devDialog != null) devDialog.dismiss();
        if (instDialog != null) instDialog.dismiss();

        ((App) getActivity().getApplication()).dismissDialog();
    }

    private void prepareDevice(boolean force) {
        if (prefs.contains(pref.DEVICE) && prefs.contains(pref.DEVICE_CODE))
            new Thread(() -> parseRelease(null, force)).start();
        else
            new Thread(() -> setDevice(null, false, false)).start();
    }

    private boolean abortDevice(Map<String, Object> response, BottomSheetDialog dialog) {
        if (!(boolean) response.get("success")) {
            if (dialog != null) dialog.dismiss();
            errorCard((int)response.get("code"), R.string.err_no_rels);
            if ((int)response.get("code") == 404 || (int)response.get("code") == 500)
                prefs.edit().remove(pref.DEVICE).remove(pref.DEVICE_CODE).apply();
            return true;
        }
        return false;
    }

    private void parseRelease(BottomSheetDialog dialog, boolean force) {
        try {
            JSONObject release;
            Map<String, Object> responseLast = API.request("releases/?limit=1&codename=" + prefs.getString(pref.DEVICE_CODE, "err"));
            if (!prefs.contains(pref.RELEASE_ID) || !(boolean) responseLast.get("success") && (int)responseLast.get("code") != 0)
                if (abortDevice(responseLast, dialog)) return; //no internet/cached_id

            String id = null;
            boolean useCached;
            if ((int)responseLast.get("code") == 200) {
                id = new JSONObject((String) responseLast.get("response"))
                        .getJSONArray("data").getJSONObject(0).getString("_id");
                useCached = !force && id.equals(prefs.getString(pref.RELEASE_ID, "err"));
            } else {
                useCached = true;
            }

            if (!force && useCached && prefs.contains(pref.CACHE_RELEASE)) {
                release = new JSONObject(prefs.getString(pref.CACHE_RELEASE, null));
            } else {
                Map<String, Object> response = API.request("releases/get?_id=" + id);
                if (abortDevice(response, dialog)) return;
                release = new JSONObject((String) response.get("response"));
            }

            final String version = release.getString("version");
            final String url = release.getJSONObject("mirrors").getString("DL");
            final String stringBuildType = Tools.getBuildType(getActivity(), release);
            final String md5 = release.getString("md5");

            ((TextView)rootView.findViewById(R.id.relType)).setText(stringBuildType);
            ((TextView)rootView.findViewById(R.id.relVers)).setText(version);
            ((TextView)rootView.findViewById(R.id.relDate)).setText(Tools.formatDate(release.getLong("date")));
            ((TextView)rootView.findViewById(R.id.relSize)).setText(Tools.formatSize(getActivity(), release.getInt("size")));

            JSONObject device = new JSONObject(prefs.getString(pref.DEVICE, "{}"));

            ((TextView)rootView.findViewById(R.id.devCode)).setText(device.getString("codename"));
            ((TextView)rootView.findViewById(R.id.devModel)).setText(device.getString("full_name"));
            ((TextView)rootView.findViewById(R.id.devStatus)).setText(device.getBoolean("supported") ?
                    R.string.dev_maintained : R.string.dev_unmaintained);
            ((TextView)rootView.findViewById(R.id.devPatch)).setText(Build.VERSION.SECURITY_PATCH);

            _installButton.setOnClickListener(view -> {
                if (((App)getActivity().getApplication()).isDownloadSrvRunning())
                    Tools.showSnackbar(getActivity(), _installButton, R.string.err_service_running).show();
                else
                    instDialog = Install.dialog(getActivity(), version, stringBuildType, url, md5, false, null);
            });
            getActivity().runOnUiThread(() -> {
                _installButton.setText(getString(R.string.install_latest, version, stringBuildType));
                _errorLayout.setVisibility(View.GONE);
                _shimmer.setVisibility(View.GONE);
                _shimmer2.setVisibility(View.GONE);
                _cardInfo.setVisibility(View.VISIBLE);
                _cardRelease.setVisibility(View.VISIBLE);
                _installButton.show();
                _refreshLayout.setEnabled(true);
                _refreshLayout.setRefreshing(false);
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

    private void setDevice(String codename, boolean skipDialog, boolean force) {
        if (codename == null)
            codename = findDevice();
        if (codename == null) {
            showDeviceDialog(Build.DEVICE, true, null);
            return;
        }
        if (codename.equals("no_internet_error"))
            return;
        Map<String, Object> response = API.request("devices/get?codename=" + codename);
        if (!(boolean) response.get("success")) {
            errorCard((int) response.get("code"), R.string.err_no_device);
            return;
        }
        if (skipDialog){
            prefs.edit().putString(pref.DEVICE, (String)response.get("response")).putString(pref.DEVICE_CODE, codename).apply();
            new Thread(() -> parseRelease(null, force)).start();
        } else
            showDeviceDialog(codename, false, (String)response.get("response"));
    }

    protected void showDeviceDialog(String device, boolean fail, String cache) {
        getActivity().runOnUiThread(() -> {
            ((AHBottomNavigation)getActivity().findViewById(R.id.bottom_navigation)).setCurrentItem(0);

            View sheetView = getLayoutInflater().inflate(R.layout.dialog_device, (ViewGroup)null);

            devDialog = Tools.initBottomSheet(getActivity(), sheetView);

            Button gSelect = sheetView.findViewById(R.id.guessSelect);
            Button gRight = sheetView.findViewById(R.id.btnInstall);
            Button gWrong = sheetView.findViewById(R.id.btnCancel);
            TextView gCode = sheetView.findViewById(R.id.guessDeviceCode);
            TextView gBottom = sheetView.findViewById(R.id.guessBottomText);
            ProgressBar gProgress = sheetView.findViewById(R.id.setupProgress);

            gCode.setText(device.toUpperCase());

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

        try {
            Map<String, Object> response = API.request("devices");
            if(!(boolean)response.get("success")) {
                errorCard((int)response.get("code"), R.string.err_no_device);
                return "no_internet_error";
            }
            JSONArray devices = new JSONObject((String)response.get("response")).getJSONArray("data");
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

        getActivity().runOnUiThread(() -> {
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
}