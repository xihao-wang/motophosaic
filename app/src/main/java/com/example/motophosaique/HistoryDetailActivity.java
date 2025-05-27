
package com.example.motophosaique;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        ImageView imageView = findViewById(R.id.detailImage);
        TextView typeView   = findViewById(R.id.detailType);
        TextView algoView   = findViewById(R.id.detailAlgo);
        TextView timeView   = findViewById(R.id.detailTime);

        String path = getIntent().getStringExtra("imagePath");
        String type = getIntent().getStringExtra("type");
        String algo = getIntent().getStringExtra("algo");
        float  time = getIntent().getFloatExtra("timeSec", 0f);

        if (path != null) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(path));
            }
        }

        typeView.setText("Type: " + type);
        algoView.setText("Algorithm: " + algo);
        timeView.setText("Time: " + time + "s");
    }
}
