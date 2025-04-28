package com.example.motophosaique;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;

public class SelectFragment extends Fragment {

    public SelectFragment() {
        super(R.layout.fragment_select);
    }

    private int blockSize = 16;
    private String mode    = "grey";

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // —— 1) 显示原图预览 PGM ——
        ImageView ivPreview = v.findViewById(R.id.ivPreview);
        File cache = requireActivity().getCacheDir();
        File inPgm = new File(cache, "input.pgm");
        Bitmap bmp = Utils.decodePGM(inPgm);
        if (bmp != null) {
            ivPreview.setImageBitmap(bmp);
        }

        // —— 2) 返回 Home ——
        v.findViewById(R.id.btnBack)
                .setOnClickListener(x ->
                        Navigation.findNavController(x).popBackStack()
                );

        // —— 3) SeekBar 控制 blockSize ——
        TextView tvBlock = v.findViewById(R.id.tvBlockSize);
        SeekBar sb       = v.findViewById(R.id.seekBar);
        sb.setProgress(blockSize);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blockSize = Math.max(1, progress);
                tvBlock.setText("Size Of Block: " + blockSize);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar){}
            @Override public void onStopTrackingTouch(SeekBar seekBar){}
        });

        // —— 4) 模式单选 ——
        RadioGroup rg = v.findViewById(R.id.rgMode);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGrey)   mode = "grey";
            if (checkedId == R.id.rbColor)  mode = "color";
            if (checkedId == R.id.rbObject) mode = "object";
        });

        // —— 5) Generate → Result 跳转 ——
        v.findViewById(R.id.btnGenerate)
                .setOnClickListener(x -> {
                    Bundle args = new Bundle();
                    args.putInt("blockSize", blockSize);
                    args.putString("mode", mode);
                    Navigation.findNavController(x)
                            .navigate(R.id.action_select_to_result, args);
                });
    }
}
