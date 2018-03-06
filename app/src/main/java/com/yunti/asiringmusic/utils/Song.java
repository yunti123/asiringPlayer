package com.yunti.asiringmusic.utils;

public class Song {
    private String title;
    private String artist;
    private long id;

    /*---------------------------------------------------------------|
    |-------------------------Constructor----------------------------|
    |---------------------------------------------------------------*/

    public Song(String title, String artist, long id){
        this.title = title;
        this.artist = artist;
        this.id = id;
    }

    /*---------------------------------------------------------------|
    |----------------------------Getters-----------------------------|
    |---------------------------------------------------------------*/

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public long getId(){
        return this.id;
    }
}
