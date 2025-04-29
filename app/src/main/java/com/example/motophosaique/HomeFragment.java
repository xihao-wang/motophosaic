package com.example.motophosaique;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView tv = v.findViewById(R.id.mosaicPlaceholder);
        String text = "Turn Pixels Into MAGIC";
        SpannableString ss = new SpannableString(text);

        int[] colors = {
                0xFFE84133,
                0xFF4A8E20,
                0xFF3D868D,
                0xFFFFCA00,
                0xFF60BAC2
        };

        int start = text.indexOf("MAGIC");
        for (int i = 0; i < colors.length; i++) {
            ss.setSpan(
                    new ForegroundColorSpan(colors[i]),
                    start + i,
                    start + i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        tv.setText(ss);

        // —— 2) 点击 + 号区域，跳到 SelectFragment ——
        v.findViewById(R.id.importContainer)
                .setOnClickListener(view ->
                        Navigation.findNavController(view)
                                .navigate(R.id.action_home_to_select)
                );
    }
}
