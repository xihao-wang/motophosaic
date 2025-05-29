package com.example.motophosaique;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SelectFragment extends Fragment {
    private int blockSize = 16;
    private String colorMode = "grey";
    private boolean blockGuideShown = false;
    private boolean generateGuideShown = false;
    private static final int[] BLOCK_VALUES = {2, 4, 8, 16, 32, 64, 128};

    public SelectFragment() {
        super(R.layout.fragment_select);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("blockGuideShown", blockGuideShown);
        out.putBoolean("generateGuideShown", generateGuideShown);
        out.putInt("blockSize", blockSize);
        out.putString("colorMode", colorMode);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Restore state if needed
        if (savedInstanceState != null) {
            blockGuideShown    = savedInstanceState.getBoolean("blockGuideShown", false);
            generateGuideShown = savedInstanceState.getBoolean("generateGuideShown", false);
            blockSize          = savedInstanceState.getInt("blockSize", blockSize);
            colorMode          = savedInstanceState.getString("colorMode", colorMode);
        }

        Bundle args = getArguments();
        String uriStr = args != null ? args.getString("photoUri", "") : "";

        // Preview image
        ImageView ivPreview = v.findViewById(R.id.ivPreview);
        if (!uriStr.isEmpty()) ivPreview.setImageURI(Uri.parse(uriStr));

        // Block size label & SeekBar
        TextView tvBlock = v.findViewById(R.id.tvBlockSize);
        SeekBar sb      = v.findViewById(R.id.seekBar);

        // Configure SeekBar: 0..6 maps to BLOCK_VALUES indices
        sb.setMax(BLOCK_VALUES.length - 1);

        // Find the index of the current blockSize in BLOCK_VALUES (default to 3 => 16)
        int defaultIndex = 3;
        for (int i = 0; i < BLOCK_VALUES.length; i++) {
            if (BLOCK_VALUES[i] == blockSize) {
                defaultIndex = i;
                break;
            }
        }
        sb.setProgress(defaultIndex);
        blockSize = BLOCK_VALUES[defaultIndex];
        tvBlock.setText("Size Of Block: " + blockSize);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Map progress (0..6) to the actual block size
                blockSize = BLOCK_VALUES[progress];
                tvBlock.setText("Size Of Block: " + blockSize);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        TabLayout tabMode = v.findViewById(R.id.tabMode);
        ViewPager2 vpAlgo = v.findViewById(R.id.vpAlgo);
        vpAlgo.setAdapter(new AlgoPagerAdapter(this));
        new TabLayoutMediator(tabMode, vpAlgo, (tab, pos) ->
                tab.setText(pos==0?"Grey":pos==1?"Color":"Cheating")
        ).attach();

        vpAlgo.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int pos) {
                colorMode = pos==0?"grey":pos==1?"color":"object";
            }
        });
        vpAlgo.setCurrentItem(0, false);

        v.findViewById(R.id.btnBack).setOnClickListener(x ->
                Navigation.findNavController(x).popBackStack()
        );

        v.findViewById(R.id.btnGenerate).setOnClickListener(x -> {
            Bundle b = new Bundle();
            b.putString("photoUri",  uriStr);
            b.putInt("blockSize",    blockSize);
            b.putString("colorMode",  colorMode);
            b.putString("algo",       AlgoConfig.selectedAlgo);
            b.putBoolean("withRep",   AlgoConfig.withRep);
            Navigation.findNavController(x)
                    .navigate(R.id.action_select_to_result, b);
        });

        boolean showGuide = args != null && args.getBoolean("showBlockSizeGuide", false);
        if (showGuide && !blockGuideShown) {
            blockGuideShown = true;
            TapTargetView.showFor(requireActivity(),
                    TapTarget.forView(tvBlock,
                                    "Adjust Block Size",
                                    "Drag this to set the mosaic block size.")
                            .outerCircleColor(R.color.white)
                            .targetCircleColor(R.color.black)
                            .titleTextColor(android.R.color.black)
                            .descriptionTextColor(android.R.color.black)
                            .cancelable(true),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            vpAlgo.setCurrentItem(0, true);
                            vpAlgo.post(() -> {
                                Fragment f = getChildFragmentManager()
                                        .findFragmentByTag("f0");
                                if (f instanceof GreyAlgoFragment) {
                                    ((GreyAlgoFragment)f)
                                            .showAverageGuideManually(() -> {
                                                showGenerateGuideManually(() -> {
                                                    Navigation.findNavController(v)
                                                            .popBackStack(R.id.homeFragment, false);
                                                });
                                            });
                                }
                            });
                        }
                    }
            );
        }
    }

    public void showGenerateGuideManually(Runnable onFinished) {
        if (generateGuideShown || getView() == null) return;
        View btn = getView().findViewById(R.id.btnGenerate);
        if (btn == null) return;

        generateGuideShown = true;
        TapTargetView.showFor(requireActivity(),
                TapTarget.forView(btn,
                                "Generate Mosaic",
                                "Appuyez ici pour générer votre image mosaïque.")
                        .outerCircleColor(R.color.white)
                        .targetCircleColor(R.color.black)
                        .titleTextColor(android.R.color.black)
                        .descriptionTextColor(android.R.color.black)
                        .cancelable(false)
                        .transparentTarget(true)
                        .tintTarget(false)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Parfait!")
                                .setMessage("Vous savez maintenant comment générer une mosaïque.")
                                .setCancelable(false)
                                .setPositiveButton("Let's go", (dialog, which) -> {
                                    dialog.dismiss();
                                    onFinished.run();
                                })
                                .show();
                    }
                }
        );
    }
}
