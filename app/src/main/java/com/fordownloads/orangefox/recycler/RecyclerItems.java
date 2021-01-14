package com.fordownloads.orangefox.recycler;

import android.os.Parcel;
import android.os.Parcelable;

public class RecyclerItems implements Parcelable {
        private final String title;
        private final String subtitle;
        private final String data;
        private final int icon;

        public RecyclerItems(String title, String subtitle, int icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.data = null;
            this.icon = icon;
        }

        public RecyclerItems(String title, String subtitle, int icon, String data) {
            this.title = title;
            this.subtitle = subtitle;
            this.data = data;
            this.icon = icon;
        }

        public String getTitle() {
            return this.title;
        }
        public String getSubtitle() {
                return this.subtitle;
        }
        public String getData() {
                return this.data;
        }
        public int getIcon() {
                return this.icon;
        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(data);
        dest.writeInt(icon);
    }

    public static final Creator<RecyclerItems> CREATOR = new Creator<RecyclerItems>() {
        @Override
        public RecyclerItems createFromParcel(Parcel source) {
            String title = source.readString();
            String subtitle = source.readString();
            String data = source.readString();
            int icon = source.readInt();
            return new RecyclerItems(title, subtitle, icon, data);
        }

        @Override
        public RecyclerItems[] newArray(int size) {
            return new RecyclerItems[size];
        }
    };
}
