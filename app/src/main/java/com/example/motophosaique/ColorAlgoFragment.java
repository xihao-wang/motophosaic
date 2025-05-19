package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ColorAlgoFragment extends Fragment {
    public ColorAlgoFragment() {
        super(R.layout.fragment_algos_color);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAvgColor = view.findViewById(R.id.btnAvgColor);
        Button btnHistoColor = view.findViewById(R.id.btnHistoColor);
        Button btnDistColor = view.findViewById(R.id.btnDistColor);

        // ✅ 初次默认选中（可选）
        if (!AlgoConfig.hasUserSelected) {
            setSelected(btnAvgColor, btnHistoColor, btnDistColor);
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = true;
        }

        btnAvgColor.setOnClickListener(v -> {
            setSelected(btnAvgColor, btnHistoColor, btnDistColor);
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = true;
            AlgoConfig.hasUserSelected = true;
        });

        btnHistoColor.setOnClickListener(v -> {
            setSelected(btnHistoColor, btnAvgColor, btnDistColor);
            AlgoConfig.selectedAlgo = "histo";
            AlgoConfig.isColor = true;
            AlgoConfig.hasUserSelected = true;
        });

        btnDistColor.setOnClickListener(v -> {
            setSelected(btnDistColor, btnAvgColor, btnHistoColor);
            AlgoConfig.selectedAlgo = "distribute";
            AlgoConfig.isColor = true;
            AlgoConfig.hasUserSelected = true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Button btnAvgColor = requireView().findViewById(R.id.btnAvgColor);
        Button btnHistoColor = requireView().findViewById(R.id.btnHistoColor);
        Button btnDistColor = requireView().findViewById(R.id.btnDistColor);

        if (btnAvgColor.isSelected()) {
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = true;
        } else if (btnHistoColor.isSelected()) {
            AlgoConfig.selectedAlgo = "histo";
            AlgoConfig.isColor = true;
        } else if (btnDistColor.isSelected()) {
            AlgoConfig.selectedAlgo = "distribute";
            AlgoConfig.isColor = true;
        }
    }

    private void setSelected(Button selected, Button... others) {
        selected.setSelected(true);
        for (Button b : others) b.setSelected(false);
    }
}
