package com.example.motophosaique;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;

public class ResultFragment extends Fragment {
    public ResultFragment() {
        super(R.layout.fragment_result);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        // 返回上一页
        v.findViewById(R.id.btnBack)
                .setOnClickListener(x -> Navigation.findNavController(x).popBackStack());

        // 取参数
        int blockSize = getArguments().getInt    ("blockSize", 16);
        String algo   = getArguments().getString ("algo",      "average");      // ← 注意这里 key 是 "algo"
        boolean withRep = getArguments().getBoolean("withRep", true);

        ImageView iv = v.findViewById(R.id.ivResult);
        TextView  tv = v.findViewById(R.id.tvTime);

        new AsyncTask<Void,Void,Bitmap>() {
            long start, elapsed;

            @Override protected void onPreExecute() {
                start = System.currentTimeMillis();
            }

            @Override protected Bitmap doInBackground(Void... u) {
                File cache = requireActivity().getCacheDir();
                File in    = new File(cache, "input.pgm");
                File out   = new File(cache, "output.pgm");

                // 调 JNI，传入算法名称 algo
                int res = ((MainActivity)requireActivity())
                        .generateMosaic(
                                in.getAbsolutePath(),
                                out.getAbsolutePath(),
                                blockSize,
                                algo,
                                withRep
                        );
                elapsed = System.currentTimeMillis() - start;
                if (res != 0) {
                    return null;
                }
                // 解码 PGM -> Bitmap
                return Utils.decodePGM(out);
            }

            @Override protected void onPostExecute(Bitmap bmp) {
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                    tv.setText("Waiting time: " + (elapsed/1000f) + "s");
                } else {
                    tv.setText("生成失败（algo=" + algo + "），请查看日志");
                }
            }
        }.execute();
    }
}
