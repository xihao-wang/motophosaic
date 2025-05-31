package com.example.motophosaique;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private Uri photoUri;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private AlgoViewModel vm;

    private ValueAnimator colorAnimator;
    private int[] colors = {
            0xFFE84133,
            0xFF4A8E20,
            0xFF3D868D,
            0xFFFFCA00,
            0xFF60BAC2
    };

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(AlgoViewModel.class);

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dispatchTakePictureIntent();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dispatchPickImageIntent();
                    } else {
                        Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        navigateToSelectFragment();
                    } else {
                        Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        photoUri = uri;
                        navigateToSelectFragment();
                    } else {
                        Toast.makeText(requireContext(), "Failed to pick image", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        vm.selectedAlgo.setValue("average");
        vm.withRep.setValue(false);

        TextView tv = v.findViewById(R.id.mosaicPlaceholder);
        String text = "Turn Pixels Into MAGIC";
        int start = text.indexOf("MAGIC");

        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(500);
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            SpannableString spanString = new SpannableString(text);

            int[] nextColors = new int[colors.length];
            System.arraycopy(colors, 1, nextColors, 0, colors.length - 1);
            nextColors[colors.length - 1] = colors[0];

            ArgbEvaluator evaluator = new ArgbEvaluator();
            for (int i = 0; i < colors.length; i++) {
                int interpolated = (int) evaluator.evaluate(fraction, colors[i], nextColors[i]);
                spanString.setSpan(
                        new ForegroundColorSpan(interpolated),
                        start + i, start + i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            tv.setText(spanString);

            if (fraction >= 0.999f) {
                System.arraycopy(nextColors, 0, colors, 0, colors.length);
            }
        });
        colorAnimator.start();

        View importContainer = v.findViewById(R.id.importContainer);
        importContainer.setOnClickListener(view -> showPhotoOptionsDialog());

        ConstraintLayout helpOverlay = v.findViewById(R.id.helpOverlay);
        CardView helpPopup           = v.findViewById(R.id.helpPopup);
        ImageView helpButton         = v.findViewById(R.id.helpButton);

        helpButton.setOnClickListener(_ignored -> helpOverlay.setVisibility(View.VISIBLE));

        helpOverlay.setOnClickListener(_ignored -> helpOverlay.setVisibility(View.GONE));

        helpPopup.setOnClickListener(_ignored -> {
        //
        });

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isFirst = prefs.getBoolean("first_launch", true);
        if (isFirst) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Guide pour débutant")
                    .setMessage("Souhaitez-vous un guide pour démarrer ?")
                    .setPositiveButton("Oui", (dlg, w) -> {
                        TapTargetView.showFor(requireActivity(),
                                TapTarget.forView(importContainer,
                                                "Import a Photo",
                                                "Appuyez ici pour choisir ou prendre une photo.")
                                        .outerCircleColor(R.color.white)
                                        .targetCircleColor(R.color.black)
                                        .titleTextColor(android.R.color.black)
                                        .descriptionTextColor(android.R.color.black)
                                        .cancelable(true),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        navigateToSelectFragmentWithDefaultImage();
                                    }
                                    @Override
                                    public void onTargetCancel(TapTargetView view) {
                                        super.onTargetCancel(view);
                                        navigateToSelectFragmentWithDefaultImage();
                                    }
                                });
                        prefs.edit().putBoolean("first_launch", false).apply();
                    })
                    .setNegativeButton("Non", (dlg, w) ->
                            prefs.edit().putBoolean("first_launch", false).apply()
                    )
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (colorAnimator != null && colorAnimator.isRunning()) {
            colorAnimator.cancel();
        }
    }

    private void showPhotoOptionsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sélectionner une photo")
                .setItems(new String[]{"Choisir une photo", "Prendre une photo"},
                        (dlg, which) -> {
                            if (which == 0) {
                                checkStoragePermission();
                            } else {
                                checkCameraPermission();
                            }
                        })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            dispatchPickImageIntent();
        } else {
            storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createTempImageFile();
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(requireContext(),
                    "Erreur création fichier",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void dispatchPickImageIntent() {
        pickImageLauncher.launch("image/*");
    }

    private File createTempImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        String fileName = "tmp_" + ts + "_";
        File cacheDir = requireActivity().getCacheDir();
        return File.createTempFile(fileName, ".jpg", cacheDir);
    }

    private void navigateToSelectFragment() {
        Bundle args = new Bundle();
        args.putString("photoUri", photoUri.toString());
        args.putBoolean("showBlockSizeGuide", false);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_select, args);
    }

    private void navigateToSelectFragmentWithDefaultImage() {
        String pkg = requireContext().getPackageName();
        Uri def = Uri.parse("android.resource://" + pkg + "/" + R.drawable.background);
        Bundle args = new Bundle();
        args.putString("photoUri", def.toString());
        args.putBoolean("showBlockSizeGuide", true);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_select, args);
    }
}
