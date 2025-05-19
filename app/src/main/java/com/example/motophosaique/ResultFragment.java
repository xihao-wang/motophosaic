package com.example.motophosaique;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ResultFragment extends Fragment {

    public ResultFragment() {
        super(R.layout.fragment_result);
    }

    private Bitmap originalBmp;
    private Bitmap generatedBmp;
    private float usedSeconds;

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        int blockSize = getArguments().getInt("blockSize", 16);
        String algo = AlgoConfig.selectedAlgo;
        boolean withRep = AlgoConfig.withRep;

        PhotoView photoView = v.findViewById(R.id.ivResult);
        TextView tvTime = v.findViewById(R.id.tvTime);
        Button btnOriginal = v.findViewById(R.id.btnOriginal);
        Button btnGenerated = v.findViewById(R.id.btnGenerated);
        Button btnShare = v.findViewById(R.id.btnShare);
        Button btnSave = v.findViewById(R.id.btnSave);
        List<Button> toggleButtons = Arrays.asList(btnOriginal, btnGenerated);

        v.findViewById(R.id.btnBack).setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        File cache = requireActivity().getCacheDir();
        File inFile = new File(cache, "input.pgm");
        File outFile = new File(cache, "output.pgm");

        originalBmp = Utils.decodePGM(inFile);

        btnOriginal.setSelected(false);
        btnGenerated.setSelected(true);
        if (originalBmp != null) {
            photoView.setImageBitmap(originalBmp);
        }

        btnOriginal.setOnClickListener(view -> {
            if (originalBmp != null) {
                photoView.setImageBitmap(originalBmp);
            }
            for (Button b : toggleButtons) b.setSelected(false);
            btnOriginal.setSelected(true);
        });

        btnGenerated.setOnClickListener(view -> {
            if (generatedBmp != null) {
                photoView.setImageBitmap(generatedBmp);
            }
            for (Button b : toggleButtons) b.setSelected(false);
            btnGenerated.setSelected(true);
        });

        btnShare.setOnClickListener(view -> {
            if (generatedBmp == null) return;

            try {
                File file = new File(requireContext().getCacheDir(), "share_image.jpg");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    generatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }

                Uri uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        file
                );

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share via"));

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Share failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(view -> {
            if (generatedBmp == null) return;

            String savedImageURL = MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    generatedBmp,
                    "mosaic_" + System.currentTimeMillis(),
                    "Generated Mosaic Image"
            );

            if (savedImageURL != null) {
                Toast.makeText(requireContext(), "Saved to gallery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
            }
        });

        new AsyncTask<Void, Void, Bitmap>() {
            long start, elapsed;

            @Override
            protected void onPreExecute() {
                start = System.currentTimeMillis();
            }

            @Override
            protected Bitmap doInBackground(Void... u) {
                int res = ((MainActivity) requireActivity())
                        .generateMosaic(
                                inFile.getAbsolutePath(),
                                outFile.getAbsolutePath(),
                                blockSize,
                                algo,
                                withRep
                        );
                elapsed = System.currentTimeMillis() - start;
                usedSeconds = elapsed / 1000f;
                if (res != 0) return null;
                return Utils.decodePGM(outFile);
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp != null) {
                    generatedBmp = bmp;
                    photoView.setImageBitmap(generatedBmp);
                    tvTime.setText("Waiting time: " + usedSeconds + "s");

                    // ✅ 自动保存用于历史缩略图展示
                    File picDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                    if (picDir != null) {
                        String filename = "mosaic_" + algo + "_" + usedSeconds + ".jpg";
                        File outFile = new File(picDir, filename);
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            generatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            Toast.makeText(requireContext(), "Saved to history", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "History save failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    btnOriginal.setSelected(false);
                    btnGenerated.setSelected(true);
                } else {
                    tvTime.setText("fail（algo=" + algo + "）read the log");
                }
            }
        }.execute();
    }
}
