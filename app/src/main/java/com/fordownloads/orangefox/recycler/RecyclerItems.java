package com.fordownloads.orangefox.recycler;

public class RecyclerItems {
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
}
