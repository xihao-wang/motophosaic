package com.example.motophosaique;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private String    originalUri;

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
        //  boolean withRep  = args != null && args.getBoolean("withRep", true);
        boolean withRep = true;
        originalUri = uriStr;

        PhotoView photoView = v.findViewById(R.id.ivResult);
        TextView tvTime     = v.findViewById(R.id.tvTime);
        Button btnOrig      = v.findViewById(R.id.btnOriginal);
        Button btnGen       = v.findViewById(R.id.btnGenerated);
        Button btnShare     = v.findViewById(R.id.btnShare);
        Button btnSave      = v.findViewById(R.id.btnSave);
        List<Button> toggles = Arrays.asList(btnOrig, btnGen);

        v.findViewById(R.id.btnBack)
                .setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        // 加载原图
        if (!uriStr.isEmpty()) {
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

        if (originalBmp != null) photoView.setImageBitmap(originalBmp);
        btnOrig.setSelected(true);
        btnGen .setSelected(false);

        // 切换原图/生成图
        btnOrig.setOnClickListener(view -> {
            if (originalBmp != null) photoView.setImageBitmap(originalBmp);
            toggles.forEach(b -> b.setSelected(false));
            btnOrig.setSelected(true);
        });
        btnGen.setOnClickListener(view -> {
            if (generatedBmp != null) photoView.setImageBitmap(generatedBmp);
            toggles.forEach(b -> b.setSelected(false));
            btnGen.setSelected(true);
        });

        // 分享逻辑保持不变
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

        // 保存到本地相册逻辑保持不变
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

        // 异步生成马赛克：根据 type 分支处理
        new AsyncTask<Void, Void, Bitmap>() {
            @Override protected void onPreExecute() {
                usedSeconds = 0;
                tvTime.setText("Processing...");
            }

            @Override protected Bitmap doInBackground(Void... voids) {
                long start = System.currentTimeMillis();
                File cacheDir = requireActivity().getCacheDir();

                File inFile;
                File outFile;

                if ("cheat_grey".equals(algo)) {
                    // Cheat Grey —— 一定要 PGM
                    inFile  = new File(cacheDir, "input.pgm");
                    outFile = new File(cacheDir, "output.pgm");
                    try {
                        Utils.saveAsPGM(originalBmp, inFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if ("grey".equals(type)) {
                    // 灰度模式：PGM 路径
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

                // 调用 native 生成
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

                // 解码输出
                Bitmap resultBmp;
                if ("grey".equals(type)|| "cheat_grey".equals(algo)) {
                    resultBmp = Utils.decodePGM(outFile);
                } else {
                    resultBmp = BitmapFactory.decodeFile(outFile.getAbsolutePath());
                }

                usedSeconds = (System.currentTimeMillis() - start) / 1000f;
                return resultBmp;
            }

            @Override protected void onPostExecute(Bitmap bmp) {
                if (bmp == null) {
                    tvTime.setText("Fail (" + algo + ")");
                    return;
                }
                generatedBmp = bmp;
                photoView.setImageBitmap(bmp);
                tvTime.setText(String.format(Locale.US, "Time: %.2fs", usedSeconds));

                // 缓存历史路径以备备用（不影响本地相册保存逻辑）
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

                btnGen.setSelected(true);
            }
        }.execute();
    }
    private void addToHistory(String imagePath,
                              String originalUri,
                              String type,
                              String algo,
                              float timeSec) {
        // prefs 中存一个 Set<String>，每条用 | 分隔
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("history_prefs", Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("history_set", new LinkedHashSet<>());
        // 新记录：
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

