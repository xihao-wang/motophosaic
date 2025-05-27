package com.example.motophosaique;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    private static final String TAG = "Utils";


    public static boolean copyAsset(Context ctx, String assetName, File outFile) {
        try (InputStream is = ctx.getAssets().open(assetName);
             FileOutputStream os = new FileOutputStream(outFile)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "copyAsset failed: " + assetName, e);
            return false;
        }
    }


    public static void copyAssetDir(Context ctx, String assetDir, File outDir) throws IOException {
        AssetManager am = ctx.getAssets();
        String[] children = am.list(assetDir);
        if (children == null || children.length == 0) {
            File parent = outDir.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            try (InputStream is = am.open(assetDir);
                 FileOutputStream os = new FileOutputStream(outDir)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            }
        } else {
            // 这是个目录
            if (!outDir.exists()) outDir.mkdirs();
            for (String child : children) {
                String childAssetPath = assetDir + "/" + child;
                File  childOutFile    = new File(outDir, child);
                copyAssetDir(ctx, childAssetPath, childOutFile);
            }
        }
    }


    public static Bitmap decodePGM(File pgmFile) {
        try (DataInputStream ds = new DataInputStream(new java.io.FileInputStream(pgmFile))) {
            String magic = ds.readLine();
            if (!"P5".equals(magic.trim())) {
                Log.e(TAG, "Unsupported PGM format: " + magic);
                return null;
            }
            String line = ds.readLine();
            while (line != null && line.startsWith("#")) {
                line = ds.readLine();
            }
            String[] parts = line.trim().split("\\s+");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            ds.readLine();
            int len = width * height;
            byte[] pixels = new byte[len];
            ds.readFully(pixels);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int v = pixels[idx++] & 0xFF;
                    int color = 0xFF000000 | (v << 16) | (v << 8) | v;
                    bmp.setPixel(x, y, color);
                }
            }
            return bmp;
        } catch (IOException e) {
            Log.e(TAG, "decodePGM failed: " + pgmFile.getAbsolutePath(), e);
            return null;
        }
    }

    public static void saveAsPGM(Bitmap bitmap, File outputFile) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        StringBuilder header = new StringBuilder();
        header.append("P5\n");
        header.append(width).append(" ").append(height).append("\n");
        header.append("255\n");


        byte[] pixels = new byte[width * height];
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int gray = (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
                pixels[idx++] = (byte) (gray & 0xFF);
            }
        }


        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(header.toString().getBytes());
            fos.write(pixels);
            Log.d(TAG, "PGM saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "saveAsPGM failed: " + outputFile.getAbsolutePath(), e);
            throw e;
        }
    }
}