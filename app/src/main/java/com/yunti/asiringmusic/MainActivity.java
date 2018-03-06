package com.yunti.asiringmusic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import com.yunti.asiringmusic.utils.MusicService;
import com.yunti.asiringmusic.utils.Song;
import com.yunti.asiringmusic.utils.SongAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songs;
    private ListView songView;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound;
    private boolean paused;
    private boolean playbackPaused;
    private MediaController controller;

    /*---------------------------------------------------------------|
    |-------------------------Constructor----------------------------|
    |---------------------------------------------------------------*/

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.songView = (ListView)findViewById(R.id.song_list);
        this.songs = new ArrayList<Song>();
        this.musicBound = false;
        this.paused = false;
        this.playbackPaused = false;
        this.musicService = new MusicService();

        this.getSongList();

        Collections.sort(this.songs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this.songs,this);
        this.songView.setAdapter(songAdapter);

        this.setController();
    }

    /*---------------------------------------------------------------|
    |--------------------------Implements----------------------------|
    |---------------------------------------------------------------*/

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
    protected void onResume(){
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

    /*---------------------------------------------------------------|
    |-----------------------Public Methods---------------------------|
    |---------------------------------------------------------------*/

    public void getSongList(){
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

    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getGroupId()){
            case 1:
                musicService.toggleShuffle();
                break;
            case 2:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*---------------------------------------------------------------|
    |----------------------Private Methods---------------------------|
    |---------------------------------------------------------------*/

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

    private void setController(){
        controller = new MediaController(this);
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
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    protected void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
        }

        startService(playIntent);
    }

    protected void onDestroy(){
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    protected void onStop(){
        controller.hide();
        super.onStop();
    }
}
