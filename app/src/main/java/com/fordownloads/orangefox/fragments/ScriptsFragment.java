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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.fordownloads.orangefox.utils.ParseORS;
import com.fordownloads.orangefox.utils.Tools;
import com.fordownloads.orangefox.viewpager.fdViewPagerItemAdapter;
import com.fordownloads.orangefox.viewpager.fdViewPagerItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ScriptsFragment extends Fragment {
    private ItemTouchHelper mItemTouchHelper;
    ArrayList<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    ORSAdapter ORSAdapter;
    ExtendedFloatingActionButton _createScript;
    BottomSheetDialog dialog = null;
    View _emptyHelp, _emptyArt;
    Button _btnAdd;
    View sheetView, rootView;
    ParseORS parser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(savedInstanceState != null)
            items = savedInstanceState.getParcelableArrayList("items");

        //prerender dialog to avoid lags
        new Thread(() ->  addDialog(getActivity(), false)).start();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList("items", items);
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
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            _emptyArt.setVisibility(View.VISIBLE);
        else
            _emptyArt.setVisibility(View.GONE);
    }

    public void add(RecyclerItems i) {
        items.add(i);
        listEmpty(false, true);
    }

    public void listEmpty(boolean empty, boolean notify) {
        if (empty)
            _createScript.hide();
        else
            _createScript.show();
        _emptyHelp.setVisibility(empty ? View.VISIBLE : View.GONE);
        _btnAdd.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (notify) {
            ORSAdapter.notifyItemInserted(items.size() - 1);
            dialog.dismiss();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scripts, container, false);

        _emptyHelp = rootView.findViewById(R.id.emptyHelp);
        _emptyArt = rootView.findViewById(R.id.emptyArt);
        _createScript = rootView.findViewById(R.id.createScript);
        _createScript.hide();
        _createScript.setOnClickListener(v -> buildScript(Tools.getORS(),false, false));

        _btnAdd = rootView.findViewById(R.id.btnAdd);
        _btnAdd.setOnClickListener(v -> addDialog(getActivity(), true));
        rootView.findViewById(R.id.btnAdd2).setOnClickListener(v -> addDialog(getActivity(), true));

        parser = new ParseORS(getActivity());

        rotateUI(getResources().getConfiguration());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.scripts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            items.clear();
            ORSAdapter.notifyDataSetChanged();
            listEmpty(true, false);
        } else if (id == R.id.load) {
            if (!Shell.rootAccess()) {
                Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_no_pm_root).show();
                return false;
            }
            File orsFile = SuFile.open(Tools.getORS());
            if (!orsFile.exists()){
                Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_no_ors).show();
                return false;
            }
            items.clear();
            try {
                items.addAll(parser.parse(orsFile));
                if (!items.isEmpty()) {
                    listEmpty(false, false);
                    ORSAdapter.notifyDataSetChanged();
                }
                Tools.showSnackbar(getActivity(), getSnackView(), parser.hasErrors() ? R.string.err_ors_bad : R.string.parser_ok).show();
            } catch (IOException e) {
                Tools.reportException(e);
                Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_ors_bad).show();
            }
        } else if (id == R.id.delete) {
            if (Shell.rootAccess()) {
                File orsFile = SuFile.open(Tools.getORS());
                if (!orsFile.exists())
                    Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_no_ors).show();
                else if (orsFile.delete())
                    Tools.showSnackbar(getActivity(), getSnackView(), R.string.deleted_ors).show();
                else
                    Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_file_delete).show();
            } else
                Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_no_pm_root).show();
        } else if (id == R.id.reboot && !Shell.su("reboot recovery").exec().isSuccess())
            Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_reboot_notify).show();
        else if (id == R.id.rebootBootloader && !Shell.su("reboot bootloader").exec().isSuccess())
            Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_reboot_notify).show();

        return false;
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
                    .setAction(R.string.ignore, view -> buildScript(Tools.getORS(), true, false)).show();
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
        if (items.size() != 0)
            listEmpty(false, false);

        ORSAdapter = new ORSAdapter(holder -> mItemTouchHelper.startDrag(holder), items, null);

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ORSAdapter);

        ORSAdapter.setOnItemDismissListener(position -> {
            if (items.size() == 0)
                listEmpty(true, false);
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(ORSAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode != Activity.RESULT_OK || resultData == null) return;
        switch (requestCode) {
            case 10:
                String path = Tools.getFileFromFilePicker(resultData);
                if (path == null) {
                    Tools.showSnackbar(getActivity(), getSnackView(), R.string.err_filepicker).show();
                    dialog.dismiss();
                } else
                    add(new RecyclerItems(getString(R.string.script_zip), path, R.drawable.ic_outline_archive_24, "install " + path));
                break;
            case 400:
                String pass = resultData.getStringExtra("pass");
                add(new RecyclerItems(getString(R.string.script_decrypt), pass, R.drawable.ic_baseline_lock_open_24, "decrypt " + pass));
                break;
        }
    }

    private View getSnackView() {
        return _createScript.getVisibility() == View.VISIBLE ?
            _createScript : rootView.findViewById(R.id.snackbarPlace);
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
        Animation shake = AnimationUtils.loadAnimation(a, R.anim.shake);

        // BACKUP ------------------------------------------------------------
        View backupView = inflater.inflate(R.layout.listview_button, null);
        ListView backupList = backupView.findViewById(R.id.listView);
        TextView backupName = backupView.findViewById(R.id.textName1);
        String[] backupParts = res.getStringArray(R.array.backup_list_cmd);
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

        // MOUNT ------------------------------------------------------------
        View mountView = inflater.inflate(R.layout.listview_mount, null);
        ListView mountList = mountView.findViewById(R.id.listView);
        String[] mount_array = {"System", "Vendor"};
        TextView mountName = mountView.findViewById(R.id.textName1);
        mountList.setAdapter(new ArrayAdapter<String>(a, R.layout.list_radio, mount_array) {
            int selectedPosition = 0;

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) v = inflater.inflate(R.layout.list_radio, null);
                RadioButton r = v.findViewById(R.id.title);
                r.setText(mount_array[pos]);
                r.setChecked(pos == selectedPosition);
                r.setOnClickListener(view -> {
                    selectedPosition = pos;
                    mountList.setTag(pos);
                    mountName.setText(mount_array[pos]);
                    notifyDataSetChanged();
                });
                return v;
            }
        });
        a.runOnUiThread(() -> mountName.setText(mount_array[0]));
        mountList.setTag(0);

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

        View installView = inflater.inflate(R.layout.list_select_zip_btn, null);

        // INIT ------------------------------------------------------------
        ViewPager viewPager = sheetView.findViewById(R.id.viewpager);
        fdViewPagerItemAdapter adapter = new fdViewPagerItemAdapter(fdViewPagerItems.with(a)
                .add(R.string.script_zip, installView)
                .add(R.string.script_backup, backupView)
                .add(R.string.script_wipe, wipeView)
                .add(R.string.script_mount, mountView)
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
            String pass = decryptPass.getText().toString();
            if (pass.equals(""))
                decryptView.findViewById(R.id.textBox1).startAnimation(shake);
            else
                add(new RecyclerItems(getString(R.string.script_decrypt),
                        pass, R.drawable.ic_baseline_lock_open_24, "decrypt " + pass));
        });

        backupView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            StringBuilder arg = new StringBuilder();
            StringBuilder argUser = new StringBuilder();
            boolean empty = true;
            arg.append("backup ");
            for (byte i = 0; i < backupList.getCount(); i++) {
                CheckBox item = (CheckBox) backupList.getChildAt(i);
                if (item != null && item.isChecked()) {
                    arg.append(backupParts[i]);
                    if (i >= 5)
                        argUser.append("\n").append(item.getText());
                    else {
                        argUser.append(item.getText()).append("; ");
                        empty = false;
                    }
                }
            }

            if (empty) {
                backupList.startAnimation(shake);
                return;
            }

            arg.append(" ");
            String name = backupName.getText().toString().trim();
            name = name.equals("") ? Tools.getBackupFileName() : name;
            arg.append(name);
            add(new RecyclerItems(getString(R.string.script_backup),
                    name + "\n" + argUser.toString(),
                    R.drawable.ic_outline_cloud_download_24, arg.toString()));
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

            if (empty)
                wipeList.startAnimation(shake);
            else
                add(new RecyclerItems(getString(R.string.script_wipe), argUser.toString(), R.drawable.ic_delete, arg.toString().trim()));
        });

        rebootView.findViewById(R.id.btnAdd).setOnClickListener(v -> add(new RecyclerItems(getString(R.string.script_reboot),
                reboot_array[(int) rebootList.getTag()], R.drawable.ic_round_refresh_24, "reboot " + reboot_cmd[(int) rebootList.getTag()])));

        mountView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String part = mountName.getText().toString().trim();
            if (part.equals(""))
                mountView.findViewById(R.id.textBox1).startAnimation(shake);
            else
                add(new RecyclerItems(getString(R.string.script_mount),
                        part, R.drawable.ic_outline_dns_24, "mount " + part.toLowerCase()));
        });

        mountView.findViewById(R.id.btnUmount).setOnClickListener(v -> {
            String part = mountName.getText().toString().trim();
            if (part.equals(""))
                mountView.findViewById(R.id.textBox1).startAnimation(shake);
            else
                add(new RecyclerItems(getString(R.string.script_umount),
                        part, R.drawable.ic_outline_umount_24, "umount " + part.toLowerCase()));
        });

        mountView.findViewById(R.id.btnRW).setOnClickListener(v -> add(new RecyclerItems(getString(R.string.script_rw),
                "System", R.drawable.ic_round_sync_24, "remountrw")));

        etcView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            int tag = (int)etcList.getTag();
            String text = etcTextName1.getText().toString().trim();
            if (tag != 3 && tag != 4 && text.equals("")) {
                etcView.findViewById(R.id.textBoxes).startAnimation(shake);
                return;
            }

            switch (tag) {
                case 0:
                    add(new RecyclerItems(getString(R.string.script_cmd), text, R.drawable.ic_terminal, "cmd " + text));
                    break;
                case 1:
                    add(new RecyclerItems(getString(R.string.script_mkdir), text, R.drawable.ic_baseline_folder_open_24, "mkdir " + text));
                    break;
                case 2:
                    add(new RecyclerItems(getString(R.string.script_print), text, R.drawable.ic_outline_message_24, "print " + text));
                    break;
                case 3:
                    add(new RecyclerItems(getString(R.string.script_sideload), getString(R.string.script_no_params), R.drawable.ic_baseline_devices_24, "sideload"));
                    break;
                case 4:
                    add(new RecyclerItems(getString(R.string.script_fixperms), getString(R.string.script_no_params), R.drawable.ic_outline_build_24, "fixperms"));
                    break;
                case 5:
                    add(new RecyclerItems(getString(R.string.script_set),
                            text + " = " + etcTextName2.getText(), R.drawable.ic_equals, "set " + text + " " + etcTextName2.getText()));
                    break;
            }
        });

        a.runOnUiThread(() -> ((SmartTabLayout) sheetView.findViewById(R.id.viewpagertab)).setViewPager(viewPager));

        if (show) {
            dialog.show();
            sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);
        }
    }
}