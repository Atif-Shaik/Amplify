package com.example.sound;

import javafx.scene.image.Image;

public class Song {
    public Image art;
    public String filepath, title, artist, album;

    public Song(Image art, String filepath, String title, String artist, String album) {
        this.art = art;
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public String getSelectedFilepath() {
        return filepath;
    }

} // class ends
