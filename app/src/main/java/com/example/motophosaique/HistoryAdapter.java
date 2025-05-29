package com.example.motophosaique;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<HistoryItem> items;

    public HistoryAdapter(Context context, List<HistoryItem> items) {
        this.context = context;
        this.items = items;
    }

    public void updateData(List<HistoryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);

        File imgFile = new File(item.getImagePath());
        if (imgFile.exists()) {
            holder.imageThumb.setImageBitmap(
                    BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        }

        holder.textType.setText("Type: " + item.getType());
        holder.textAlgo.setText("Algo: " + item.getAlgo());
        holder.textTime.setText("Time: " + item.getTimeSec() + "s");

        holder.itemView.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("imagePath", item.getImagePath());
            b.putString("originalUri", item.getOriginalUri());
            b.putString("type",      item.getType());
            b.putString("algo",      item.getAlgo());
            b.putFloat ("timeSec",   item.getTimeSec());
            Navigation.findNavController(v)
                    .navigate(R.id.historyDetailFragment, b);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textType, textAlgo, textTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textType   = itemView.findViewById(R.id.textType);
            textAlgo   = itemView.findViewById(R.id.textAlgo);
            textTime   = itemView.findViewById(R.id.textTime);
        }
    }
}
