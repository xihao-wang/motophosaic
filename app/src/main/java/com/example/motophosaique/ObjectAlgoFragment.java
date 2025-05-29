package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ObjectAlgoFragment extends Fragment {
    public ObjectAlgoFragment() { super(R.layout.fragment_algos_object); }

    @Override public void onViewCreated(View v, Bundle s) {
        super.onViewCreated(v,s);
        Button btnCheatGrey  = v.findViewById(R.id.btnCheatGrey);
        Button btnCheatColor = v.findViewById(R.id.btnCheatColor);
        // 默认
        btnCheatGrey.setSelected(true);
        AlgoConfig.selectedAlgo = "cheat_grey";
        AlgoConfig.isColor     = false;

        btnCheatGrey.setOnClickListener(b -> {
            setSelected(btnCheatGrey, btnCheatColor);
            AlgoConfig.selectedAlgo = "cheat_grey";
            AlgoConfig.isColor     = false;
        });
        btnCheatColor.setOnClickListener(b -> {
            setSelected(btnCheatColor, btnCheatGrey);
            AlgoConfig.selectedAlgo = "cheat_color";
            AlgoConfig.isColor     = true;
        });
    }

    private void setSelected(Button sel, Button... others) {
        sel.setSelected(true);
        for (Button o: others) o.setSelected(false);
    }
}
