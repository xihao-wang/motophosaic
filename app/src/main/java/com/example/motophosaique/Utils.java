package com.example.motophosaique;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 1) 复制单个 asset 文件
 * 2) 递归复制 asset 目录
 * 3) 解码 P5 PGM -> Bitmap
 */
public class Utils {
    private static final String TAG = "Utils";

    /** 复制 assets 下的单个文件到外部 File */
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

    /** 递归拷贝 assets 目录 */
    public static void copyAssetDir(Context ctx, String assetDir, File outDir) throws IOException {
        AssetManager am = ctx.getAssets();
        String[] children = am.list(assetDir);
        if (children == null || children.length == 0) {
            // 这是个文件
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

    /** 从 P5 PGM 解码成灰度 Bitmap */
    public static Bitmap decodePGM(File pgmFile) {
        try (DataInputStream ds = new DataInputStream(new java.io.FileInputStream(pgmFile))) {
            // 1) 读 magic
            String magic = ds.readLine();
            if (!"P5".equals(magic.trim())) {
                Log.e(TAG, "Unsupported PGM format: " + magic);
                return null;
            }
            // 2) 跳过注释
            String line = ds.readLine();
            while (line != null && line.startsWith("#")) {
                line = ds.readLine();
            }
            // 3) 解析宽高
            String[] parts = line.trim().split("\\s+");
            int width  = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            // 4) 跳过 maxVal
            ds.readLine();
            // 5) 读像素
            int len = width * height;
            byte[] pixels = new byte[len];
            ds.readFully(pixels);
            // 6) 构造 Bitmap
            Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
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
}
