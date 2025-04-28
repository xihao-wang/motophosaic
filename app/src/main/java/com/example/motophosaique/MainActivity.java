package com.example.motophosaique;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("photomosaic-lib");
    }
    // JNI 接口
    public native int generateMosaic(
            String inputPath,
            String outputPath,
            int blockSize);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1) 拷贝 assets/input.pgm
        File cache = getCacheDir();
        File inPgm = new File(cache, "input.pgm");
        Utils.copyAsset(this, "input.pgm", inPgm);

        // 2) 拷贝整个 img_tile 目录
        try {
            Utils.copyAssetDir(this, "img_tile", new File(cache, "img_tile"));
            Utils.copyAssetDir(this, "img_tile_color", new File(cache, "img_tile_color"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
