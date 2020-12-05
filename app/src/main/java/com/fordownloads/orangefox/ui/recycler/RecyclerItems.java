package com.fordownloads.orangefox.ui.recycler;

public class RecyclerItems {
        private final String title;
        private final String subtitle;
        private final String id;
        private final int icon;

        public RecyclerItems(String title, String subtitle, int icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.id = null;
            this.icon = icon;
        }

        public RecyclerItems(String title, String subtitle, int icon, String id) {
            this.title = title;
            this.subtitle = subtitle;
            this.id = id;
            this.icon = icon;
        }

        public String getTitle() {
            return this.title;
        }
        public String getSubtitle() {
                return this.subtitle;
        }
        public String getId() {
                return this.id;
        }
        public int getIcon() {
                return this.icon;
        }
}
