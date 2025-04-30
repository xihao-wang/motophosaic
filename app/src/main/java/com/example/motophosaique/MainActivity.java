package com.example.motophosaique;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static { System.loadLibrary("photomosaic-lib"); }

    // JNI 接口，注意这里多了 mode 和 withRep 两个参数
    public native int generateMosaic(
            String inputPath,
            String outputPath,
            int blockSize,
            String mode,
            boolean withRepetition
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 拷贝 assets/input.pgm 和 img_tile* 目录到 cacheDir
        File cache = getCacheDir();
        Utils.copyAsset(this, "input.pgm", new File(cache, "input.pgm"));
        try {
            Utils.copyAssetDir(this, "img_tile",       new File(cache, "img_tile"));
            Utils.copyAssetDir(this, "img_tile_color", new File(cache, "img_tile_color"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
