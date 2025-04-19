package com.example.sound;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SoundLoader {
    public MediaPlayer mediaPlayer;
    public Media media;
    AudioFile audioFile;
    public String title, artist;
    String newFilePath;
    public Image albumArt, defaultAlbumArt;
    public ImageView banner;
    Tag tag;

    String fileName, newtitle, finaltitle;
    int count1, count2, i, j;

    public void openSong(String file) {
        media = new Media(file);
        mediaPlayer = new MediaPlayer(media);

        try {
            // decoding URL encoded characters like %20, %5B etc.
            newFilePath = URLDecoder.decode(file, StandardCharsets.UTF_8);

            // removing "file:/" from path for audio file
            newFilePath = newFilePath.substring(6);

            audioFile = AudioFileIO.read(new java.io.File(newFilePath));
            tag = audioFile.getTag();

            // get artwork from metadata
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(artwork.getBinaryData());
                albumArt = new Image(byteArrayInputStream);
            } else {
                albumArt = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
            }

            // get title and artist from metadata
            if (tag != null) {
                title = tag.getFirst(FieldKey.TITLE);
                artist = tag.getFirst(FieldKey.ARTIST);
            } // if ends here

            // handle missing title data
            if (title == null || title.isEmpty() ||title.isBlank()) {
                fileName = newFilePath;
                count1 = 0;
                count2 = 0;
                // count the length to final '/' slash
                for(i = 0; i < fileName.length(); i++) {
                    if(fileName.charAt(i) == '/') {
                        count1 = i + 1;
                    }
                } // loop ends
                // cut name to final '/' to get the end file name
                newtitle = fileName.substring(count1);

                // count the length to final '.' dot
                for(j = 0; j < newtitle.length(); j++) {
                    if(newtitle.charAt(j) == '.') {
                        count2 = j;
                    }
                }
                finaltitle = newtitle.substring(0, count2); // cut the string from start to dot (.) for final title
                title = finaltitle; // final title name from path of the file
            } // if ends here

            // handle missing artist name
            if (artist == null || artist.isEmpty() || artist.isBlank()) {
                artist = "Arist Name";
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // method ends here

} // class ends here
