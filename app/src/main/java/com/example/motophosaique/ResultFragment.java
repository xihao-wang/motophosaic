package com.example.motophosaique;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResultFragment extends Fragment {

    private Bitmap originalBmp;
    private Bitmap generatedBmp;
    private float usedSeconds;
    private String lastHistoryPath;
    private String originalUri;

    private Bitmap blendingBmp;

    public ResultFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Bundle args = getArguments();
        String uriStr    = args != null ? args.getString("photoUri", "") : "";
        int blockSize    = args != null ? args.getInt("blockSize", 16) : 16;
        String algo      = args != null ? args.getString("algo", "average") : "average";
        String type      = args != null ? args.getString("colorMode", "grey") : "grey";
        boolean withRep  = true;
        originalUri      = uriStr;

        PhotoView ivOriginal  = v.findViewById(R.id.ivOriginal);
        PhotoView ivGenerated = v.findViewById(R.id.ivGenerated);

        TextView tvTime     = v.findViewById(R.id.tvTime);
        Button btnOrig      = v.findViewById(R.id.btnOriginal);
        Button btnGen       = v.findViewById(R.id.btnGenerated);
        Button btnShare     = v.findViewById(R.id.btnShare);
        Button btnSave      = v.findViewById(R.id.btnSave);

        List<Button> toggles = Arrays.asList(btnOrig, btnGen);

        v.findViewById(R.id.btnBack)
                .setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        if (!TextUtils.isEmpty(uriStr)) {
            try {
                Uri uri = Uri.parse(uriStr);
                originalBmp = MediaStore.Images.Media
                        .getBitmap(requireContext().getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
                originalBmp = null;
            }
        } else {
            originalBmp = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        }

        if (originalBmp != null) {
            blendingBmp = Bitmap.createBitmap(
                    originalBmp.getWidth(),
                    originalBmp.getHeight(),
                    Config.ARGB_8888
            );
            ivOriginal.setImageBitmap(originalBmp);
        }

        ivGenerated.setAlpha(0f);

        btnOrig.setSelected(true);
        btnGen.setSelected(false);

        btnOrig.setOnClickListener(view -> {
            if (originalBmp == null) return;
            if (generatedBmp == null) {
                ivOriginal.setImageBitmap(originalBmp);
                btnOrig.setSelected(true);
                btnGen.setSelected(false);
                return;
            }
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(500);
            animator.addUpdateListener(animation -> {
                float fraction = (float) animation.getAnimatedValue();
                // fraction=0：pure generatedBmp；fraction=1：pure originalBmp
                blendBitmaps(fraction, 1f - fraction, blendingBmp);
                ivOriginal.setImageBitmap(blendingBmp);
            });
            animator.start();
            btnOrig.setSelected(true);
            btnGen.setSelected(false);
        });

        btnGen.setOnClickListener(view -> {
            if (generatedBmp == null) return;
            if (originalBmp == null) {
                ivOriginal.setImageBitmap(generatedBmp);
                btnOrig.setSelected(false);
                btnGen.setSelected(true);
                return;
            }
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(500);
            animator.addUpdateListener(animation -> {
                float fraction = (float) animation.getAnimatedValue();
                // fraction=0：pure originalBmp；fraction=1：pure generatedBmp
                blendBitmaps(1f - fraction, fraction, blendingBmp);
                ivOriginal.setImageBitmap(blendingBmp);
            });
            animator.start();
            btnOrig.setSelected(false);
            btnGen.setSelected(true);
        });

        btnShare.setOnClickListener(view -> {
            if (generatedBmp == null) return;
            try {
                File cacheFile = new File(requireContext().getCacheDir(), "share.jpg");
                try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                    generatedBmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }
                Uri shareUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        cacheFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Share failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(view -> {
            if (generatedBmp == null) {
                Toast.makeText(requireContext(), "No mosaic to save", Toast.LENGTH_SHORT).show();
                return;
            }
            String filename = String.format(Locale.US,
                    "motophosaique_%s_%s_%.2f.jpg",
                    type, algo, usedSeconds);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Motophosaique");
            Uri uri = requireContext().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    generatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Toast.makeText(requireContext(),
                            "Saved to gallery: Pictures/Motophosaique/" + filename,
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(),
                            "Save failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(),
                        "Cannot create media entry", Toast.LENGTH_SHORT).show();
            }
        });

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                usedSeconds = 0;
                tvTime.setText("Processing...");
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                long start = System.currentTimeMillis();
                File cacheDir = requireActivity().getCacheDir();
                File inFile, outFile;
                if ("cheat_grey".equals(algo)) {
                    inFile  = new File(cacheDir, "input.pgm");
                    outFile = new File(cacheDir, "output.pgm");
                    try {
                        Utils.saveAsPGM(originalBmp, inFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if ("grey".equals(type)) {
                    inFile  = new File(cacheDir, "input.pgm");
                    outFile = new File(cacheDir, "output.pgm");
                    try {
                        Utils.saveAsPGM(originalBmp, inFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    inFile  = new File(cacheDir, "input.ppm");
                    outFile = new File(cacheDir, "output.ppm");
                    try {
                        Utils.saveAsPPM(originalBmp, inFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                int res = ((MainActivity) requireActivity())
                        .generateMosaic(
                                inFile.getAbsolutePath(),
                                outFile.getAbsolutePath(),
                                blockSize,
                                type,
                                algo,
                                withRep
                        );
                if (res != 0) return null;

                Bitmap resultBmp;
                if ("grey".equals(type) || "cheat_grey".equals(algo)) {
                    resultBmp = Utils.decodePGM(outFile);
                } else {
                    resultBmp = BitmapFactory.decodeFile(outFile.getAbsolutePath());
                }
                usedSeconds = (System.currentTimeMillis() - start) / 1000f;
                return resultBmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp == null) {
                    tvTime.setText("Fail (" + algo + ")");
                    return;
                }
                generatedBmp = bmp;

                tvTime.setText(String.format(Locale.US, "Time: %.2fs", usedSeconds));

                if (originalBmp != null) {
                    if (   blendingBmp.getWidth()  != generatedBmp.getWidth()
                            || blendingBmp.getHeight() != generatedBmp.getHeight()) {
                        blendingBmp.recycle();
                        blendingBmp = Bitmap.createBitmap(
                                generatedBmp.getWidth(),
                                generatedBmp.getHeight(),
                                Config.ARGB_8888
                        );
                    }
                    if (originalBmp.getWidth()  != blendingBmp.getWidth()
                            || originalBmp.getHeight() != blendingBmp.getHeight()) {
                        originalBmp = Bitmap.createScaledBitmap(
                                originalBmp,
                                blendingBmp.getWidth(),
                                blendingBmp.getHeight(),
                                false
                        );
                    }
                    ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                    anim.setDuration(500);
                    anim.addUpdateListener(animation -> {
                        float fraction = (float) animation.getAnimatedValue();
                        // fraction=0：pure originalBmp；fraction=1：pure generatedBmp
                        blendBitmaps(1f - fraction, fraction, blendingBmp);
                        ivOriginal.setImageBitmap(blendingBmp);
                    });
                    anim.start();

                    btnOrig.setSelected(false);
                    btnGen.setSelected(true);
                } else {
                    ivOriginal.setImageBitmap(generatedBmp);
                    btnOrig.setSelected(false);
                    btnGen.setSelected(true);
                }

                File picDir = requireContext()
                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String histName = String.format(Locale.US,
                        "mosaic_%s_%s_%.2f.jpg", type, algo, usedSeconds);
                File outFile = new File(picDir, histName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    lastHistoryPath = outFile.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addToHistory(
                        lastHistoryPath,
                        originalUri,
                        type,
                        algo,
                        usedSeconds
                );
            }
        }.execute();
    }


    private void blendBitmaps(float alphaOriginal, float alphaGenerated, Bitmap outBitmap) {
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        paint.setAlpha((int) (alphaOriginal * 255));
        canvas.drawBitmap(originalBmp, 0f, 0f, paint);

        paint.setAlpha((int) (alphaGenerated * 255));
        canvas.drawBitmap(generatedBmp, 0f, 0f, paint);
    }

    private void addToHistory(String imagePath,
                              String originalUri,
                              String type,
                              String algo,
                              float timeSec) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("history_prefs", Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("history_set", new LinkedHashSet<>());
        String entry = TextUtils.join("|",
                Arrays.asList(
                        imagePath,
                        originalUri,
                        type,
                        algo,
                        String.valueOf(timeSec)
                ));
        set.add(entry);
        prefs.edit().putStringSet("history_set", set).apply();
    }
}
