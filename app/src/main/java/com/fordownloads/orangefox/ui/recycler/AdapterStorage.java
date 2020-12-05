package com.fordownloads.orangefox.ui.recycler;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class AdapterStorage implements Parcelable, Serializable {
    RecyclerAdapter adapter;
    public AdapterStorage(RecyclerAdapter adapter) { this.adapter = adapter; }
    public RecyclerAdapter getAdapter() { return adapter; }

    protected AdapterStorage(Parcel in) {
        //adapter = (RecyclerAdapter) in.readValue(RecyclerAdapter.class.getClassLoader());
        //adapter = ((AdapterSerial)in.readValue(RecyclerAdapter.class.getClassLoader())).getAdapter();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeValue(adapter);
        //dest.writeSerializable(new AdapterSerial(adapter));
    }

    public static final Parcelable.Creator<AdapterStorage> CREATOR = new Parcelable.Creator<AdapterStorage>() {
        @Override
        public AdapterStorage createFromParcel(Parcel in) {
            return new AdapterStorage(in);
        }

        @Override
        public AdapterStorage[] newArray(int size) {
            return new AdapterStorage[size];
        }
    };
}