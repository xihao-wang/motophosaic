package com.example.motophosaique;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Set;

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
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("history_prefs", Context.MODE_PRIVATE);
        Set<String> entries = prefs.getStringSet("history_set", null);
        List<HistoryItem> items = new ArrayList<>();
        if (entries != null) {
            for (String e : entries) {
                String[] p = e.split("\\|", 5);
                if (p.length == 5) {
                    items.add(new HistoryItem(
                            p[0],          // imagePath
                            p[1],          // originalUri
                            p[2],          // type
                            p[3],          // algo
                            Float.parseFloat(p[4])
                    ));
                }
            }
        }
        adapter.updateData(items);
    }
}