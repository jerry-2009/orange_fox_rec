package com.fordownloads.orangefox.ui.recycler;

public class ItemRel {

        private String title;
        private String subtitle;
        private int icon;

        public ItemRel(String title, String subtitle, int icon){
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return this.subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public int getIcon() {
            return this.icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }
}
