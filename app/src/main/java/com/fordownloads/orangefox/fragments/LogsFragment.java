package com.fordownloads.orangefox.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.activity.LogViewActivity;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.utils.Install;
import com.fordownloads.orangefox.utils.Tools;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.app.Activity.RESULT_OK;
import static com.fordownloads.orangefox.consts.LOGS_DIR;

public class LogsFragment extends Fragment {
    ArrayList<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    View rootView;
    TextView _emptyHelp;
    int currentPos = 0;
    ListPopupWindow popup;

    public void onMenuItemClick(AdapterView<?> p, View v, int pos, long id) {
        String fileName = items.get(currentPos).getSubtitle();
        File log = new File(LOGS_DIR, fileName);
        if (pos == 1)
            if (log.delete()) {
                removeItem();
            } else {
                Tools.showSnackbar(getActivity(), rootView.findViewById(R.id.snackbarPlace), R.string.err_file_delete).show();
            }
        else
            Tools.share(getActivity(), fileName, log);
        popup.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (!Install.hasStoragePM(getActivity())) return;
        _emptyHelp.setOnClickListener(null);
        _emptyHelp.setText(R.string.log_empty);
        new Thread(this::refrestList).start();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.delete_logs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            if (items.isEmpty())
                Tools.showSnackbar(getActivity(), rootView.findViewById(R.id.snackbarPlace), R.string.err_logs_deleted).show();
            else {
                try {
                    Files.walk(LOGS_DIR.toPath()).map(Path::toFile).forEach(File::delete);
                } catch (Exception ignored) {}
                if (LOGS_DIR.delete()) {
                    items.clear();
                    adapter.notifyDataSetChanged();
                    _emptyHelp.setVisibility(View.VISIBLE);
                } else
                    Tools.showSnackbar(getActivity(), rootView.findViewById(R.id.snackbarPlace), R.string.err_logs_delete).show();
            }
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_logs, container, false);
        setHasOptionsMenu(true);

        String[] actions = {getString(R.string.share), getString(R.string.delete)};
        popup = new ListPopupWindow(new ContextThemeWrapper(getActivity(), R.style.ThemePopupFill));
        popup.setDropDownGravity(Gravity.CENTER);
        popup.setHeight(ListPopupWindow.WRAP_CONTENT);
        popup.setWidth(ListPopupWindow.MATCH_PARENT);
        popup.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, actions));
        popup.setOnItemClickListener(this::onMenuItemClick);

        adapter = new RecyclerAdapter(getActivity(), items, v ->
        {
            startActivityForResult(new Intent(getActivity(), LogViewActivity.class).putExtra("file_name", ((TextView)v.findViewById(R.id.subtitle)).getText()), 500);
            currentPos = recyclerView.getChildLayoutPosition(v);
        },v ->
        {
            currentPos = recyclerView.getChildLayoutPosition(v);
            popup.setAnchorView(v);
            popup.show();
            return true;
        }
        );
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        _emptyHelp = rootView.findViewById(R.id.emptyHelp);

        if (!Install.hasStoragePM(getActivity())) {
            _emptyHelp.setText(R.string.log_grantpm);
            _emptyHelp.setOnClickListener(v -> Install.requestPM(this));
        }

        new Thread(this::refrestList).start();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 500 && resultCode == RESULT_OK) removeItem();
    }

    private void removeItem() {
        items.remove(currentPos);
        adapter.notifyItemRemoved(currentPos);
        if (items.isEmpty())
            _emptyHelp.setVisibility(View.VISIBLE);
    }

    private void refrestList() {
        try {
            Files.list(LOGS_DIR.toPath()).sorted()
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        String type;
                        if (name.equals("install.log"))
                            type = getString(R.string.log_type_install);
                        else if (name.equals("lastrecoverylog.log"))
                            type = getString(R.string.log_type_last);
                        else if (name.equals("post-install.log"))
                            type = getString(R.string.log_type_postinstall);
                        else if (name.endsWith(".zip"))
                            type = getString(R.string.log_type_zip);
                        else
                            type = getString(R.string.log_type_normal);

                        items.add(new RecyclerItems(type, name, R.drawable.ic_commit));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        requireActivity().runOnUiThread(() -> {
            if (!items.isEmpty())
                _emptyHelp.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        });
    }
}