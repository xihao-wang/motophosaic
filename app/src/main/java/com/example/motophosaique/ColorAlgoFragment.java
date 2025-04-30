package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class ColorAlgoFragment extends Fragment {
    public ColorAlgoFragment() {
        super(R.layout.fragment_algos_color);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        Button btnAvg   = v.findViewById(R.id.btnAvgColor);
        Button btnHisto = v.findViewById(R.id.btnHistoColor);
        Button btnDist  = v.findViewById(R.id.btnDistColor);

        btnAvg  .setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("color_average"));
        btnHisto.setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("color_histo"));
        btnDist .setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("color_distribution"));
    }
}
