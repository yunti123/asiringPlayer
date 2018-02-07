package com.yunti.asiringmusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final int NOTIFY_ID = 1;

    private ArrayList<Song> songs;
    private MediaPlayer player;
    private int songPos;
    private IBinder musicBind;
    private String songTitle;
    private boolean shuffle = false;
    private Random rand;

    /*---------------------------------------------------------------|
    |-------------------------Constructor----------------------------|
    |---------------------------------------------------------------*/

    public void onCreate(){
        super.onCreate();
        player = new MediaPlayer();
        this.musicBind = new MusicBinder();
        this.shuffle = false;
        this.songTitle = "";
        songPos = 0;

        this.initMusicPlayer();
        this.rand = new Random();
    }

    /*---------------------------------------------------------------|
    |-------------------------Implements-----------------------------|
    |---------------------------------------------------------------*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(this.player.getCurrentPosition() > 0){
            mp.reset();
            this.playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent notIntent = new Intent(this,MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivities(this, 0,
                new Intent[]{notIntent},PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(this.songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID,not);
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    /*---------------------------------------------------------------|
    |-----------------------Public Methods---------------------------|
    |---------------------------------------------------------------*/

    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void initMusicPlayer(){
        //Buraya bak
        //player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPos);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();

        Uri trackUri = ContentUris.withAppendedId
                (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currSong);
        try {
            player.setDataSource(getApplicationContext(),trackUri);
        }catch (Exception e){
            System.out.println("MUSIC SERVICE: Error setting data source  " + e);
        }

        player.prepareAsync();
    }

    public void pausePlayer(){
        this.player.pause();
    }

    public void seek(int pos){
        this.player.seekTo(pos);
    }

    public void go(){
        this.player.start();
    }

    public void playPrev(){
        if(this.songPos != 0)
            this.songPos -= 1;
        else
            this.songPos = songs.size() -1;

        this.playSong();
    }

    public void playNext(){
        if (this.shuffle){
            int newSong = this.songPos;
            while (newSong == this.songPos){
                newSong = this.rand.nextInt(this.songs.size());
            }
            this.songPos = newSong;
        }
        else {
            if(this.songPos == this.songs.size())
                this.songPos = 0;
            else
                this.songPos++;
        }

        this.playSong();
    }

    /*---------------------------------------------------------------|
    |-------------------------Getter Setter--------------------------|
    |---------------------------------------------------------------*/

    public void toggleShuffle(){
        if(this.shuffle)
            shuffle = false;
        else
            shuffle = true;
    }

    public void setSong(int songIndex){
        this.songPos = songIndex;
    }

    public void setList(ArrayList<Song> theSong){
        this.songs = theSong;
    }

    public int getPosn(){
        return this.player.getCurrentPosition();
    }

    public int getDur(){
        return this.player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }


    /*---------------------------------------------------------------|
    |-------------------------Inner Class----------------------------|
    |---------------------------------------------------------------*/

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
