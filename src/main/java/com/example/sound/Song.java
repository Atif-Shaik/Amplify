package com.example.sound;

import javafx.scene.image.Image;

public class Song {
    public Image art;
    public String filepath, title, artist;

    public Song(Image art, String filepath, String title, String artist) {
        this.art = art;
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
    }

    public String getSelectedFilepath() {
        return filepath;
    }

} // class ends
