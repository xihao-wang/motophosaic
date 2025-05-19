package com.example.motophosaique;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView recycler = v.findViewById(R.id.historyRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<HistoryItem> data = loadHistory();
        HistoryAdapter adapter = new HistoryAdapter(requireContext(), data);
        recycler.setAdapter(adapter);
    }

    private List<HistoryItem> loadHistory() {
        List<HistoryItem> items = new ArrayList<>();
        File picDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (picDir != null && picDir.exists()) {
            File[] files = picDir.listFiles((dir, name) -> name.endsWith(".jpg"));

            if (files != null) {
                for (File f : files) {
                    String name = f.getName().replace(".jpg", "");
                    String[] parts = name.split("_");
                    if (parts.length >= 3) {
                        String algo = parts[1];
                        float time = Float.parseFloat(parts[2]);
                        items.add(new HistoryItem(f.getAbsolutePath(), algo, time));
                    }
                }
            }
        }

        return items;
    }
}
