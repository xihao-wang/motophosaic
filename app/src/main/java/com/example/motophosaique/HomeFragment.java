package com.example.motophosaique;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        // 点击最中间的大 + 区域，跳到 SelectFragment
        v.findViewById(R.id.importContainer)
                .setOnClickListener(view ->
                        Navigation.findNavController(view)
                                .navigate(R.id.action_home_to_select)
                );
    }
}
