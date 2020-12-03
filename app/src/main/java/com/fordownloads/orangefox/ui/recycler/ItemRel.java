package com.fordownloads.orangefox.ui.recycler;

public class ItemRel {
        private final String title;
        private final String subtitle;
        private final int icon;

        public ItemRel(String title, String subtitle, int icon){
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }

        public String getTitle() {
            return this.title;
        }
        public String getSubtitle() {
                return this.subtitle;
            }
        public int getIcon() {
                return this.icon;
            }
}
