package com.example.motophosaique;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.io.IOException;
import java.io.InputStream;

public class SelectFragment extends Fragment {

    private static final String TAG = "SelectFragment";

    public SelectFragment() {
        super(R.layout.fragment_select);
    }

    private int blockSize = 16;
    private String mode = "grey";

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // —— 1) 显示图片预览 ——
        ImageView ivPreview = v.findViewById(R.id.ivPreview);
        File cache = requireActivity().getCacheDir();
        File inPgm = new File(cache, "input.pgm");

        // 检查是否有新的 photoUri
        Bundle args = getArguments();
        if (args != null && args.containsKey("photoUri")) {
            String photoUriString = args.getString("photoUri");
            Log.d(TAG, "Received photoUri: " + photoUriString);
            try {
                Uri photoUri = Uri.parse(photoUriString);
                Bitmap bitmap = loadBitmapFromUri(photoUri);
                if (bitmap != null) {
                    // 清空旧图片
                    ivPreview.setImageBitmap(null);
                    // 保存为 PGM 并显示
                    Utils.saveAsPGM(bitmap, inPgm);
                    ivPreview.setImageBitmap(bitmap);
                    Log.d(TAG, "New photo saved as PGM and displayed");
                } else {
                    Log.e(TAG, "Failed to load bitmap from URI: " + photoUriString);
                    // 回退：加载现有 PGM
                    loadExistingPgm(ivPreview, inPgm);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error processing photoUri: " + e.getMessage());
                // 回退：加载现有 PGM
                loadExistingPgm(ivPreview, inPgm);
            }
        } else {
            Log.w(TAG, "No photoUri in arguments, loading existing PGM");
            // 没有新照片，加载现有 PGM
            loadExistingPgm(ivPreview, inPgm);
        }

        // —— 2) 返回 Home ——
        v.findViewById(R.id.btnBack)
                .setOnClickListener(x ->
                        Navigation.findNavController(x).popBackStack()
                );

        // —— 3) SeekBar 控制 blockSize ——
        TextView tvBlock = v.findViewById(R.id.tvBlockSize);
        SeekBar sb = v.findViewById(R.id.seekBar);
        sb.setProgress(blockSize);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blockSize = Math.max(1, progress);
                tvBlock.setText("Size Of Block: " + blockSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // —— 4) 模式单选 ——
        RadioGroup rg = v.findViewById(R.id.rgMode);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGrey) mode = "grey";
            if (checkedId == R.id.rbColor) mode = "color";
            if (checkedId == R.id.rbObject) mode = "object";
        });

        // —— 5) Generate → Result 跳转 ——
        v.findViewById(R.id.btnGenerate)
                .setOnClickListener(x -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("blockSize", blockSize);
                    bundle.putString("mode", mode);
                    Navigation.findNavController(x)
                            .navigate(R.id.action_select_to_result, bundle);
                });
    }

    // 加载现有 PGM 文件（回退逻辑）
    private void loadExistingPgm(ImageView ivPreview, File inPgm) {
        if (inPgm.exists()) {
            Bitmap bmp = Utils.decodePGM(inPgm);
            if (bmp != null) {
                ivPreview.setImageBitmap(bmp);
                Log.d(TAG, "Loaded existing PGM: " + inPgm.getAbsolutePath());
            } else {
                Log.w(TAG, "Failed to decode existing PGM");
            }
        } else {
            Log.w(TAG, "No existing PGM file found at: " + inPgm.getAbsolutePath());
        }
    }

    // 从 URI 加载 Bitmap
    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open InputStream for URI: " + uri);
                return null;
            }
            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: " + uri);
            }
            return bitmap;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error closing InputStream: " + e.getMessage());
                }
            }
        }
    }
}