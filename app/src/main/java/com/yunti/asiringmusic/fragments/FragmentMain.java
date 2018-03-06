package com.yunti.asiringmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunti.asiringmusic.R;

import java.util.ArrayList;
import java.util.List;


public class FragmentMain extends Fragment {

    private View fragmentMainView;



    public FragmentMain() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentMainView = inflater.inflate(R.layout.fragment_main,container,false);

        return fragmentMainView;
    }
}
