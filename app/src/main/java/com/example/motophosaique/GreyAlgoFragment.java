package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GreyAlgoFragment extends Fragment {
    public GreyAlgoFragment() {
        super(R.layout.fragment_algos_grey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAverage = view.findViewById(R.id.btnAverage);
        Button btnHisto = view.findViewById(R.id.btnHisto);
        Button btnDistribution = view.findViewById(R.id.btnDistribution);


        if (!AlgoConfig.hasUserSelected) {
            setSelected(btnAverage, btnHisto, btnDistribution);
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = false;
        }

        btnAverage.setOnClickListener(v -> {
            setSelected(btnAverage, btnHisto, btnDistribution);
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = false;
            AlgoConfig.hasUserSelected = true;
        });

        btnHisto.setOnClickListener(v -> {
            setSelected(btnHisto, btnAverage, btnDistribution);
            AlgoConfig.selectedAlgo = "histo";
            AlgoConfig.isColor = false;
            AlgoConfig.hasUserSelected = true;
        });

        btnDistribution.setOnClickListener(v -> {
            setSelected(btnDistribution, btnAverage, btnHisto);
            AlgoConfig.selectedAlgo = "distribute";
            AlgoConfig.isColor = false;
            AlgoConfig.hasUserSelected = true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Button btnAverage = requireView().findViewById(R.id.btnAverage);
        Button btnHisto = requireView().findViewById(R.id.btnHisto);
        Button btnDistribution = requireView().findViewById(R.id.btnDistribution);

        if (btnAverage.isSelected()) {
            AlgoConfig.selectedAlgo = "average";
            AlgoConfig.isColor = false;
        } else if (btnHisto.isSelected()) {
            AlgoConfig.selectedAlgo = "histo";
            AlgoConfig.isColor = false;
        } else if (btnDistribution.isSelected()) {
            AlgoConfig.selectedAlgo = "distribute";
            AlgoConfig.isColor = false;
        }
    }

    private void setSelected(Button selected, Button... others) {
        selected.setSelected(true);
        for (Button b : others) b.setSelected(false);
    }
}
