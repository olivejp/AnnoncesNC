package com.orlanth23.annoncesnc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orlanth23.annoncesnc.R;

public class DemoFragmentFavorites extends Fragment {

    public DemoFragmentFavorites() {
    }

    public static DemoFragmentFavorites newInstance() {
        DemoFragmentFavorites fragment = new DemoFragmentFavorites();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo_fragment_favorites, container, false);
    }
}
