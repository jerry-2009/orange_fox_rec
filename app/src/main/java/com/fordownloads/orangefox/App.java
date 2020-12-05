package com.fordownloads.orangefox;

import android.app.Application;
import android.content.Context;
import androidx.core.content.ContextCompat;
import com.fordownloads.orangefox.ui.recycler.DataAdapterRel;

public class App extends Application {

    private static Context mContext;
    private static DataAdapterRel dataAdapterRel;
    private static DataAdapterRel dataAdapterStable;
    private static DataAdapterRel dataAdapterBeta;

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

    public static DataAdapterRel getDataAdapterStable() { return dataAdapterStable; }
    public static void setDataAdapterStable(DataAdapterRel dataAdapterStable) { App.dataAdapterStable = dataAdapterStable; }

    public static DataAdapterRel getDataAdapterBeta() { return dataAdapterBeta; }
    public static void setDataAdapterBeta(DataAdapterRel dataAdapterBeta) { App.dataAdapterBeta = dataAdapterBeta; }

    public static int col(int resource) {
        return ContextCompat.getColor(getContext(), resource);
    }
}