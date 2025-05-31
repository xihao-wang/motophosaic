package com.example.motophosaique;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryDetailFragment extends Fragment {
    private Bitmap mosaicBmp;
    private Bitmap originalBmp;
    private Bitmap blendingBmp;
    private boolean showingMosaic = true;

    public HistoryDetailFragment() {
        super(R.layout.fragment_history_detail);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        View btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view ->
                Navigation.findNavController(view).popBackStack()
        );

        PhotoView iv = v.findViewById(R.id.detailImage);
        TextView tvType      = v.findViewById(R.id.detailType);
        TextView tvAlgo      = v.findViewById(R.id.detailAlgo);
        TextView tvTime      = v.findViewById(R.id.detailTime);
        TextView tvTimestamp = v.findViewById(R.id.detailTimestamp);

        Bundle args = getArguments();
        String mosaicPath   = "";
        String originalUri  = "";
        String type         = "";
        String algo         = "";
        float  timeSec      = 0f;
        String timestamp    = "";

        if (args != null) {
            mosaicPath  = args.getString("imagePath", "");
            originalUri = args.getString("originalUri", "");
            type        = args.getString("type", "");
            algo        = args.getString("algo", "");
            timeSec     = args.getFloat("timeSec", 0f);
            timestamp   = args.getString("timestamp", "");
        }

        if (!mosaicPath.isEmpty()) {
            File f = new File(mosaicPath);
            if (f.exists()) {
                mosaicBmp = BitmapFactory.decodeFile(mosaicPath);
            }
        }
        if (!originalUri.isEmpty()) {
            try {
                originalBmp = MediaStore.Images.Media.getBitmap(
                        requireContext().getContentResolver(),
                        Uri.parse(originalUri)
                );
            } catch (IOException e) {
                originalBmp = mosaicBmp;
            }
        } else {
            originalBmp = mosaicBmp;
        }

        if (mosaicBmp == null) {
            Toast.makeText(requireContext(), "Cannot load mosaic image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (originalBmp == null) {
            originalBmp = mosaicBmp;
        }

        blendingBmp = Bitmap.createBitmap(
                mosaicBmp.getWidth(),
                mosaicBmp.getHeight(),
                Config.ARGB_8888
        );

        if (originalBmp.getWidth()  != mosaicBmp.getWidth() ||
                originalBmp.getHeight() != mosaicBmp.getHeight()) {
            originalBmp = Bitmap.createScaledBitmap(
                    originalBmp,
                    mosaicBmp.getWidth(),
                    mosaicBmp.getHeight(),
                    false
            );
        }

        iv.setImageBitmap(mosaicBmp);
        showingMosaic = true;

        iv.setOnClickListener(view -> {
            if (showingMosaic) {
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                animator.setDuration(500);
                animator.addUpdateListener(animation -> {
                    float fraction = (float) animation.getAnimatedValue();
                    blendBitmaps(1f - fraction, fraction, blendingBmp);
                    iv.setImageBitmap(blendingBmp);
                });
                animator.start();
            } else {
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                animator.setDuration(500);
                animator.addUpdateListener(animation -> {
                    float fraction = (float) animation.getAnimatedValue();
                    blendBitmaps(fraction, 1f - fraction, blendingBmp);
                    iv.setImageBitmap(blendingBmp);
                });
                animator.start();
            }
            showingMosaic = !showingMosaic;
        });

        tvType.setText("Type: " + type);
        tvAlgo.setText("Algo: " + algo);
        tvTime.setText("Time: " + timeSec + "s");

        if (timestamp == null || timestamp.isEmpty()) {
            String currentTime = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm", Locale.getDefault()
            ).format(new Date());
            timestamp = currentTime;
        }
        tvTimestamp.setText("Generated: " + timestamp);
        tvTimestamp.setVisibility(View.VISIBLE);
    }


    private void blendBitmaps(float alphaMosaic, float alphaOriginal, Bitmap outBitmap) {
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        paint.setAlpha((int) (alphaMosaic * 255));
        canvas.drawBitmap(mosaicBmp, 0f, 0f, paint);

        paint.setAlpha((int) (alphaOriginal * 255));
        canvas.drawBitmap(originalBmp, 0f, 0f, paint);
    }
}
