package com.example.motophosaique;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AlgoPagerAdapter extends FragmentStateAdapter {
    public AlgoPagerAdapter(@NonNull SelectFragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new GreyAlgoFragment();
            case 1: return new ColorAlgoFragment();
            default: return new ObjectAlgoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}