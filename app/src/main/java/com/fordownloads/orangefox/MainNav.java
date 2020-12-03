package com.fordownloads.orangefox;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.fordownloads.orangefox.ui.install.InstallFragment;
import com.fordownloads.orangefox.ui.scripts.ScriptsFragment;
import com.fordownloads.orangefox.ui.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainNav extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setContext(this);

        setContentView(R.layout.activity_main_nav);

        AHBottomNavigation bn = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_install, R.drawable.ic_round_save_alt_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_scripts, R.drawable.ic_outline_receipt_long_24, R.color.fox_accent));
        bn.addItem(new AHBottomNavigationItem(R.string.bnav_settings, R.drawable.ic_settings, R.color.fox_accent));

        bn.setDefaultBackgroundColor(App.col(R.color.fox_title_solid_bg));
        bn.setAccentColor(App.col(R.color.fox_accent));
        bn.setInactiveColor(App.col(R.color.google_gray));
        /*bn.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE_FORCE);*/

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getSupportFragmentManager().beginTransaction().add(R.id.nav_frame, new InstallFragment(), "install").commit();

        bn.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
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
            }
        });
    }

}