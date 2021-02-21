package com.fordownloads.orangefox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.activity.LogViewActivity;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.utils.Tools;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class LogsFragment extends Fragment {
    ArrayList<RecyclerItems> items = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    View rootView;
    int currentPos = 0;

    public boolean onMenuItemClick(MenuItem item) {
        String fileName = items.get(currentPos).getSubtitle();
        File log = new File(Environment.getExternalStorageDirectory(), "Fox/logs/" + fileName);
        if (item.getItemId() == R.id.delete)
            if (!log.delete()) {
                Toast.makeText(getActivity(), R.string.err_file_delete, Toast.LENGTH_LONG).show();
            } else {
                items.remove(currentPos);
                adapter.notifyItemRemoved(currentPos);
            }
        else if (item.getItemId() == R.id.share)
            Tools.share(getActivity(), fileName, log);
        return true;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_logs, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(myToolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.bnav_logs);

        adapter = new RecyclerAdapter(getActivity(), items, v ->
        {
            startActivityForResult(new Intent(getActivity(), LogViewActivity.class).putExtra("file_name", ((TextView)v.findViewById(R.id.subtitle)).getText()), 500);
            currentPos = recyclerView.getChildLayoutPosition(v);
        },v ->
        {
            currentPos = recyclerView.getChildLayoutPosition(v);
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.getMenuInflater().inflate(R.menu.share, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onMenuItemClick);
            popup.show();
            return true;
        }
        );
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        new Thread(this::refrestList).start();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 500 && resultCode == RESULT_OK) {
            items.remove(currentPos);
            adapter.notifyItemRemoved(currentPos);
        }
    }

    private void refrestList() {
        try {
            Files.list(new File(Environment.getExternalStorageDirectory(), "Fox/logs").toPath()).sorted()
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
        getActivity().runOnUiThread(() -> {
            if (!items.isEmpty())
                rootView.findViewById(R.id.emptyHelp).setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        });
    }
}