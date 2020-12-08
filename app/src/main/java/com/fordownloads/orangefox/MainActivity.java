package com.fordownloads.orangefox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.fordownloads.orangefox.ui.nav.InstallFragment;
import com.fordownloads.orangefox.ui.nav.ScriptsFragment;
import com.fordownloads.orangefox.ui.nav.SettingsFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        prefs = getPreferences(Context.MODE_PRIVATE);
        if (prefs.contains(pref.DEVICE)) {

        } else {
            findDevice();
        }

        /*prepareDevice();*/
        prepareBottomNav();
        showDeviceDialog("x00t", false);
    }

    private void prepareDevice() {

        if (prefs.contains(pref.CACHE_DEVICE)) {

        } else {

        }
        if (prefs.contains(pref.CACHE_RELEASE)) {

        } else {

        }
    }

    protected void showDeviceDialog(String device, boolean fail) {
        BottomSheetDialog devDialog = new BottomSheetDialog(this, R.style.ThemeBottomSheet);
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

        gCode.setText(device.toUpperCase());

        if (fail) {
            gRight.setVisibility(View.GONE);
            gWrong.setVisibility(View.GONE);
            gBottom.setText(R.string.guess_fail);
        } else
            gSelect.setVisibility(View.GONE);

        gRight.setOnClickListener(v -> {
            devDialog.dismiss();
        });

        devDialog.show();
    }

    protected JSONObject findDevice() {
        String chk1 = Build.VERSION.CODENAME.toLowerCase();
        String chk2 = Build.DEVICE.toLowerCase();
        String chk3 = Build.MODEL.toLowerCase();
        String chk4 = Build.PRODUCT.toLowerCase();

        try {
            Map<String, Object> response = API.request("device");
            runOnUiThread(() -> API.errorHandler(this, response, R.string.err_no_rel));
            if(!(boolean)response.get("success"))
                return null;
            JSONArray devices = new JSONArray(response);
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

    protected void prepareBottomNav() {
        AHBottomNavigation bn = findViewById(R.id.bottom_navigation);
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_install, R.drawable.ic_round_save_alt_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_scripts, R.drawable.ic_outline_receipt_long_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_settings, R.drawable.ic_settings, R.color.fox_accent));

        bn.setBehaviorTranslationEnabled(false);
        bn.setUseElevation(true);

        bn.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.fox_title_solid_bg));
        bn.setAccentColor(ContextCompat.getColor(this, R.color.fox_accent));
        bn.setInactiveColor(ContextCompat.getColor(this, R.color.google_gray));

        bn.setTitleTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.euclid_flex));
        bn.setTitleTextSizeInSp(14, 12);
        /*bn.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE_FORCE);*/

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (getSupportFragmentManager().findFragmentByTag("install") == null)
            getSupportFragmentManager().beginTransaction().add(R.id.nav_frame, new InstallFragment(), "install").commit();

        bn.setOnTabSelectedListener((position, wasSelected) -> {
            if (wasSelected)
                return true;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction tsa = fm.beginTransaction();
            tsa.setCustomAnimations(R.anim.slide_in, R.anim.slide_in);

            switch (position) {
                case 2:
                    if(fm.findFragmentByTag("settings") != null) tsa.show(fm.findFragmentByTag("settings")).commit();
                    else tsa.add(R.id.nav_frame, new SettingsFragment(), "settings").commit();

                    if(fm.findFragmentByTag("scripts") != null) fm.beginTransaction().hide(fm.findFragmentByTag("scripts")).commit();
                    if(fm.findFragmentByTag("install") != null) fm.beginTransaction().hide(fm.findFragmentByTag("install")).commit();

                    break;
                case 1:
                    if(fm.findFragmentByTag("scripts") != null) tsa.show(fm.findFragmentByTag("scripts")).commit();
                    else tsa.add (R.id.nav_frame, new ScriptsFragment(), "scripts").commit();

                    if(fm.findFragmentByTag("install") != null) fm.beginTransaction().hide(fm.findFragmentByTag("install")).commit();
                    if(fm.findFragmentByTag("settings") != null) fm.beginTransaction().hide(fm.findFragmentByTag("settings")).commit();

                    break;
                default:
                    if(fm.findFragmentByTag("install") != null) tsa.show(fm.findFragmentByTag("install")).commit();
                    else tsa.add (R.id.nav_frame, new InstallFragment(), "install").commit();

                    if(fm.findFragmentByTag("scripts") != null) fm.beginTransaction().hide(fm.findFragmentByTag("scripts")).commit();
                    if(fm.findFragmentByTag("settings") != null) fm.beginTransaction().hide(fm.findFragmentByTag("settings")).commit();

                    break;
            }
            return true;
        });
    }

}