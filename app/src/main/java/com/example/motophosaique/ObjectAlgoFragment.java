package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ObjectAlgoFragment extends Fragment {
    public ObjectAlgoFragment() {
        super(R.layout.fragment_algos_object);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        // 暂时没有目标检测算法，留空或拷贝灰度逻辑
    }
}
