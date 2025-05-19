package com.example.motophosaique;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);

        File imgFile = new File(item.imagePath);
        if (imgFile.exists()) {
            holder.imageThumb.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        }

        holder.textAlgo.setText("Algo: " + item.algo);
        holder.textTime.setText("Time: " + item.timeSec + "s");

        // 点击进入详情（可扩展）
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            intent.putExtra("imagePath", item.imagePath);
            intent.putExtra("algo", item.algo);
            intent.putExtra("timeSec", item.timeSec);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textAlgo, textTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textAlgo = itemView.findViewById(R.id.textAlgo);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}
