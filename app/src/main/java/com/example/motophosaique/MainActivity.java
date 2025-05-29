package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static { System.loadLibrary("photomosaic-lib"); }

    public native int generateMosaic(
            String inputPath,
            String outputPath,
            int blockSize,
            String mode,
            String algo,
            boolean withRepetition
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File cache = getCacheDir();
        Utils.copyAsset(this, "input.pgm", new File(cache, "input.pgm"));
        try {
            Utils.copyAssetDir(this, "img_tile",       new File(cache, "img_tile"));
            Utils.copyAssetDir(this, "img_tile_color", new File(cache, "img_tile_color"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        View navHome = findViewById(R.id.navHome);
        View navHistory = findViewById(R.id.navHistory);
        NavController navController = Navigation.findNavController(this, R.id.nav_host);

        navHome.setOnClickListener(v -> {
            navHome.setSelected(true);
            navHistory.setSelected(false);
            navController.navigate(R.id.homeFragment);
        });

        navHistory.setOnClickListener(v -> {
            navHome.setSelected(false);
            navHistory.setSelected(true);
            navController.navigate(R.id.historyFragment);
        });

    }
}
