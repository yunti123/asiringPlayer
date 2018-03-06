package com.yunti.asiringmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yunti.asiringmusic.fragments.FragmentMain;
import com.yunti.asiringmusic.fragments.FragmentMusic;
import com.yunti.asiringmusic.fragments.FragmentPlay;
import com.yunti.asiringmusic.listeners.CustomListener;
import com.yunti.asiringmusic.utils.MusicService;
import com.yunti.asiringmusic.utils.Song;
import com.yunti.asiringmusic.utils.SongAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main2Activity extends AppCompatActivity {

    private ArrayList<Song> songs;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound;
    private boolean paused;
    private boolean playbackPaused;
    private FragmentMain main;
    private FragmentMusic music;
    private FragmentPlay play;
    private View layout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        this.songs = new ArrayList<Song>();
        this.musicBound = false;
        this.paused = false;
        this.playbackPaused = false;
        this.musicService = new MusicService();
        this.layout = findViewById(R.id.main_content);
        this.getSongList();

        Collections.sort(this.songs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        final SongAdapter songAdapter = new SongAdapter(this.songs,this);

        main = new FragmentMain();
        play = new FragmentPlay(musicService,this.songs);

        viewPager = (ViewPager)findViewById(R.id.container);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment fret;

                music = new FragmentMusic();


                music.setListener(new CustomListener() {

                    @Override
                    public void setAdapter() {
                        music.setSongAdapter(songAdapter);
                    }

                });


                switch (position){
                    case 0:
                        fret = music;
                        break;
                    case 1:
                        fret = play;
                        break;
                    case 2:
                        fret = main;
                        break;
                    default:
                        fret = null;
                }

                return fret;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    public void songPicked(View view){
        play.onSongPicked(Integer.parseInt(view.getTag().toString()),true);
        viewPager.setCurrentItem(1);
    }


    private void getSongList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,
                null,null,null,null);

        if(musicCursor != null && musicCursor.moveToFirst()){
            int titleCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idCol = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do{
                long thisId = musicCursor.getLong(idCol);
                String thisTitle = musicCursor.getString(titleCol);
                String thisArtist = musicCursor.getString(artistCol);
                songs.add(new Song(thisTitle,thisArtist,thisId));
            }while (musicCursor.moveToNext());
        }
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

    private void permissionIssues(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //perssion issues
            }
        }
    }
}
