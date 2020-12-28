package com.fordownloads.orangefox.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.recycler.AdapterStorage;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;

import org.jetbrains.annotations.NotNull;

public class RecyclerFragment extends Fragment {
    private RecyclerAdapter adapter;
    private RecyclerView _recycler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = ((AdapterStorage)getArguments().getParcelable("adapter")).getAdapter();
        return inflater.inflate(R.layout.fragment_recycler, container, false);
    }

    public static Bundle arguments(AdapterStorage param) {
        return new Bundler().putParcelable("adapter", param).get();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _recycler = view.findViewById(R.id.releaseRecycler);
        _recycler.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        if (_recycler != null) //prevent mem leak
            _recycler.setAdapter(null);
        super.onDestroyView();
    }
}