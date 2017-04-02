package com.orlanth23.annoncesnc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orlanth23.annoncesnc.R;

public class DemoFragmentAnnonces extends Fragment {

    public DemoFragmentAnnonces() {
    }

    public static DemoFragmentAnnonces newInstance() {
        DemoFragmentAnnonces fragment = new DemoFragmentAnnonces();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo_fragment_annonces, container, false);
    }
}
