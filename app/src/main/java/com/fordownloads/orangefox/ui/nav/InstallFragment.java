package com.fordownloads.orangefox.ui.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.fordownloads.orangefox.API;
import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.RecyclerActivity;
import com.fordownloads.orangefox.pref;
import com.fordownloads.orangefox.ui.recycler.RecyclerItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class InstallFragment extends Fragment {
    TextView _ofTitle;
    Button _installButton, _releaseInfo, _oldReleases;
    SharedPreferences prefs;
    View rootView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_install, container, false);
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        _ofTitle = rootView.findViewById(R.id.ofTitle);
        _installButton = rootView.findViewById(R.id.installButton);
        _releaseInfo = rootView.findViewById(R.id.releaseInfo);
        _oldReleases = rootView.findViewById(R.id.oldReleases);

        _releaseInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.CACHE_RELEASE, "err"));
            intent.putExtra("type", 1);
            intent.putExtra("title", R.string.rel_activity);
            startActivityForResult(intent, 200);
        });

        _oldReleases.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RecyclerActivity.class);
            intent.putExtra("release", prefs.getString(pref.DEVICE_CODE, "err"));
            intent.putExtra("type", 2);
            intent.putExtra("title", R.string.rels_activity);
            startActivityForResult(intent, 200);
        });

        _installButton.setOnClickListener((View view)-> {
            Activity act = getActivity();
            ((AHBottomNavigation)act.findViewById(R.id.bottom_navigation)).hideBottomNavigation(true);
            view.setVisibility(View.GONE);
            ((LinearLayout)act.findViewById(R.id.cards)).setVisibility(View.GONE);
        });

        rotateUI(rootView.findViewById(R.id.cards), getResources().getConfiguration());

        prepareDevice();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Toast.makeText(getActivity().getApplicationContext(), "releaseId" +
                    data.getStringExtra("release"),
                    Toast.LENGTH_LONG).show();
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

    private void prepareDevice() {
        if (prefs.contains(pref.DEVICE) && prefs.contains(pref.DEVICE_CODE))
            new Thread(() -> parseRelease(null)).start();
        else
            new Thread(this::setDevice).start();
    }

    private void parseRelease(BottomSheetDialog dialog) {
        Log.i("OFOFOF", "parseRel");
        try {
            JSONObject release;
            if (prefs.contains(pref.CACHE_RELEASE)) {
                release = new JSONObject(prefs.getString(pref.CACHE_RELEASE, null));
            } else {
                Map<String, Object> response = API.request("device/" + prefs.getString(pref.DEVICE_CODE, "err") + "/releases/last");
                if (!(boolean) response.get("success"))
                    return;
                release = new JSONObject((String) response.get("response"));
            }

            String buildType = release.getString("build_type");
            switch (buildType) {
                case "stable":
                    buildType = getString(R.string.rel_stable);
                    break;
                case "beta":
                    buildType = getString(R.string.rel_beta);
                    break;
            }

            ((TextView) rootView.findViewById(R.id.relType)).setText(buildType);
            ((TextView) rootView.findViewById(R.id.relVers)).setText(release.getString("version"));
            ((TextView) rootView.findViewById(R.id.relDate)).setText(release.getString("date"));
            ((TextView) rootView.findViewById(R.id.relSize)).setText(release.getString("size_human"));

            JSONObject device = new JSONObject(prefs.getString(pref.DEVICE, "{}"));
            ((TextView) rootView.findViewById(R.id.devCode)).setText(device.getString("codename"));
            ((TextView) rootView.findViewById(R.id.devModel)).setText(device.getString("fullname"));/*
            ((TextView) rootView.findViewById(R.id.devStatus)).setText(device.getInt("maintained"));
            ((TextView) rootView.findViewById(R.id.devStatus)).setText(device.getJSONObject("maintainer").getString("name"));*/
            ((TextView) rootView.findViewById(R.id.devPatch)).setText(Build.VERSION.SECURITY_PATCH);
            Log.i("OFOFOF", "eba");

            if (!prefs.contains(pref.CACHE_RELEASE))
                prefs.edit().putString(pref.CACHE_RELEASE, release.toString()).apply();

            if (dialog != null)
                dialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDevice() {
        try {
            JSONObject response = findDevice();
            if (response == null) {
                showDeviceDialog(Build.VERSION.CODENAME, true);
                return;
            }
            prefs.edit().putString(pref.DEVICE, response.toString()).putString(pref.DEVICE_CODE, response.getString("codename")).apply();
            showDeviceDialog(response.getString("codename"), false);
        } catch (JSONException e) {
            e.printStackTrace();
            showDeviceDialog(Build.VERSION.CODENAME, true);
        }
    }

    protected void showDeviceDialog(String device, boolean fail) {
        getActivity().runOnUiThread(() -> {
            BottomSheetDialog devDialog = new BottomSheetDialog(getActivity(), R.style.ThemeBottomSheet);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_device, null);
            devDialog.setContentView(sheetView);
            devDialog.setDismissWithAnimation(true);

            devDialog.setCancelable(false);
            devDialog.setCanceledOnTouchOutside(false);

            Button gSelect = sheetView.findViewById(R.id.guessSelect);
            Button gRight = sheetView.findViewById(R.id.guessRight);
            Button gWrong = sheetView.findViewById(R.id.guessWrong);
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

            gRight.setOnClickListener(v -> {
                gRight.setVisibility(View.GONE);
                gWrong.setVisibility(View.GONE);
                gProgress.setVisibility(View.VISIBLE);
                new Thread(() -> parseRelease(devDialog)).start();
            });

            devDialog.show();
        });
    }

    protected JSONObject findDevice() {
        String chk1 = Build.VERSION.CODENAME.toLowerCase();
        String chk2 = Build.DEVICE.toLowerCase();
        String chk3 = Build.MODEL.toLowerCase();
        String chk4 = Build.PRODUCT.toLowerCase();

        try {
            Map<String, Object> response = API.request("device");
            if(!(boolean)response.get("success"))
                return null;
            JSONArray devices = new JSONArray((String)response.get("response"));
            for (int i = 0; i < devices.length(); i++)
            {
                JSONObject device = devices.getJSONObject(i);
                String dbDev = device.getString("codename").toLowerCase();
                if (dbDev.contains(chk1) || dbDev.contains(chk2) || dbDev.contains(chk3) || dbDev.contains(chk4))
                    return device;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}