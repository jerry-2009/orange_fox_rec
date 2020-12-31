package com.fordownloads.orangefox.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.consts;
import com.fordownloads.orangefox.recycler.AdapterStorage;
import com.fordownloads.orangefox.recycler.RecyclerAdapter;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.fordownloads.orangefox.recycler.ors.ORSAdapter;
import com.fordownloads.orangefox.recycler.ors.SimpleItemTouchHelperCallback;
import com.fordownloads.orangefox.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ScriptsFragment extends Fragment {
    private ItemTouchHelper mItemTouchHelper;
    List<RecyclerItems> items = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scripts, container, false);
        Toolbar myToolbar = rootView.findViewById(R.id.appToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setTitle(R.string.bnav_scripts);

        rootView.findViewById(R.id.createScript).setOnClickListener(v -> {
            for (RecyclerItems item : items) {

                Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        items.add(new RecyclerItems("title1", "text", R.drawable.ic_outline_build_24));
        items.add(new RecyclerItems("title2", "text", R.drawable.ic_outline_build_24));
        items.add(new RecyclerItems("title3", "text", R.drawable.ic_outline_build_24));

        ORSAdapter adapter = new ORSAdapter(getActivity(), holder -> mItemTouchHelper.startDrag(holder), items, v -> {
            addDialog(getActivity());
            Toast.makeText(getActivity(), ((TextView)v.findViewById(R.id.text)).getText(), Toast.LENGTH_SHORT).show();
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public BottomSheetDialog addDialog(FragmentActivity a) {
        View sheetView = a.getLayoutInflater().inflate(R.layout.dialog_add, null);
        BottomSheetDialog dialog = Tools.initBottomSheet(a, sheetView);

        FragmentPagerItems.Creator pageList = FragmentPagerItems.with(a);
        pageList.add(R.string.rel_info, RecyclerFragment.class, RecyclerFragment.arguments(new AdapterStorage(new RecyclerAdapter(a, items, null))));


        ViewPager viewPager = sheetView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentPagerItemAdapter(a.getSupportFragmentManager(), pageList.create()));
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);
        ((SmartTabLayout)sheetView.findViewById(R.id.viewpagertab)).setViewPager(viewPager);

        dialog.show();
        sheetView.animate().setInterpolator(consts.intr).setDuration(800).translationY(0);

        return dialog;
    }
}