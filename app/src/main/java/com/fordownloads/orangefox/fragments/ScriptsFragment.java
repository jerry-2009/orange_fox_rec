package com.fordownloads.orangefox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.activity.PatternActivity;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.recycler.ors.ORSAdapter;
import com.fordownloads.orangefox.recycler.ors.SimpleItemTouchHelperCallback;
import com.fordownloads.orangefox.utils.Tools;
import com.fordownloads.orangefox.viewpager.fdViewPagerItemAdapter;
import com.fordownloads.orangefox.viewpager.fdViewPagerItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ScriptsFragment extends Fragment {
    private ItemTouchHelper mItemTouchHelper;
    List<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    ORSAdapter ORSAdapter;
    ExtendedFloatingActionButton _createScript;
    BottomSheetDialog dialog = null;
    View _emptyHelp, _emptyArt;
    Button _btnAdd;
    View sheetView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //prerender dialog to avoid lags
        new Thread(() ->  addDialog(getActivity(), false)).start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
            sheetView = null;
        }

        rotateUI(config);
    }

    private void rotateUI(Configuration config) {
        if (_emptyArt == null) return;

        if (Tools.isLandscape(getActivity(), config, Tools.getScreenSize(getActivity())))
            _emptyArt.setVisibility(View.GONE);
        else if (requireActivity().isInMultiWindowMode() || config.orientation == Configuration.ORIENTATION_PORTRAIT)
            _emptyArt.setVisibility(View.VISIBLE);
    }

    public void listEmpty(boolean empty) {
        if (empty)
            _createScript.hide();
        else
            _createScript.show();
        _emptyHelp.setVisibility(empty ? View.VISIBLE : View.GONE);
        _btnAdd.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scripts, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(myToolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.bnav_scripts);

        _emptyHelp = rootView.findViewById(R.id.emptyHelp);
        _emptyArt = rootView.findViewById(R.id.emptyArt);
        _createScript = rootView.findViewById(R.id.createScript);
        _createScript.hide();
        _createScript.setOnClickListener(v -> buildScript(consts.ORS_FILE,false, false));

        _btnAdd = rootView.findViewById(R.id.btnAdd);
        _btnAdd.setOnClickListener(v -> addDialog(getActivity(), true));
        rootView.findViewById(R.id.btnAdd2).setOnClickListener(v -> addDialog(getActivity(), true));

        rotateUI(getResources().getConfiguration());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.scripts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                items.clear();
                ORSAdapter.notifyDataSetChanged();
                listEmpty(true);
                return true;
            case R.id.reboot:
                if (!Shell.su("reboot recovery").exec().isSuccess())
                    Tools.showSnackbar(getActivity(), _createScript, R.string.err_reboot_notify).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buildScript(String orsFile, boolean force, boolean silent) {
        if (items.size() == 0) {
            if (!silent)
                Tools.showSnackbar(getActivity(), _createScript, R.string.script_err_nothing).show();
            return;
        }
        if(!Shell.rootAccess()) {
            if (!silent)
                Tools.showSnackbar(getActivity(), _createScript, R.string.err_no_pm_root);
            return;
        }
        StringBuilder content = new StringBuilder();
        content.append("echo \"");
        for (RecyclerItems item : items)
            content.append(item.getData()).append("\n");

        if (!force && content.toString().contains("wipe system") && !content.toString().contains("install ")) {
            Tools.showSnackbar(getActivity(), _createScript, R.string.script_err_rom)
                    .setAction(R.string.ignore, view -> buildScript(consts.ORS_FILE, true, false)).show();
            return;
        }

        content.append("\" > ").append(orsFile);
        if(Shell.su(content.toString().trim()).exec().isSuccess() && !silent)
            Tools.showSnackbar(getActivity(), _createScript, R.string.script_created)
                        .setAction(R.string.reboot, view -> {
                            if (!Shell.su("reboot recovery").exec().isSuccess())
                                Tools.showSnackbar(getActivity(), _createScript, R.string.err_reboot_notify).show();
                        }).show();
        else if (!silent)
            Tools.showSnackbar(getActivity(), _createScript, R.string.err_ors_short).show();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ORSAdapter = new ORSAdapter(getActivity(), holder -> mItemTouchHelper.startDrag(holder), items, v ->
                Toast.makeText(getActivity(), ((TextView)v.findViewById(R.id.text)).getText(), Toast.LENGTH_SHORT).show());

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ORSAdapter);

        ORSAdapter.setOnItemDismissListener(position -> {
            if (items.size() == 0)
                listEmpty(true);
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
                listEmpty(false);
                dialog.hide();
            }
        }
        if (requestCode == 400 && resultCode == Activity.RESULT_OK && resultData != null) {
            String pass = resultData.getStringExtra("pass");
            items.add(new RecyclerItems(getString(R.string.script_decrypt),
                    pass, R.drawable.ic_baseline_lock_open_24, "decrypt " + pass));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            listEmpty(false);
            dialog.dismiss();
        }
    }

    public void addDialog(FragmentActivity a, boolean show) {
        if (show && dialog != null) {
            sheetView.setY(Tools.getScreenSize(a)[1]);
            dialog.show();
            sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);
            return;
        }

        LayoutInflater inflater = a.getLayoutInflater();
        sheetView = inflater.inflate(R.layout.dialog_add, null);
        a.runOnUiThread(() -> dialog = Tools.initBottomSheet(a, sheetView));

        Resources res = getResources();

        // BACKUP ------------------------------------------------------------
        View backupView = inflater.inflate(R.layout.listview_button, null);
        ListView backupList = backupView.findViewById(R.id.listView);
        TextView backupName = backupView.findViewById(R.id.textName1);
        backupView.findViewById(R.id.textBox1).setVisibility(View.VISIBLE);
        backupList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, res.getStringArray(R.array.backup_list)));
        a.runOnUiThread(() -> backupName.setText(Tools.getBackupFileName()));

        // WIPE ------------------------------------------------------------
        View wipeView = inflater.inflate(R.layout.listview_button, null);
        ListView wipeList = wipeView.findViewById(R.id.listView);
        wipeList.setAdapter(new ArrayAdapter<>(a,
                R.layout.list_check, res.getStringArray(R.array.wipe_list)));

        // DECRYPT ------------------------------------------------------------
        View decryptView = inflater.inflate(R.layout.decrypt_layout, null);
        TextView decryptPass = decryptView.findViewById(R.id.textName1);
        decryptView.findViewById(R.id.btnOpenPattern).setOnClickListener(v -> startActivityForResult(new Intent(a, PatternActivity.class), 400));

        // REBOOT ------------------------------------------------------------
        View rebootView = inflater.inflate(R.layout.listview_button, null);
        ListView rebootList = rebootView.findViewById(R.id.listView);
        String[] reboot_array = res.getStringArray(R.array.reboot_list);
        String[] reboot_cmd = res.getStringArray(R.array.reboot_list_cmd);
        rebootList.setAdapter(new ArrayAdapter<String>(a, R.layout.list_radio, reboot_array) {
            int selectedPosition = 0;

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) v = inflater.inflate(R.layout.list_radio, null);
                RadioButton r = v.findViewById(R.id.title);
                r.setText(reboot_array[pos]);
                r.setChecked(pos == selectedPosition);
                r.setOnClickListener(view -> {
                    selectedPosition = pos;
                    rebootList.setTag(pos);
                    notifyDataSetChanged();
                });
                return v;
            }
        });
        rebootList.setTag(0);

        // ETC ------------------------------------------------------------
        View etcView = inflater.inflate(R.layout.listview_button, null);
        ListView etcList = etcView.findViewById(R.id.listView);
        String[] etc_array = res.getStringArray(R.array.etc_list);
        String[] etc_text = res.getStringArray(R.array.etc_list_mode);
        TextInputLayout etcTextView1 = etcView.findViewById(R.id.textBox1);
        TextInputLayout etcTextView2 = etcView.findViewById(R.id.textBox2);
        TextView etcTextName1 = etcView.findViewById(R.id.textName1);
        TextView etcTextName2 = etcView.findViewById(R.id.textName2);
        etcView.findViewById(R.id.textBox1).setVisibility(View.VISIBLE);
        etcTextView1.setHint(etc_text[0]);
        etcList.setTag(0);

        etcList.setAdapter(new ArrayAdapter<String>(a, R.layout.list_radio, etc_array) {
            int selectedPosition = 0;

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) v = inflater.inflate(R.layout.list_radio, null);
                RadioButton r = v.findViewById(R.id.title);
                r.setText(etc_array[pos]);
                r.setChecked(pos == selectedPosition);
                r.setOnClickListener(view -> {
                    selectedPosition = pos;
                    etcList.setTag(pos);
                    if (pos == 3 || pos == 4) {
                        etcTextView1.setVisibility(View.GONE);
                        etcTextView2.setVisibility(View.GONE);
                    } else if (pos == 5) {
                        etcTextView1.setVisibility(View.VISIBLE);
                        etcTextView2.setVisibility(View.VISIBLE);
                    } else {
                        etcTextView1.setVisibility(View.VISIBLE);
                        etcTextView2.setVisibility(View.GONE);
                    }
                    etcTextView1.setHint(etc_text[pos]);
                    notifyDataSetChanged();
                });
                return v;
            }
        });

        rebootList.setTag(0);

        View installView = inflater.inflate(R.layout.list_select_zip_btn, null);

        // INIT ------------------------------------------------------------
        ViewPager viewPager = sheetView.findViewById(R.id.viewpager);
        fdViewPagerItemAdapter adapter = new fdViewPagerItemAdapter(fdViewPagerItems.with(a)
                .add(R.string.script_zip, installView)
                .add(R.string.script_backup, backupView)
                .add(R.string.script_wipe, wipeView)
                .add(R.string.script_decrypt, decryptView)
                .add(R.string.script_reboot, rebootView)
                .add(R.string.script_etc, etcView)
                .create());
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);

        a.runOnUiThread(() -> viewPager.setAdapter(adapter));

        installView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            startActivityForResult(intent, 10);
        });

        decryptView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            items.add(new RecyclerItems(getString(R.string.script_decrypt),
                    decryptPass.getText().toString(), R.drawable.ic_baseline_lock_open_24, "decrypt " + decryptPass.getText()));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            listEmpty(false);
            dialog.dismiss();
        });

        backupView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            boolean empty = true;
            arg.append("backup ");
            for (byte i = 0; i < backupList.getCount(); i++) {
                CheckBox item = (CheckBox) backupList.getChildAt(i);
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
            listEmpty(false);
            dialog.dismiss();
        });

        wipeView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            boolean empty = true;
            for (byte i = 0; i < wipeList.getCount(); i++) {
                CheckBox item = (CheckBox) wipeList.getChildAt(i);
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
            listEmpty(false);
            dialog.hide();
        });

        rebootView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            items.add(new RecyclerItems(getString(R.string.script_reboot),
                    reboot_array[(int) rebootList.getTag()], R.drawable.ic_round_refresh_24, "reboot " + reboot_cmd[(int) rebootList.getTag()]));
            ORSAdapter.notifyItemInserted(items.size() - 1);
            listEmpty(false);
            dialog.hide();
        });

        //ПОТРЯСТИ VIEW
        etcView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            switch ((int)etcList.getTag()) {
                case 0:
                    items.add(new RecyclerItems(getString(R.string.script_cmd),
                            etcTextName1.getText().toString(), R.drawable.ic_terminal, "cmd " + etcTextName1.getText()));
                    break;
                case 1:
                    items.add(new RecyclerItems(getString(R.string.script_mkdir),
                            etcTextName1.getText().toString(), R.drawable.ic_baseline_folder_open_24, "mkdir " + etcTextName1.getText()));
                    break;
                case 2:
                    items.add(new RecyclerItems(getString(R.string.script_print),
                            etcTextName1.getText().toString(), R.drawable.ic_outline_message_24, "print " + etcTextName1.getText()));
                    break;
                case 3:
                    items.add(new RecyclerItems(getString(R.string.script_sideload), getString(R.string.script_no_params),
                            R.drawable.ic_baseline_devices_24, "sideload"));
                    break;
                case 4:
                    items.add(new RecyclerItems(getString(R.string.script_fixperms), getString(R.string.script_no_params),
                            R.drawable.ic_outline_build_24, "fixperms"));
                    break;
                case 5:
                    items.add(new RecyclerItems(getString(R.string.script_set),
                            etcTextName1.getText() + " = " + etcTextName2.getText(),
                            R.drawable.ic_equals, "set " + etcTextName1.getText() + " " + etcTextName2.getText()));
                    break;
            }
            ORSAdapter.notifyItemInserted(items.size() - 1);
            listEmpty(false);
            dialog.hide();
        });

        a.runOnUiThread(() -> ((SmartTabLayout) sheetView.findViewById(R.id.viewpagertab)).setViewPager(viewPager));

        if (show) {
            dialog.show();
            sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);
        }
    }
}