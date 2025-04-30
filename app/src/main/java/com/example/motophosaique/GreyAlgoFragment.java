package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class GreyAlgoFragment extends Fragment {
    public GreyAlgoFragment() {
        super(R.layout.fragment_algos_grey);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        Button btnAvg   = v.findViewById(R.id.btnAverage);
        Button btnHisto = v.findViewById(R.id.btnHisto);
        Button btnDist  = v.findViewById(R.id.btnDistribution);

        btnAvg  .setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("average"));
        btnHisto.setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("histo"));
        btnDist .setOnClickListener(x -> ((SelectFragment)getParentFragment()).setAlgo("distribution"));
    }

    private void navToResult(View x, int blockSize, String mode, boolean withRep) {
        Bundle args = new Bundle();
        args.putInt("blockSize", blockSize);
        args.putString("mode", mode);
        args.putBoolean("withRep", withRep);
        Navigation.findNavController(x)
                .navigate(R.id.action_select_to_result, args);
    }
}
