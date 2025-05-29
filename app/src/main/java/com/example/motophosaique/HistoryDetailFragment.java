package com.example.motophosaique;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.motophosaique.R;

import java.io.File;
import java.io.IOException;

public class HistoryDetailFragment extends Fragment {
    private Bitmap mosaicBmp, originalBmp;
    private boolean showingMosaic = true;

    public HistoryDetailFragment() {
        super(R.layout.fragment_history_detail);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageView iv = v.findViewById(R.id.detailImage);

        Bundle args = getArguments();
        String mosaicPath   = args.getString("imagePath");
        String originalUri  = args.getString("originalUri");
        String type         = args.getString("type");
        String algo         = args.getString("algo");
        float  time         = args.getFloat("timeSec");

        // 1) 解码马赛克图
        if (mosaicPath != null) {
            File f = new File(mosaicPath);
            if (f.exists()) {
                mosaicBmp = BitmapFactory.decodeFile(mosaicPath);
            }
        }
        // 2) 解码原图
        if (originalUri != null) {
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

        // 3) 一开始显示马赛克图
        iv.setImageBitmap(mosaicBmp);

        // 4) 点击切换
        iv.setOnClickListener(view -> {
            if (showingMosaic) {
                iv.setImageBitmap(originalBmp);
            } else {
                iv.setImageBitmap(mosaicBmp);
            }
            showingMosaic = !showingMosaic;
        });

        // 保持原有的文字显示
        ((TextView)v.findViewById(R.id.detailType )).setText("Type: " + type);
        ((TextView)v.findViewById(R.id.detailAlgo )).setText("Algo: " + algo);
        ((TextView)v.findViewById(R.id.detailTime )).setText("Time: " + time + "s");
    }
}
