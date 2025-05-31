package com.example.motophosaique;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.io.IOException;
import java.io.InputStream;

public class SelectFragment extends Fragment {
    private int blockSize = 16;
    private String colorMode = "grey";
    private boolean blockGuideShown = false;
    private boolean generateGuideShown = false;
    private static final int[] BLOCK_VALUES = {2, 4, 8, 16, 32, 64, 128};

    private GridOverlayView gridOverlay;
    private ImageView ivPreview;
    private MaterialCardView previewCard;
    private int origImageWidth = 0;
    private int origImageHeight = 0;

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

        if (savedInstanceState != null) {
            blockGuideShown    = savedInstanceState.getBoolean("blockGuideShown", false);
            generateGuideShown = savedInstanceState.getBoolean("generateGuideShown", false);
            blockSize          = savedInstanceState.getInt("blockSize", blockSize);
            colorMode          = savedInstanceState.getString("colorMode", colorMode);
        }

        Bundle args = getArguments();
        String uriStr = args != null ? args.getString("photoUri", "") : "";

        ivPreview   = v.findViewById(R.id.ivPreview);
        gridOverlay = v.findViewById(R.id.gridOverlay);
        previewCard = v.findViewById(R.id.previewCard);

        if (!TextUtils.isEmpty(uriStr)) {
            try {
                Uri uri = Uri.parse(uriStr);
                InputStream is = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (bmp != null) {
                    origImageWidth  = bmp.getWidth();
                    origImageHeight = bmp.getHeight();
                    ivPreview.setImageBitmap(bmp);
                }
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (origImageWidth == 0 || origImageHeight == 0) {
            Bitmap defaultBmp = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            if (defaultBmp != null) {
                origImageWidth  = defaultBmp.getWidth();
                origImageHeight = defaultBmp.getHeight();
                ivPreview.setImageBitmap(defaultBmp);
            }
        }

        if (origImageWidth > 0 && origImageHeight > 0) {
            ConstraintLayout.LayoutParams lp =
                    (ConstraintLayout.LayoutParams) previewCard.getLayoutParams();
            lp.dimensionRatio = origImageWidth + ":" + origImageHeight;
            previewCard.setLayoutParams(lp);
        }

        gridOverlay.setImageInfo(origImageWidth, origImageHeight);
        gridOverlay.setBlockSize(blockSize);

        TextView tvBlock = v.findViewById(R.id.tvBlockSize);
        SeekBar sb      = v.findViewById(R.id.seekBar);

        sb.setMax(BLOCK_VALUES.length - 1);
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
                blockSize = BLOCK_VALUES[progress];
                tvBlock.setText("Size Of Block: " + blockSize);
                gridOverlay.setBlockSize(blockSize);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        TabLayout tabMode = v.findViewById(R.id.tabMode);
        ViewPager2 vpAlgo  = v.findViewById(R.id.vpAlgo);
        vpAlgo.setAdapter(new AlgoPagerAdapter(this));

        new TabLayoutMediator(tabMode, vpAlgo, (tab, pos) -> {
            TextView custom = new TextView(requireContext());
            custom.setTextSize(16);
            custom.setTypeface(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL);
            switch (pos) {
                case 0: custom.setText("Grey");    break;
                case 1: custom.setText("Color");   break;
                default: custom.setText("Cheating");
            }
            custom.setGravity(android.view.Gravity.CENTER);
            custom.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tab.setCustomView(custom);
        }).attach();

        tabMode.setSelectedTabIndicatorColor(
                ContextCompat.getColor(requireContext(), R.color.indicator_grey)
        );
        TabLayout.Tab firstTab = tabMode.getTabAt(0);
        if (firstTab != null && firstTab.getCustomView() instanceof TextView) {
            ((TextView) firstTab.getCustomView())
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.indicator_grey));
        }
        tabMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                int indicatorColor;
                switch (pos) {
                    case 0:
                        indicatorColor = R.color.indicator_grey;
                        colorMode = "grey";
                        break;
                    case 1:
                        indicatorColor = R.color.indicator_blue;
                        colorMode = "color";
                        break;
                    default:
                        indicatorColor = R.color.indicator_red;
                        colorMode = "object";
                        break;
                }
                tabMode.setSelectedTabIndicatorColor(
                        ContextCompat.getColor(requireContext(), indicatorColor)
                );
                if (tab.getCustomView() instanceof TextView) {
                    ((TextView) tab.getCustomView())
                            .setTextColor(ContextCompat.getColor(requireContext(), indicatorColor));
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() instanceof TextView) {
                    ((TextView) tab.getCustomView())
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                }
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        vpAlgo.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int pos) {
                TabLayout.Tab tab = tabMode.getTabAt(pos);
                if (tab != null) tab.select();
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
                                    ((GreyAlgoFragment) f)
                                            .showAverageGuideManually(() -> {
                                                showGenerateGuideManually(() -> {
                                                    Navigation.findNavController(v)
                                                            .popBackStack(R.id.homeFragment, false);
                                                });
                                            });
                                }
                            });
                        }
                    });
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
                                "Appuyez ici pour générer une mosaïque.")
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
                                .setMessage("Vous savez maintenant comment générer la mosaïque.")
                                .setCancelable(false)
                                .setPositiveButton("Let's go", (dialog, which) -> {
                                    dialog.dismiss();
                                    onFinished.run();
                                })
                                .show();
                    }
                });
    }
}
