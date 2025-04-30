package com.example.motophosaique;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private Uri photoUri;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        if (photoUri == null) {
                            dispatchTakePictureIntent();
                        } else {
                            dispatchPickImageIntent();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
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
                        Toast.makeText(requireContext(), "Failed to select photo", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView tv = v.findViewById(R.id.mosaicPlaceholder);
        String text = "Turn Pixels Into MAGIC";
        SpannableString ss = new SpannableString(text);

        int[] colors = {
                0xFFE84133,
                0xFF4A8E20,
                0xFF3D868D,
                0xFFFFCA00,
                0xFF60BAC2
        };

        int start = text.indexOf("MAGIC");
        for (int i = 0; i < colors.length; i++) {
            ss.setSpan(
                    new ForegroundColorSpan(colors[i]),
                    start + i,
                    start + i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        tv.setText(ss);

        v.findViewById(R.id.importContainer).setOnClickListener(view -> {
            Log.d("HomeFragment", "importContainer clicked, showing options");
            showPhotoOptionsDialog();
        });
    }

    private void showPhotoOptionsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Photo")
                .setItems(new String[]{"Choose a Photo", "Take a Photo"}, (dialog, which) -> {
                    Log.d("HomeFragment", "Option selected: " + which);
                    if (which == 0) {
                        checkStoragePermission();
                    } else {
                        checkCameraPermission();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED) {
            dispatchPickImageIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.motophosaique.fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchPickImageIntent() {
        pickImageLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void navigateToSelectFragment() {
        Log.d("HomeFragment", "Navigating with photoUri: " + photoUri);
        Bundle args = new Bundle();
        args.putString("photoUri", photoUri.toString());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_select, args);
    }
}
