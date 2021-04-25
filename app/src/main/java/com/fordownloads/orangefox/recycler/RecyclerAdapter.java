package com.fordownloads.orangefox.recycler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.fordownloads.orangefox.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<RecyclerItems> items, itemsAll;
    private final OnClickListener listener;
    private final View.OnLongClickListener listenerHold;
    
    public void filter(CharSequence q) {
        items.clear();
        String text;

        if(q.length() == 0)
            items.addAll(itemsAll);
        else
            for (RecyclerItems item : itemsAll)
                if (item.getSubtitle().toLowerCase().contains(text = q.toString().toLowerCase()) || item.getTitle().toLowerCase().contains(text))
                    items.add(item);

        notifyDataSetChanged();
    }

    public RecyclerAdapter(Context context, List<RecyclerItems> items, OnClickListener listener, View.OnLongClickListener listenerHold) {
        this.items = items;
        this.itemsAll = new ArrayList<>(items);
        this.listener = listener;
        this.listenerHold = listenerHold;
        this.inflater = LayoutInflater.from(context);
    }
    public RecyclerAdapter(Context context, List<RecyclerItems> items, OnClickListener listener) {
        this.items = items;
        this.itemsAll = new ArrayList<>(items);
        this.listener = listener;
        this.listenerHold = null;
        this.inflater = LayoutInflater.from(context);
    }
    public RecyclerAdapter(Context context, List<RecyclerItems> items) {
        this.items = items;
        this.itemsAll = new ArrayList<>(items);
        this.listener = null;
        this.listenerHold = null;
        this.inflater = LayoutInflater.from(context);
    }
    @NotNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        if (listener != null)
            view.setOnClickListener(listener);
        if (listenerHold != null)
        view.setOnLongClickListener(listenerHold);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        RecyclerItems item = items.get(position);
        holder.iconView.setImageResource(item.getIcon());
        holder.titleView.setText(item.getTitle());
        holder.subtitleView.setText(item.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView, subtitleView;
        ViewHolder(View view){
            super(view);
            iconView = view.findViewById(R.id.icon);
            titleView = view.findViewById(R.id.title);
            subtitleView = view.findViewById(R.id.subtitle);
        }
    }
}
