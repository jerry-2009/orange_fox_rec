package com.fordownloads.orangefox.ui.nav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
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
import com.fordownloads.orangefox.API;
import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.Install;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.RecyclerActivity;
import com.fordownloads.orangefox.SettingsActivity;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.ui.Tools;
import com.fordownloads.orangefox.vars;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class InstallFragment extends Fragment {
    TextView _ofTitle, _cardErrorText, _cardErrorTitle;
    Button _releaseInfo, _oldReleases, _btnRefresh;
    SharedPreferences prefs;
    ExtendedFloatingActionButton _installButton;
    View rootView;
    CardView _cardError, _cardInfo, _cardRelease;
    ImageView _cardErrorIcon;
    View _shimmer;
    SwipeRefreshLayout _refreshLayout;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_install, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        _ofTitle = rootView.findViewById(R.id.ofTitle);
        _installButton = rootView.findViewById(R.id.installButton);
        _releaseInfo = rootView.findViewById(R.id.releaseInfo);
        _oldReleases = rootView.findViewById(R.id.oldReleases);
        _btnRefresh = rootView.findViewById(R.id.btnRefresh);

        _cardError = rootView.findViewById(R.id.cardError);
        _cardInfo = rootView.findViewById(R.id.cardInfo);
        _cardRelease = rootView.findViewById(R.id.cardRelease);

        _cardErrorIcon = rootView.findViewById(R.id.cardErrorIcon);
        _cardErrorText = rootView.findViewById(R.id.cardErrorText);
        _cardErrorTitle = rootView.findViewById(R.id.cardErrorTitle);
        _shimmer = rootView.findViewById(R.id.shimmer);

        _installButton.hide();

        rootView.findViewById(R.id.settingsOpen).setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), SettingsActivity.class), 300));

        _releaseInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.CACHE_RELEASE, "no_cache_release"));
            intent.putExtra("type", 1);
            intent.putExtra("title", R.string.rel_activity);
            startActivityForResult(intent, 200);
        });

        _oldReleases.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.DEVICE_CODE, "no_device_code"));
            intent.putExtra("type", 2);
            intent.putExtra("title", R.string.rels_activity);
            startActivityForResult(intent, 200);
        });

        _btnRefresh.setOnClickListener(view -> {
            _cardError.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            prepareDevice(false);
        });

        _refreshLayout = rootView.findViewById(R.id.refreshLayout);
        _refreshLayout.setOnRefreshListener(() -> {
            _cardError.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
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
            _cardError.setVisibility(View.GONE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.VISIBLE);
            _installButton.hide();
            new Thread(() -> setDevice(data.getStringExtra("codename"), true, true)).start();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        rotateUI(getActivity().findViewById(R.id.cards), config);
    }

    private void rotateUI(LinearLayout cards, Configuration config) {
        if (cards == null) return;

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && (float)(size.x / size.y) > 1.6)
            cards.setOrientation(LinearLayout.HORIZONTAL);
        else if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            cards.setOrientation(LinearLayout.VERTICAL);
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
            if (prefs.contains(pref.CACHE_RELEASE) && !force) {
                release = new JSONObject(prefs.getString(pref.CACHE_RELEASE, null));
            } else {
                Map<String, Object> responseLast = API.request("releases/?limit=1&codename=" + prefs.getString(pref.DEVICE_CODE, "err"));
                if (abortDevice(responseLast, dialog)) return;

                String id = new JSONObject((String) responseLast.get("response"))
                        .getJSONArray("data").getJSONObject(0).getString("_id");

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
            ((TextView)rootView.findViewById(R.id.devMaintainer)).setText(device.getJSONObject("maintainer").getString("name"));
            ((TextView)rootView.findViewById(R.id.devPatch)).setText(Build.VERSION.SECURITY_PATCH);

            _installButton.setOnClickListener(view -> {
                if (((App)getActivity().getApplication()).isDownloadSrvRunning())
                    Tools.showSnackbar(getActivity(), _installButton, R.string.err_service_running).show();
                else
                    Install.dialog(getActivity(), version, stringBuildType, url, md5, false, null);
            });
            getActivity().runOnUiThread(() -> {
                _installButton.setText(getString(R.string.install_latest, version, stringBuildType));
                _cardError.setVisibility(View.GONE);
                _shimmer.setVisibility(View.GONE);
                _cardInfo.setVisibility(View.VISIBLE);
                _cardRelease.setVisibility(View.VISIBLE);
                _installButton.show();
                _refreshLayout.setEnabled(true);
                _refreshLayout.setRefreshing(false);
            });

            if (!prefs.contains(pref.CACHE_RELEASE))
                prefs.edit().putString(pref.CACHE_RELEASE, release.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            errorCard(1000, 0);
        }
        if (dialog != null) dialog.dismiss();
    }

    private void setDevice(String codename, boolean skipDialog, boolean force) {
        if (codename == null)
            codename = findDevice();
        if (codename == "no_internet_error")
            return;
        if (codename == null) {
            showDeviceDialog(Build.DEVICE, true, null);
            return;
        }
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
            BottomSheetDialog devDialog = new BottomSheetDialog(getActivity(), R.style.ThemeBottomSheet);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_device, null);
            devDialog.setContentView(sheetView);
            devDialog.setDismissWithAnimation(true);
            /*
            devDialog.setCancelable(false);
            devDialog.setCanceledOnTouchOutside(false);*/

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

            devDialog.setOnDismissListener(v -> {
                errorCard(404, R.string.err_dev_not_selected);
            });

            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);

            sheetView.setY(size.y);
            devDialog.show();

            sheetView.animate()
                    .setInterpolator(vars.intr)
                    .setDuration(600)
                    .setStartDelay(200)
                    .setStartDelay(100)
                    .translationY(0);
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
            _cardErrorTitle.setText(isInternet ? R.string.err_card_no_internet : R.string.err_card_error);
            _cardErrorText.setText(text);
            _cardErrorIcon.setImageResource(isInternet ? R.drawable.ic_round_public_off_24 : R.drawable.ic_round_warning_24);

            _cardError.setVisibility(View.VISIBLE);
            _cardInfo.setVisibility(View.GONE);
            _cardRelease.setVisibility(View.GONE);
            _shimmer.setVisibility(View.GONE);
            _refreshLayout.setRefreshing(false);
            _refreshLayout.setEnabled(true);
        });
    }
}