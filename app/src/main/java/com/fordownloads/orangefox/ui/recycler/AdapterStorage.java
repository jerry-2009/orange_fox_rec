package com.fordownloads.orangefox.ui.recycler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fordownloads.orangefox.R;

import java.util.List;

public class AdapterStorage implements Parcelable {
    List<RecyclerItems> list;
    public AdapterStorage(List<RecyclerItems> list) { this.list = list; }
    public List<RecyclerItems> getList() { return list; }
    protected AdapterStorage(Parcel in) {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(list);
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