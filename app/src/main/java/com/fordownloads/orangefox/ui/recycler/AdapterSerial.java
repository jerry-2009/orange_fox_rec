package com.fordownloads.orangefox.ui.recycler;

import java.io.Serializable;

public class AdapterSerial implements Serializable {
    RecyclerAdapter adapter;
    public AdapterSerial(RecyclerAdapter adapter) { this.adapter = adapter; }
    public RecyclerAdapter getAdapter() { return adapter; }
}
