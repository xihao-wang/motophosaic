package com.example.motophosaique;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class SelectFragment extends Fragment {
    public SelectFragment() {
        super(R.layout.fragment_select);
    }

    // 外层管理的参数
    private int    blockSize = 16;
    private String colorMode;      // "grey" / "color" / "object"
    private String algo      = "average";
    private boolean withRep  = false; // 暂时如果需要分发

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // —— 返回按钮 ——
        v.findViewById(R.id.btnBack)
                .setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        // —— 预览原图 ——
        ImageView ivPrev = v.findViewById(R.id.ivPreview);
        File cache = requireActivity().getCacheDir();
        Bitmap preview = Utils.decodePGM(new File(cache, "input.pgm"));
        if (preview != null) ivPrev.setImageBitmap(preview);

        // —— BlockSize SeekBar ——
        TextView tvBlock = v.findViewById(R.id.tvBlockSize);
        SeekBar  sb      = v.findViewById(R.id.seekBar);
        sb.setProgress(blockSize);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                blockSize = Math.max(1, p);
                tvBlock.setText("Size Of Block: " + blockSize);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s)  {}
        });

        // —— TabLayout + ViewPager2 管理 “灰度/彩色/目标” 三个子 Fragment ——
        TabLayout  tabMode = v.findViewById(R.id.tabMode);
        ViewPager2 vpAlgo  = v.findViewById(R.id.vpAlgo);
        vpAlgo.setAdapter(new AlgoPagerAdapter(this));

        // 设置 Tab 标题
        new TabLayoutMediator(tabMode, vpAlgo, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("Grey");   break;
                case 1: tab.setText("Color");  break;
                case 2: tab.setText("Object"); break;
            }
        }).attach();

        // 根据选中的 tab 切换 colorMode 值
        tabMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: colorMode = "grey";   break;
                    case 1: colorMode = "color";  break;
                    case 2: colorMode = "object"; break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        // 初始化为第一个“Grey”
        colorMode = "grey";

        // —— 点击 Generate 跳转并传参 ——
        v.findViewById(R.id.btnGenerate)
                .setOnClickListener(x -> {
                    Bundle args = new Bundle();
                    args.putInt   ("blockSize", blockSize);
                    args.putString("colorMode", colorMode);
                    args.putString("algo",      algo);
                    args.putBoolean("withRep",  withRep);
                    Navigation.findNavController(x)
                            .navigate(R.id.action_select_to_result, args);
                });
    }

    /** 供子 Fragment（算法选择页）调用，设置最终选中的 algo */
    public void setAlgo(String a) {
        this.algo = a;
    }
}
