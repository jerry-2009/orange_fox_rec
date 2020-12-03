package com.fordownloads.orangefox;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.fordownloads.orangefox.ui.recycler.DataAdapterRel;
import com.fordownloads.orangefox.ui.recycler.ItemRel;

import java.util.List;

public class App extends Application {

    private static Context mContext;
    private static DataAdapterRel dataAdapterRel;

    public static Context getContext() {
        return mContext;
    }
    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }

    //Эта хуйня нужна для того, чтобы когда переключаешь вкладки, вкладка Info не исчезала к хуям
    //TODO: надо разобраться с этим колхозом
    public static DataAdapterRel getDataAdapterInfo() { return dataAdapterRel; }
    public static void setDataAdapterInfo(DataAdapterRel dataAdapterRel) { App.dataAdapterRel = dataAdapterRel; }

    public static int col(int resource) {
        return ContextCompat.getColor(getContext(), resource);
    }
}