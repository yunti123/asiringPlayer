package com.yunti.asiringmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.yunti.asiringmusic.R;
import com.yunti.asiringmusic.listeners.CustomListener;
import com.yunti.asiringmusic.utils.SongAdapter;

public class FragmentMusic extends Fragment {

    private CustomListener listener = null;
    private ListView songView;
    private View fragmentMusicView;


    public FragmentMusic() {


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentMusicView = inflater.inflate(R.layout.fragment_music,container,false);
        this.songView = (ListView)fragmentMusicView.findViewById(R.id.songs_list);
        this.listener.setAdapter();
        return fragmentMusicView;

    }



    public void setSongAdapter(SongAdapter songAdapter){
        if(songAdapter != null) {
            if (songView != null)
                songView.setAdapter(songAdapter);
            else
                System.out.println("view gg");
        }
        else
            System.out.println("adpter gg");
    }

    public void setListener(CustomListener listener){
        this.listener = listener;

    }




}
