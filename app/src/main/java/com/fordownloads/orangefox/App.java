package com.fordownloads.orangefox;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

public class App extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }

    public static int col(int resource) {
        return ContextCompat.getColor(getContext(), resource);
    }
}