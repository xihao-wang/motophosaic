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
import java.util.Arrays;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryAdapter adapter;

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView recycler = v.findViewById(R.id.historyRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistoryAdapter(requireContext(), new ArrayList<>());
        recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        File picDir = requireContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        List<HistoryItem> items = new ArrayList<>();
        if (picDir != null && picDir.exists()) {
            File[] files = picDir.listFiles((dir, name) -> name.endsWith(".jpg"));
            if (files != null && files.length > 0) {
                Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                for (File f : files) {
                    String base = f.getName().replace(".jpg", "");
                    String[] parts = base.split("_");
                    if (parts.length >= 4) {
                        String type = parts[1];
                        String algo = parts[2];
                        float time;
                        try {
                            time = Float.parseFloat(parts[3]);
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        items.add(new HistoryItem(f.getAbsolutePath(), type, algo, time));
                    }
                }
            }
        }

        adapter.updateData(items);
    }
}