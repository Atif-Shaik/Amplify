package com.example.edit;

import com.example.amplify.Main;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class EditAudioTag {
    String filePath, title, artist, album, imagePath;
    AudioFile audioFile;
    Tag tag;
    Artwork artwork;
    Stage mainStage;


    public EditAudioTag(String filePath, Stage mainStage) {
        this.filePath = convertFilePath(filePath);
        this.mainStage = mainStage;
    }

    public String convertFilePath(String filePath) {
        // decoding URL encoded characters like %20, %5B etc.
        String fileAddress = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
        // removing "file:/" from path for audio file
        fileAddress = fileAddress.substring(6);
        return fileAddress;
    } // end

    public void editAndSaveAudioTag() {
        try {
            audioFile = AudioFileIO.read(new java.io.File(filePath));

            if (!isTagWritable(audioFile)) {
                showNotSupportedFileFormatError();
                return;
            }
            tag = audioFile.getTag();
            if (tag == null) {
                tag = audioFile.getTagOrCreateDefault();
                if (tag != null) {
                    writeData();
                } else {
                    showTagCannotBeCreatedError();
                    return;
                }
            } else {
                writeData();
            }
            audioFile.commit();
        } catch (TagException | ReadOnlyFileException | IOException | CannotReadException | InvalidAudioFrameException e) {
            // just chill
        } catch (CannotWriteException e) {
            writeErrorAlert();
        }
    } // method ends

    public void writeData() throws FieldDataInvalidException, IOException {
        if (!imagePath.equals("None")) {
            artwork = Artwork.createArtworkFromFile(new File(imagePath));
            boolean isValidArtwork =
                    artwork.getBinaryData() != null &&
                            artwork.getBinaryData().length > 0 &&
                            artwork.getMimeType() != null &&
                            !artwork.getMimeType().isEmpty();

            if (!isValidArtwork) {
                // show unsupported image Alert
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.initOwner(mainStage);
                alert.setTitle("Album Art Error");

                ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                errorIcon.setFitHeight(54);
                errorIcon.setFitWidth(54);
                HBox hBox = new HBox(15);
                hBox.setPrefSize(420,105);
                hBox.setPadding(new Insets(20, 0,0,20));

                VBox vBox = new VBox(1);
                Label title = new Label("Image File Error!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Failed to process the image. The selected file may be a corrupt image or an unsupported format that the application cannot read.");
                content.setWrapText(true);
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

                vBox.getChildren().addAll(title, content);
                HBox.setMargin(errorIcon, new Insets(20, 0, 0, 0));
                HBox.setMargin(vBox, new Insets(0, 20, 0, 0));
                hBox.getChildren().addAll(errorIcon, vBox);
                alert.getDialogPane().setContent(hBox);
                alert.setHeaderText(null);

                ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().add(ok);

                alert.show();
            } else {
                Artwork currentArtwork = tag.getFirstArtwork();

                if (currentArtwork != null) {
                    tag.deleteArtworkField();
                    tag.setField(artwork);
                } else {
                    tag.setField(artwork);
                }
            } // else end
        } // if ended for artwork

        if (!title.equals("None")) {
            tag.setField(FieldKey.TITLE, title);
        } // if ended for title

        if (!artist.equals("None")) {
            tag.setField(FieldKey.ARTIST, artist);
        } // if ended for artist

        if (!album.equals("None")) {
            tag.setField(FieldKey.ALBUM, album);
        } // if ended for album

    } // method ends

    public void setNewSongDetails(String imagePath, String title, String artist, String album) {
        this.imagePath = imagePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public boolean isTagWritable(AudioFile audioFile) {
        Tag tag = audioFile.getTag();

        if (tag == null) {
            return false;
        }
        
        return (tag instanceof ID3v24Tag) ||
                (tag instanceof ID3v23Tag) ||
                (tag instanceof FlacTag) ||
                (tag instanceof Mp4Tag) ||
                (tag instanceof VorbisCommentTag);
    } // end

    public void showTagCannotBeCreatedError() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Tagging Error");

        ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
        errorIcon.setFitHeight(54);
        errorIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(390,100);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Tag Creation Error!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("An unexpected error occurred and the application could not prepare the file for editing. Please try a different file.");
        content.setWrapText(true);
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

        vBox.getChildren().addAll(title, content);
        HBox.setMargin(errorIcon, new Insets(20, 0, 0, 0));
        hBox.getChildren().addAll(errorIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(ok);

        alert.show();
    } // method ends

    public void showNotSupportedFileFormatError() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Tagging Error");

        ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
        errorIcon.setFitHeight(54);
        errorIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(410,100);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Tagging Not Supported!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("The current file format (often formats like WAV or uncompressed PCM) does not support embedding metadata tags. No changes will be saved to this file.");
        content.setWrapText(true);
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

        vBox.getChildren().addAll(title, content);
        HBox.setMargin(errorIcon, new Insets(20, 0, 0, 0));
        HBox.setMargin(vBox, new Insets(0, 20, 0, 0));
        hBox.getChildren().addAll(errorIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType ok = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(ok);

        alert.show();
    } // method ends

    public void writeErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Tagging Error");

        ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
        errorIcon.setFitHeight(54);
        errorIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(420,105);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Failed to Save File!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("Your changes could not be saved. This usually happens if the file is open in another program (like a music player) or if you do not have permission to modify it.");
        content.setWrapText(true);
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

        vBox.getChildren().addAll(title, content);
        HBox.setMargin(errorIcon, new Insets(20, 0, 0, 0));
        HBox.setMargin(vBox, new Insets(0, 20, 0, 0));
        hBox.getChildren().addAll(errorIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(ok);

        alert.show();
    } // method ends

} // class ends
