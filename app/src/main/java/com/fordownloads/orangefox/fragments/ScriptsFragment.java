package com.fordownloads.orangefox.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.recycler.ors.ORSAdapter;
import com.fordownloads.orangefox.recycler.ors.SimpleItemTouchHelperCallback;
import com.fordownloads.orangefox.utils.Tools;
import com.fordownloads.orangefox.viewpager.fdViewPagerItemAdapter;
import com.fordownloads.orangefox.viewpager.fdViewPagerItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItems;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ScriptsFragment extends Fragment {
    private ItemTouchHelper mItemTouchHelper;
    List<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    ORSAdapter ORSAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scripts, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.bnav_scripts);

        rootView.findViewById(R.id.createScript).setOnClickListener(v -> {
            if (items.size() == 0)
                return;
            StringBuilder content = new StringBuilder();
            content.append("echo \"");
            for (RecyclerItems item : items)
                content.append(item.getData()).append("\n");
            content.append("\" > ").append(consts.ORS_FILE);
            if(!Shell.su(content.toString().trim()).exec().isSuccess())
                Tools.showSnackbar(getActivity(), rootView.findViewById(R.id.createScript), R.string.err_ors_short);
        });

        rootView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            addDialog(getActivity());
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ORSAdapter = new ORSAdapter(getActivity(), holder -> mItemTouchHelper.startDrag(holder), items, v -> {
            Toast.makeText(getActivity(), ((TextView)v.findViewById(R.id.text)).getText(), Toast.LENGTH_SHORT).show();
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ORSAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(ORSAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public BottomSheetDialog addDialog(FragmentActivity a) {
        LayoutInflater inflater = a.getLayoutInflater();
        View sheetView = inflater.inflate(R.layout.dialog_add, null);
        BottomSheetDialog dialog = Tools.initBottomSheet(a, sheetView);

        View backupView = inflater.inflate(R.layout.listview_button, null);
        ListView backupList = backupView.findViewById(R.id.listView);
        backupList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, getResources().getStringArray(R.array.backup_list)));

        View wipeView = inflater.inflate(R.layout.listview_button, null);
        ListView wipeList = wipeView.findViewById(R.id.listView);
        wipeList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, getResources().getStringArray(R.array.wipe_list)));

        ViewPager viewPager = sheetView.findViewById(R.id.viewpager);
        fdViewPagerItemAdapter adapter = new fdViewPagerItemAdapter(fdViewPagerItems.with(a)
                .add(R.string.script_zip, inflater.inflate(R.layout.list_select_zip_btn, null))
                .add(R.string.script_backup, backupView)
                .add(R.string.script_wipe, wipeView)
                .create());
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);

        viewPager.setAdapter(adapter);

        backupView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            arg.append("backup ");
            for (byte i = 0; i < backupList.getCount(); i++) {
                CheckBox item = (CheckBox)backupList.getChildAt(i);
                if (item != null && item.isChecked()) {
                    arg.append(item.getText().charAt(0));
                    argUser.append(item.getText()).append("; ");
                }
            }
            arg.append(" app");
            items.add(new RecyclerItems(getString(R.string.script_backup),
                    argUser.toString(), R.drawable.ic_outline_cloud_download_24, arg.toString()));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            dialog.dismiss();
        });

        wipeView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            for (byte i = 0; i < wipeList.getCount(); i++) {
                CheckBox item = (CheckBox)wipeList.getChildAt(i);
                if (item != null && item.isChecked()) {
                    arg.append("wipe ").append(item.getText().toString().toLowerCase()).append("\n");
                    argUser.append(item.getText()).append("; ");
                }
            }
            items.add(new RecyclerItems(getString(R.string.script_wipe),
                    argUser.toString(), R.drawable.ic_delete, arg.toString().trim()));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            dialog.dismiss();
        });

        ((SmartTabLayout)sheetView.findViewById(R.id.viewpagertab)).setViewPager(viewPager);

        dialog.show();
        sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);

        return dialog;
    }
}