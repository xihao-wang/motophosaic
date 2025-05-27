package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

public class GreyAlgoFragment extends Fragment {
    private boolean averageGuideShown = false;

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
            AlgoConfig.selectedAlgo = "distribution";
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

    public void showAverageGuideManually(Runnable onFinished) {
        if (averageGuideShown || getView() == null) return;
        Button btn = getView().findViewById(R.id.btnAverage);
        if (btn == null) return;

        TapTargetView.showFor(requireActivity(),
                TapTarget.forView(btn,
                                "Sélectionnez l'algorithme moyen",
                                "Ce bouton applique l'algorithme de moyenne pour créer la mosaïque.")
                        .outerCircleColor(R.color.white)
                        .targetCircleColor(R.color.black)
                        .titleTextColor(android.R.color.black)
                        .descriptionTextColor(android.R.color.black)
                        .cancelable(true)
                        .transparentTarget(true)
                        .tintTarget(false)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        averageGuideShown = true;
                        onFinished.run();
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        onTargetClick(view);
                    }
                }
        );
    }
}
