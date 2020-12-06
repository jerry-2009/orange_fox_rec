package com.fordownloads.orangefox;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.fordownloads.orangefox.ui.nav.InstallFragment;
import com.fordownloads.orangefox.ui.nav.ScriptsFragment;
import com.fordownloads.orangefox.ui.nav.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

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

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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