package com.fordownloads.orangefox.recycler;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class AdapterStorage implements Parcelable, Serializable {
    RecyclerAdapter adapter;
    public AdapterStorage(RecyclerAdapter adapter) { this.adapter = adapter; }
    public RecyclerAdapter getAdapter() { return adapter; }

    protected AdapterStorage() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    public static final Parcelable.Creator<AdapterStorage> CREATOR = new Parcelable.Creator<AdapterStorage>() {
        @Override
        public AdapterStorage createFromParcel(Parcel in) {
            return new AdapterStorage();
        }

        @Override
        public AdapterStorage[] newArray(int size) {
            return new AdapterStorage[size];
        }
    };
}