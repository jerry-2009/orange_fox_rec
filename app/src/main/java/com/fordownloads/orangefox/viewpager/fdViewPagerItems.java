package com.fordownloads.orangefox.viewpager;
import android.content.Context;
import android.view.View;

import androidx.annotation.StringRes;

import com.ogaclejapan.smarttablayout.utils.PagerItems;

public class fdViewPagerItems extends PagerItems<fdViewPagerItem> {
    public fdViewPagerItems(Context context) {
        super(context);
    }

    public static Creator with(Context context) {
        return new Creator(context);
    }

    public static class Creator {
        private final fdViewPagerItems items;
        public Creator(Context context) {
            items = new fdViewPagerItems(context);
        }
        public fdViewPagerItems.Creator add(@StringRes int title, View view) {
            items.add(fdViewPagerItem.of(items.getContext().getString(title), view));
            return this;
        }
        public fdViewPagerItems create() {
            return items;
        }
    }
}
