package com.fordownloads.orangefox.ui.recycler;
import java.io.Serializable;

public class AdapterStorage implements Serializable {
    RecyclerAdapter adapter;
    public AdapterStorage(RecyclerAdapter adapter) { this.adapter = adapter; }
    public RecyclerAdapter getAdapter() { return adapter; }
}