package com.fordownloads.orangefox.viewpager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.ogaclejapan.smarttablayout.utils.ViewPagerItem;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItems;

import java.lang.ref.WeakReference;

public class fdViewPagerItemAdapter extends PagerAdapter {

    private final fdViewPagerItems pages;
    private final SparseArrayCompat<WeakReference<View>> holder;
    private final LayoutInflater inflater;

    public fdViewPagerItemAdapter(fdViewPagerItems pages) {
        this.pages = pages;
        this.holder = new SparseArrayCompat<>(pages.size());
        this.inflater = LayoutInflater.from(pages.getContext());
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = getPagerItem(position).initiate(inflater, container);
        container.addView(view);
        holder.put(position, new WeakReference<View>(view));
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        holder.remove(position);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getPagerItem(position).getTitle();
    }

    @Override
    public float getPageWidth(int position) {
        return getPagerItem(position).getWidth();
    }

    public View getPage(int position) {
        final WeakReference<View> weakRefItem = holder.get(position);
        return (weakRefItem != null) ? weakRefItem.get() : null;
    }

    protected fdViewPagerItem getPagerItem(int position) {
        return pages.get(position);
    }
}
