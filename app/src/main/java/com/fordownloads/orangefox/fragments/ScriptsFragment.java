package com.fordownloads.orangefox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ScriptsFragment extends Fragment {
    private ItemTouchHelper mItemTouchHelper;
    List<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    ORSAdapter ORSAdapter;
    ExtendedFloatingActionButton _createScript;
    BottomSheetDialog dialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scripts, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.bnav_scripts);

        _createScript = rootView.findViewById(R.id.createScript);
        _createScript.hide();
        _createScript.setOnClickListener(v -> buildScript(false));

        rootView.findViewById(R.id.btnAdd).setOnClickListener(v -> addDialog(getActivity()));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.scripts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                items.clear();
                ORSAdapter.notifyDataSetChanged();
                _createScript.hide();
                return true;
            case R.id.reboot:
                if (!Shell.su("reboot recovery").exec().isSuccess())
                    Tools.showSnackbar(getActivity(), _createScript, R.string.err_reboot_notify).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buildScript(boolean force) {
        if (items.size() == 0) {
            Tools.showSnackbar(getActivity(), _createScript, R.string.script_err_nothing).show();
            return;
        }
        if(!Shell.rootAccess()) {
            Tools.showSnackbar(getActivity(), _createScript, R.string.err_no_pm_root);
            return;
        }
        StringBuilder content = new StringBuilder();
        content.append("echo \"");
        for (RecyclerItems item : items)
            content.append(item.getData()).append("\n");

        if (!force && content.toString().contains("wipe system") && !content.toString().contains("install ")) {
            Tools.showSnackbar(getActivity(), _createScript, R.string.script_err_rom)
                    .setAction(R.string.ignore, view -> buildScript(true)).show();
            return;
        }

        content.append("\" > ").append(consts.ORS_FILE);
        if(Shell.su(content.toString().trim()).exec().isSuccess())
            Tools.showSnackbar(getActivity(), _createScript, R.string.script_created)
                    .setAction(R.string.reboot, view -> {
                        if (!Shell.su("reboot recovery").exec().isSuccess())
                            Tools.showSnackbar(getActivity(), _createScript, R.string.err_reboot_notify).show();
                    }).show();
        else
            Tools.showSnackbar(getActivity(), _createScript, R.string.err_ors_short).show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ORSAdapter = new ORSAdapter(getActivity(), holder -> mItemTouchHelper.startDrag(holder), items, v ->
                Toast.makeText(getActivity(), ((TextView)v.findViewById(R.id.text)).getText(), Toast.LENGTH_SHORT).show());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ORSAdapter);

        ORSAdapter.setOnItemDismissListener(position -> {
            if (items.size() == 0)
                _createScript.hide();
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(ORSAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                String path = Tools.getFileFromFilePicker(resultData);
                if (path == null) {
                    Tools.showSnackbar(getActivity(), _createScript, R.string.err_filepicker).show();
                    dialog.dismiss();
                    return;
                }
                items.add(new RecyclerItems(getString(R.string.script_zip),
                        path, R.drawable.ic_outline_archive_24, "install " + path));
                ORSAdapter.notifyItemInserted(items.size() - 1);
                _createScript.show();
                dialog.dismiss();
            }
        }
    }

    public BottomSheetDialog addDialog(FragmentActivity a) {
        LayoutInflater inflater = a.getLayoutInflater();
        View sheetView = inflater.inflate(R.layout.dialog_add, null);
        dialog = Tools.initBottomSheet(a, sheetView);

        View backupView = inflater.inflate(R.layout.listview_button, null);
        ListView backupList = backupView.findViewById(R.id.listView);
        TextView backupName = backupView.findViewById(R.id.backupName);
        backupList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, getResources().getStringArray(R.array.backup_list)));

        backupName.setText(Tools.getBackupFileName());

        View wipeView = inflater.inflate(R.layout.listview_button, null);
        ListView wipeList = wipeView.findViewById(R.id.listView);
        wipeView.findViewById(R.id.backupNameLayout).setVisibility(View.GONE);
        wipeList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, getResources().getStringArray(R.array.wipe_list)));

        View installView = inflater.inflate(R.layout.list_select_zip_btn, null);

        ViewPager viewPager = sheetView.findViewById(R.id.viewpager);
        fdViewPagerItemAdapter adapter = new fdViewPagerItemAdapter(fdViewPagerItems.with(a)
                .add(R.string.script_zip, installView)
                .add(R.string.script_backup, backupView)
                .add(R.string.script_wipe, wipeView)
                .create());
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);

        viewPager.setAdapter(adapter);

        installView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            startActivityForResult(intent, 10);
        });

        backupView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            boolean empty = true;
            arg.append("backup ");
            for (byte i = 0; i < backupList.getCount(); i++) {
                CheckBox item = (CheckBox)backupList.getChildAt(i);
                if (item != null && item.isChecked()) {
                    arg.append(item.getText().charAt(0));
                    argUser.append(item.getText()).append("; ");
                    empty = false;
                }
            }

            if (empty) {
                backupList.startAnimation(AnimationUtils.loadAnimation(a, R.anim.shake));
                return;
            }

            arg.append(" ");
            String name = backupName.getText().toString().trim();
            name = name.equals("") ? Tools.getBackupFileName() : name;
            arg.append(name);
            items.add(new RecyclerItems(getString(R.string.script_backup),
                    name + "\n" + argUser.toString().substring(0, argUser.length() - 2), R.drawable.ic_outline_cloud_download_24, arg.toString()));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            _createScript.show();
            dialog.dismiss();
        });

        wipeView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            boolean empty = true;
            for (byte i = 0; i < wipeList.getCount(); i++) {
                CheckBox item = (CheckBox)wipeList.getChildAt(i);
                if (item != null && item.isChecked()) {
                    arg.append("wipe ").append(item.getText().toString().toLowerCase()).append("\n");
                    argUser.append(item.getText()).append("; ");
                    empty = false;
                }
            }

            if (empty) {
                wipeList.startAnimation(AnimationUtils.loadAnimation(a, R.anim.shake));
                return;
            }

            items.add(new RecyclerItems(getString(R.string.script_wipe),
                    argUser.toString().substring(0, argUser.length() - 2), R.drawable.ic_delete, arg.toString().trim()));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            _createScript.show();
            dialog.dismiss();
        });

        ((SmartTabLayout)sheetView.findViewById(R.id.viewpagertab)).setViewPager(viewPager);

        dialog.show();
        sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);

        return dialog;
    }
}