package com.fordownloads.orangefox.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class AutoHeightListView extends ListView {

    public AutoHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHeightListView(Context context) {
        super(context);
    }

    public AutoHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}