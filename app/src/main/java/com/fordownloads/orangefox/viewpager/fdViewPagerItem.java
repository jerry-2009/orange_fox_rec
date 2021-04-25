package com.fordownloads.orangefox.viewpager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.utils.PagerItem;

public class fdViewPagerItem extends PagerItem {

    private final View resource;

    protected fdViewPagerItem(CharSequence title, View resource) {
        super(title, DEFAULT_WIDTH);
        this.resource = resource;
    }

    public static fdViewPagerItem of(CharSequence title, View resource) {
        return new fdViewPagerItem(title, resource);
    }

    public View initiate(LayoutInflater inflater, ViewGroup container) {
        return resource;
    }

}