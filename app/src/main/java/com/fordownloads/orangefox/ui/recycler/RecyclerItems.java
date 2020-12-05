package com.fordownloads.orangefox.ui.recycler;

import android.os.Parcel;
import android.os.Parcelable;

public class RecyclerItems implements Parcelable {
        private final String title;
        private final String subtitle;
        private final int icon;

        public RecyclerItems(String title, String subtitle, int icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }

    protected RecyclerItems(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        icon = in.readInt();
    }

    public static final Creator<RecyclerItems> CREATOR = new Creator<RecyclerItems>() {
        @Override
        public RecyclerItems createFromParcel(Parcel in) {
            return new RecyclerItems(in);
        }

        @Override
        public RecyclerItems[] newArray(int size) {
            return new RecyclerItems[size];
        }
    };

    public String getTitle() {
            return this.title;
        }
        public String getSubtitle() {
                return this.subtitle;
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
            dest.writeInt(icon);
        }
}
