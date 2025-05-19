package com.example.motophosaique;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SelectFragment extends Fragment {

    private static final String TAG = "SelectFragment";

    private int blockSize = 16;
    private String colorMode = "grey";
    private String algo = "average";
    private boolean withRep = false;

    public SelectFragment() {
        super(R.layout.fragment_select);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);


        ImageView ivPreview = v.findViewById(R.id.ivPreview);
        File cache = requireActivity().getCacheDir();
        File inPgm = new File(cache, "input.pgm");


        Bundle args = getArguments();
        if (args != null && args.containsKey("photoUri")) {
            String photoUriString = args.getString("photoUri");
            Log.d(TAG, "Received photoUri: " + photoUriString);
            try {
                Uri photoUri = Uri.parse(photoUriString);
                Bitmap bitmap = loadBitmapFromUri(photoUri);
                if (bitmap != null) {
                    ivPreview.setImageBitmap(bitmap);
                    // 保存为 PGM 并显示
                    Utils.saveAsPGM(bitmap, inPgm);
                    Log.d(TAG, "New photo saved as PGM and displayed");
                } else {
                    Log.e(TAG, "Failed to load bitmap from URI: " + photoUriString);
                    loadExistingPgm(ivPreview, inPgm);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error processing photoUri: " + e.getMessage());
                loadExistingPgm(ivPreview, inPgm);
            }
        } else {
            Log.w(TAG, "No photoUri in arguments, loading existing PGM");
            loadExistingPgm(ivPreview, inPgm);
        }

        v.findViewById(R.id.btnBack)
                .setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        Bitmap preview = Utils.decodePGM(inPgm);
        if (preview != null) ivPreview.setImageBitmap(preview);

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
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        TabLayout tabMode = v.findViewById(R.id.tabMode);
        ViewPager2 vpAlgo = v.findViewById(R.id.vpAlgo);
        vpAlgo.setAdapter(new AlgoPagerAdapter(this));

        new TabLayoutMediator(tabMode, vpAlgo, (tab, pos) -> {
            switch (pos) {
                case 0:
                    tab.setText("Grey");
                    break;
                case 1:
                    tab.setText("Color");
                    break;
                case 2:
                    tab.setText("Object");
                    break;
            }
        }).attach();


        tabMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        colorMode = "grey";
                        break;
                    case 1:
                        colorMode = "color";
                        break;
                    case 2:
                        colorMode = "object";
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        colorMode = "grey";


        v.findViewById(R.id.btnGenerate)
                .setOnClickListener(x -> {
                    Bundle generateArgs = new Bundle();
                    generateArgs.putInt("blockSize", blockSize);
                    generateArgs.putString("colorMode", colorMode);
                    generateArgs.putString("algo", algo);
                    generateArgs.putBoolean("withRep", withRep);
                    Navigation.findNavController(x)
                            .navigate(R.id.action_select_to_result, generateArgs);
                });
    }


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

    public void setAlgo(String algo) {
        this.algo = algo;
        Log.d(TAG, "Algo set to: " + algo);
    }
}
