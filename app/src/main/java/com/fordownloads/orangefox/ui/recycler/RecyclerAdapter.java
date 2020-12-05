package com.fordownloads.orangefox.ui.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.fordownloads.orangefox.R;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<RecyclerItems> items;
    private final OnClickListener listener;

    public RecyclerAdapter(Context context, List<RecyclerItems> items, OnClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }
    @NotNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_release, parent, false);
        if (listener != null)
            view.setOnClickListener(listener);
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
