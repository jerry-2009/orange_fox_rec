package com.fordownloads.orangefox.ui.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fordownloads.orangefox.R;

import java.util.List;

public class DataAdapterRel extends RecyclerView.Adapter<DataAdapterRel.ViewHolder> {

    private LayoutInflater inflater;
    private List<ItemRel> items;

    public DataAdapterRel(Context context, List<ItemRel> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public DataAdapterRel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_release, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapterRel.ViewHolder holder, int position) {
        ItemRel item = items.get(position);
        holder.iconView.setImageResource(item.getIcon());
        holder.titleView.setText(item.getTitle());
        holder.subtitleView.setText(item.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView, subtitleView;
        ViewHolder(View view){
            super(view);
            iconView = (ImageView)view.findViewById(R.id.icon);
            titleView = (TextView) view.findViewById(R.id.title);
            subtitleView = (TextView) view.findViewById(R.id.subtitle);
        }
    }
}
