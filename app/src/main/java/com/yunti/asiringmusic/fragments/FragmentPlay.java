package com.yunti.asiringmusic.fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import com.yunti.asiringmusic.R;
import com.yunti.asiringmusic.utils.MusicService;
import com.yunti.asiringmusic.utils.Song;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class FragmentPlay extends Fragment implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songs;
    private MusicService musicService;
    private boolean playbackPaused;
    private boolean paused;
    private boolean musicBound;
    private MediaController controller;
    private Intent playIntent;
    private View fragmentPlayView;
    private boolean show;
    private boolean scroll;

    @SuppressLint("ValidFragment")
    public FragmentPlay(MusicService musicService,ArrayList<Song> songs) {
        this.musicService = musicService;
        this.songs = songs;
        scroll =
        show = false;
        playbackPaused = true;
        paused = false;
        musicBound = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentPlayView = inflater.inflate(R.layout.fragment_play,container,false);

        fragmentPlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (controller != null) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (show) {
                            controller.show(0);
                            show = false;
                        } else {
                            controller.hide();
                            show = true;
                        }
                    }

                }
                return false;
            }
        });


        if(playIntent == null){
            playIntent = new Intent(fragmentPlayView.getContext(),MusicService.class);
            fragmentPlayView.getContext().bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
        }

        fragmentPlayView.getContext().startService(playIntent);



        return fragmentPlayView;

    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    public int getDuration() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDur();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPosn();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService != null && musicBound)
            return musicService.isPlaying();
        else
            return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void setController(){
        controller = new MediaController(fragmentPlayView.getContext());
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(fragmentPlayView);
        controller.setEnabled(true);
    }


    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);

    }

    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    public void onDestroy(){
        fragmentPlayView.getContext().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    public void onStop(){
        controller.hide();
        super.onStop();
    }

    public void onSongPicked(int id,boolean play){
        this.playbackPaused = play;
        musicService.setSong(id);
        musicService.playSong();
        if(playbackPaused){
            setController();
            controller.show();
            controller.hide();
            controller.show();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

}
