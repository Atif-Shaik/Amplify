package com.example.amplify;

import com.example.setting.Settings;
import com.example.sound.Song;
import com.example.sound.SoundLoader;
import com.jfoenix.controls.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

public class MainSceneController {
    @FXML
    JFXButton playpause, backward, forward, addSong, playlist, volume, likeAndDislike, settings, lyricsAndBanner;
    @FXML
    Label song_title, artist_name;
    @FXML
    ImageView banner;
    @FXML
    JFXSlider slider;
    @FXML
    Label currentLength, fullLength;
    @FXML
    JFXToggleButton loop, shuffle;
    @FXML
    AnchorPane mainSceneFXML;

    JFXButton volumeButton, sleepButton;
    JFXComboBox<String> minuteSelection;
    RadioButton option1, option2;
    Dialog<Void> showSettings;
    Settings appSettings;

    int lastIndex = 0;
    LinkedHashSet<Integer> removedSongIndex;
    int playlistIndex = 0;
    int countLiveSeconds = 0; // the count veriable counts the real time
    int i;
    int liveSconds = 0, liveMinute = 0;
    int minute, seconds, totalDurationInSeconds, secondsInDuration;
    Duration duration;

    SimpleStringProperty Title, Artist, Length, Live;
    AnimationTimer timer;

    Stage mainStage;
    Scene mainScene, playlistScene;
    PlaylistController playlistController;
    ImageView play, back, fast, add, list, pause, speaker, no_speaker, volumeIcon, mute, like, dislike, setting, lyrics;
    Image art;
    LinkedList<String> opendPlaylist;
    LinkedList<String> likedList;
    AnchorPane popupContent;
    JFXPopup popup;
    Slider volumeSlider;
    Label volumePercent;

    Random random;
    String url = "jdbc:sqlite:" + System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appdata.db";
    String filePath;
    String lastRemovedLikedSong;
    private boolean isSceneCreated = false;
    boolean isPlaying = false;
    boolean isSongLoaded = false;
    boolean helperForPlayPause = false;
    boolean isDragging = false;
    boolean isLooped = false;
    boolean isShuffled = false;
    boolean isPaused = false;
    boolean isVolumeButtonPressed = false;
    boolean isLiked = false;
    boolean letItPlay = false;
    boolean dontReplay = false;
    boolean updateListview = false;
    boolean isMainSceneOn = true;
    boolean isExists = true;
    boolean letItUpdateInlistviewListener = true;
    boolean clearAllRemovedSongs = false;
    Song removedSongObject;
    String tellWhatToDoAddOrRemove;
    PauseTransition delay;

    ArrayList<String> erroredSong;
    ArrayList<String> invalidSongTrace;
    ArrayList<Song> songsShouldBeRemoved;
    LinkedHashMap<String, Integer> songsInMusicFolder;
    FileChooser fileChooser;
    SoundLoader soundLoader;

    @FXML
    public void initialize() { // initialize method starts

        // initializing SoundLoader
        soundLoader = new SoundLoader();
        // initializing file chooser
        fileChooser = new FileChooser();
        // initializing linkedLists
        opendPlaylist = new LinkedList<>();
        likedList = new LinkedList<>();
        // initializing arraylist
        removedSongIndex = new LinkedHashSet<>();
        erroredSong = new ArrayList<>();
        songsShouldBeRemoved = new ArrayList<>();
        invalidSongTrace = new ArrayList<>();
        songsInMusicFolder = new LinkedHashMap<>();
        // initializing vbox
        popupContent = new AnchorPane();
        popupContent.setPrefSize(240,40); // add before popup setting
        // initializing
        popup = new JFXPopup(popupContent); // initializing popup for volume
        volumePercent = new Label(); // initializing volume percentage label
        volumeSlider = new Slider(); // initializing volume slider
        // initializing volumeButton;
        volumeButton = new JFXButton();
        // initializing random
        random = new Random();
        // initialiazing delay
        delay = new PauseTransition(Duration.seconds(3));

        fileChooser.setTitle("Select an audio file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files","*.mp3", "*.wav", "*.aac"));

        play = new ImageView();
        pause = new ImageView();
        back = new ImageView();
        fast = new ImageView();

        // load custom icons for buttons
        art = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
        lyrics = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/lyrics.png"))));
        setting = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings.png"))));
        add = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/add.png"))));
        list = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playlist.png"))));
        speaker = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/speaker.png"))));
        no_speaker = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/no_speaker.png"))));
        volumeIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/volumeIcon.png"))));
        mute = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/mute.png"))));
        like = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/like.png"))));
        dislike = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/dislike.png"))));

        // setting volume functionality section
        volumeSlider.setPrefSize(150, 40);
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.5);

        volumeIcon.setFitHeight(24);
        volumeIcon.setFitWidth(24);
        volumeButton.setPrefWidth(24);
        volumeButton.setPrefHeight(24);
        volumeButton.setGraphic(volumeIcon);

        volumeButton.setLayoutY(5);
        volumeButton.setLayoutX(1);

        volumeSlider.setLayoutX(36);
        volumeSlider.setLayoutY(0);

        volumePercent.getStyleClass().add("font-for-volume-percent");
        volumePercent.setLayoutY(6);
        volumePercent.setLayoutX(196);

        popupContent.getStyleClass().add("jfx-popup-container");
        popupContent.getStyleClass().add("popup-pane");
        popupContent.getChildren().addAll(volumeButton, volumeSlider, volumePercent);

        // add custom icons to buttons
        playpause.setGraphic(play);
        lyricsAndBanner.setGraphic(lyrics);
        settings.setGraphic(setting);
        addSong.setGraphic(add);
        playlist.setGraphic(list);
        backward.setGraphic(back);
        forward.setGraphic(fast);
        volume.setGraphic(speaker);
        likeAndDislike.setGraphic(dislike);

        // add css styles to buttons
        volumeButton.getStyleClass().add("transparent-button");

        // add action listeners to buttons
        playlist.setOnAction(event -> switchToPlaylistScene());
        playpause.setOnAction(event -> playAndPauseController());
        addSong.setOnAction(event -> loadSongAddress());
        loop.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isLooped = newVal;
            if (isLooped) {
                isShuffled = false;
                shuffle.setSelected(false); // turning shuffle to off when loop is on
            }
        });
        shuffle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            isShuffled = newValue;
            if (isShuffled && opendPlaylist.size() > 1) {
                isLooped = false;
                loop.setSelected(false); // turning loop to off when shuffle is on
            }
            if (isShuffled && !opendPlaylist.isEmpty() && opendPlaylist.size() == 1) { // this if handles shuffle when there is only one song in the playlist
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.initOwner(mainStage);
                alert.setTitle("Shuffle Error");

                ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                errorIcon.setFitHeight(54);
                errorIcon.setFitWidth(54);
                HBox hBox = new HBox(15);
                hBox.setPrefSize(420,70);
                hBox.setPadding(new Insets(20, 0,0,20));

                VBox vBox = new VBox(1);
                Label title = new Label("Unable to shuffle playlist!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Please add more songs to ");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                Label content1 = new Label(playlistController.playlists.getValue());
                content1.setStyle("-fx-font-weight: bold; -fx-font-style: italic; -fx-text-fill: blue;");

                Label content2 = new Label(" playlist.");
                content2.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

                HBox hBox1 = new HBox();
                hBox1.getChildren().addAll(content, content1, content2);
                vBox.getChildren().addAll(title, hBox1);

                hBox.getChildren().addAll(errorIcon, vBox);
                alert.getDialogPane().setContent(hBox);
                alert.setHeaderText(null);

                ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().add(ok);

                alert.showAndWait();
                isShuffled = false;
                shuffle.setSelected(false);
            } // if ends
            if (isShuffled && opendPlaylist.isEmpty()){ // this if handles shuffle when playlist is not selected
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.initOwner(mainStage);
                alert.setTitle("Shuffle Error");
                ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                errorIcon.setFitWidth(54);
                errorIcon.setFitHeight(54);
                HBox hBox = new HBox(15);
                hBox.setPrefSize(390,70);
                hBox.setPadding(new Insets(20, 0,0,20));

                VBox vBox = new VBox(1);
                Label title = new Label("Unable to shuffle playlist!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Please load a playlist to enable this feature.");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                vBox.getChildren().addAll(title, content);

                hBox.getChildren().addAll(errorIcon, vBox);
                alert.getDialogPane().setContent(hBox);
                alert.setHeaderText(null);

                ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().add(ok);

                alert.showAndWait();
                isShuffled = false;
                shuffle.setSelected(false);
            } // if ends
        }); // shuffle listener ends
        volume.setOnAction(event -> showVolumePane());
        volumeSlider.setOnMouseReleased(event -> setMuteViaVolumeSlider());
        volumeSlider.setOnMouseDragged(event -> {delay.stop();}); // this stops the closing countdown if user is interacting with volume slider
        volumeButton.setOnAction(event -> volumeButtonController());
        likeAndDislike.setOnAction(event -> likeAndDiskikeController());
        backward.setOnAction(event -> backwardButton());
        forward.setOnAction(event -> forwardButton());
        delay.setOnFinished(event -> {
            popup.hide(); // hiding popup after 3 seconds
        });
        volumeButton.setOnMouseClicked(event -> {delay.stop();delay.play();});
        settings.setOnAction(event -> setSettings());

        // binding label with simple string property
        Title = new SimpleStringProperty("(Song Title)");
        song_title.textProperty().bind(Title);

        volumePercent.textProperty().bind(Bindings.format("%.0f%%", volumeSlider.valueProperty().multiply(100)));

        Length = new SimpleStringProperty("00:00");
        fullLength.textProperty().bind(Length);

        Live = new SimpleStringProperty("00:00");
        currentLength.textProperty().bind(Live);

        Artist = new SimpleStringProperty("Artist Name");
        artist_name.textProperty().bind(Artist);

        // creating timer for live update
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long l) {
                if (l - lastUpdate >= 1_000_000_000) {
                    countLiveSeconds++;
                    liveSconds++;
                    if (liveSconds == 60) { // this if converts seconds to minute for displaying
                        liveMinute++;
                        liveSconds = 0;
                    } // inner 1 if ends
                    Live.set(String.format("%02d:%02d", liveMinute, liveSconds));
                    lastUpdate = l;
                    if (countLiveSeconds == totalDurationInSeconds && !isLooped && !isShuffled) { // this if stops the song when it is playing solo
                        soundLoader.mediaPlayer.stop();
                        timer.stop();
                        playpause.setGraphic(play);
                        helperForPlayPause = false; // should always be false here
                        if (playlistController != null) { // this if prevents from null pointer exception when the playlist controller was not initialized
                            playlistController.helperForPlayPause = true; // should always be true here
                            playlistController.miniPlayPauseController(); // calling the mini play pause controller when the song has finished
                        }
                        isPaused = true;
                        resetLiveCountSeconds();
                    } // inner 2 if ends
                    if (countLiveSeconds == totalDurationInSeconds && isLooped) { // this if loops the song when on
                        soundLoader.mediaPlayer.seek(Duration.seconds(0));
                        resetLiveCountSeconds();
                    } // inner 3 if ends
                    if (countLiveSeconds == totalDurationInSeconds && isShuffled && opendPlaylist.size() > 1) {
                        lastIndex = playlistIndex;
                        while (true) { // this loop ensures that to load different song
                            playlistIndex = random.nextInt(opendPlaylist.size());
                            if (lastIndex != playlistIndex) {
                                break;
                            }
                        } // loop ends here
                        fullyLoadSong(); // loading song after shuffle
                        // sending shuffled song details to playlist scene
                        playlistController.setBanner(soundLoader.albumArt);
                        playlistController.setArtist(soundLoader.artist);
                        playlistController.setTitle(soundLoader.title);
                        playlistController.listView.getSelectionModel().select(playlistIndex);
                    } // shuffle if ends here
                } // main if ends
            } // override method
        }; // timer end statement

        // tooltip section
        Tooltip playPauseTip = new Tooltip("Press \"Ctrl + P\" to play or pause");
        playpause.setTooltip(playPauseTip);
        Tooltip loadTip = new Tooltip("Load song");
        addSong.setTooltip(loadTip);
        Tooltip playlistTip = new Tooltip("Load playlist");
        playlist.setTooltip(playlistTip);
        Tooltip backwardTip = new Tooltip("Press LEFT");
        backward.setTooltip(backwardTip);
        Tooltip forwardTip = new Tooltip("Press RIGHT");
        forward.setTooltip(forwardTip);

        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(0);
        openLikedListAndGetSongUsageDetails(); // loading liked song list
    } // initialize method ends ****************************************************************************************

    public void checkLikedSong() {
        for (String path: likedList) {
            if (path.equals(filePath)) {
                isLiked = true;
                likeAndDislike.setGraphic(like);
                break;
            } else {
                isLiked = false;
                likeAndDislike.setGraphic(dislike);
            }
        } // loop ends
    } // method ends

    // method fro loading liked songs
    public void openLikedListAndGetSongUsageDetails() {

        try {
            String basePath = System.getenv("LOCALAPPDATA");
            File file = new File(basePath, "AmplifyMusic" + File.separator + "appdata.db");

            if (file.exists()) { // this if ensures that when the app first installed the app will not crash due to absent od appdata.db which has not created yet
                try (Connection connection = DriverManager.getConnection(url)) {
                    String sql = "SELECT file_paths FROM liked_songs;";
                    String sql1 = "SELECT songPath, songCount FROM songsInAppData;";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql);

                    Statement statement1 = connection.createStatement();
                    ResultSet resultSet1 = statement1.executeQuery(sql1);
                    while (resultSet.next()) {
                        String data = resultSet.getString("file_paths");
                        likedList.add(data); // remenber to check file availability here later
                    } // loop ends
                    while (resultSet1.next()) { // loop for loading all songs with their traces
                        String song = resultSet1.getString("songPath");
                        Integer count = resultSet1.getInt("songCount");

                        URI uri = new URI(song);
                        File path = new File(uri);
                        if (path.exists()) songsInMusicFolder.put(song,count); // this map keeps track of how many times a song is used from music folder to prevent its deletion if used more than once
                        else invalidSongTrace.add(song);
                    } // loop ends */
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (!invalidSongTrace.isEmpty()) {
                    for (var str : invalidSongTrace) {
                        deleteSongsTableSingleCountSong(str);
                    } // for loop
                    invalidSongTrace.clear();
                } // if for deleting invalid songs
            } // if ends
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    } // method ends

    // this method loads data from playlist
    public void loadPlaylist(String playlist) {
        try (Connection connection = DriverManager.getConnection(url)){
            // readind data from table liked_songs
            String sql = "SELECT file_paths FROM " + playlist + ";";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                opendPlaylist.add(resultSet.getString("file_paths")); // to get data from file_paths row
            } // loop ends
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to read liked list!");
            alert.setContentText("Please contact us!");
            alert.showAndWait();
        } // try catch ends
    } // method ends

    public void addNewSongTrace(String str) {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "INSERT INTO songsInAppData (songPath, songCount)" +
                    "VALUES (?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, str);
            preparedStatement.setInt(2, 1);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    } // method ends

    // this method is for like and dislike functionality
    public void likeAndDiskikeController() {
        Alert commonAlert = new Alert(Alert.AlertType.INFORMATION);
        commonAlert.initOwner(mainStage);
        commonAlert.setTitle("Liked songs");
        commonAlert.setHeaderText(null);
        if (!isLiked && isSongLoaded) {
            try (Connection connection = DriverManager.getConnection(url)){
                URI uri = new URI(filePath); // converting filepath to uri object
                File file1 = new File(uri); // converting uri to file object
                String musicFolder = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "Music" + File.separator + file1.getName(); // creating path from LOCALAPPDATA root for liked song
                File file2 = new File(musicFolder); // creating file object for new path

                Path temPath = file2.toPath();
                String newFilepath = temPath.toAbsolutePath().toUri().toString();

                if (!file2.exists()) { // cheking if song is not in App's Music folder
                    Path sourcePath = file1.toPath(); // converting to path
                    Path targetPath = file2.toPath(); // converting to path
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    filePath = newFilepath; // assigninh new file path to filePath (crucial)
                    songsInMusicFolder.put(filePath, 1); // adding new song details in trace map
                    addNewSongTrace(filePath); // this will add new selected song for tracing
                } // if ends
                else if (file2.exists()) { // this else if prevents adding selected song to liked list database if it is already in liked
                    filePath = newFilepath; // assigninh new file path to filePath (crucial)
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(mainStage);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("It appears this song is already in your 'Liked' collection");
                    if (likedList.contains(filePath)) {
                        alert.showAndWait();
                        return; // omiting following code of method (crucial)
                    }
                } // if ends

                if (songsInMusicFolder.containsKey(filePath)) {
                    int cont = songsInMusicFolder.get(filePath);
                    if (cont > 1) {
                        songsInMusicFolder.put(filePath, cont + 1);
                        updateSongsTable(filePath); // this will increment the use oh song
                    }
                } else {
                    addNewSongTrace(filePath);
                    songsInMusicFolder.put(filePath, 1);
                }

                // writing data to database
                String sql1 = "INSERT INTO liked_songs (file_paths)" +
                        "VALUES (?);";
                String sql2 = "DELETE FROM deleted_songs WHERE file_paths = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                preparedStatement.setString(1, filePath);
                preparedStatement.executeUpdate(); // adding liked song to database

                isLiked = true;
                likeAndDislike.setGraphic(like);
                likedList.add(filePath);

                PreparedStatement preparedStatement1 = connection.prepareStatement(sql2);
                preparedStatement1.setString(1, filePath);
                preparedStatement1.executeUpdate();

                if (playlistController != null) {
                    // this if adds the liked song to the listview if the liked songs list is selected
                    if (playlistController.loadedPlaylist.equals("LIKED SONGS"))  {
                        playlistController.soundLoader.openSong(filePath); // preparing song object
                        Song song = new Song(playlistController.soundLoader.albumArt, filePath, playlistController.soundLoader.title, playlistController.soundLoader.artist);
                        removedSongObject = song;
                        updateListview = true;
                        tellWhatToDoAddOrRemove = "Add";
                        clearAllRemovedSongs = true;
                        opendPlaylist.add(filePath);
                        if (lastRemovedLikedSong.equals(filePath)) { // this if does not let the reload the song
                            dontReplay = false;
                        } // end
                    } // if ends
                } // main if
                commonAlert.setContentText("Song added to liked list!");
                commonAlert.showAndWait();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } // try catch ends
        } // if ends
        else if (isLiked && isSongLoaded) {
            try (Connection connection = DriverManager.getConnection(url)){
                // deleting song from database
                String sql1 = "DELETE FROM liked_songs WHERE file_paths = ?;";
                String sql2 = "INSERT INTO deleted_songs (file_paths)" +
                        "VALUES (?);";

                PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                preparedStatement.setString(1, filePath);
                preparedStatement.executeUpdate(); // deleting song from liked list database

                isLiked = false;
                likeAndDislike.setGraphic(dislike);

                if (songsInMusicFolder.containsKey(filePath)) {
                    int count = songsInMusicFolder.get(filePath);
                    if (count > 1) {
                        songsInMusicFolder.put(filePath, count - 1);
                    } else if (count == 1) {
                        deleteSongsTableSingleCountSong(filePath);
                        songsInMusicFolder.remove(filePath);
                    }
                }

                PreparedStatement preparedStatement1 = connection.prepareStatement(sql2);
                preparedStatement1.setString(1,filePath);
                preparedStatement1.executeUpdate();

                if (playlistController != null) {
                    if (playlistController.loadedPlaylist.equals("LIKED SONGS")) {
                        letItPlay = true; // setting it true so that listview cannot load other song and this song can still finish its playback
                        dontReplay = false;
                        for (int i = 0; i < playlistController.objectsOfOpendPlaylist.size(); i++) {
                            Song file = playlistController.objectsOfOpendPlaylist.get(i);
                            updateListview = true;
                            removedSongObject = file;
                            tellWhatToDoAddOrRemove = "Remove";
                            clearAllRemovedSongs = false;
                            if (file.filepath.equals(filePath)) {
                                likedList.remove(filePath);
                                opendPlaylist.remove(filePath);
                                removedSongIndex.add(playlistController.objectsOfOpendPlaylist.indexOf(file)); // getting index of song to remove it from listview
                                lastRemovedLikedSong = filePath;
                                break;
                            }
                        } // loop ends
                    } // if inner
                } // main if
                commonAlert.setContentText("Song removed from liked list!");
                commonAlert.showAndWait();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } // try catch ends
        } // else if ends
    } // method ends here

    // action listener for volumeButton
    public void volumeButtonController() {
        if (!isVolumeButtonPressed) {
            isVolumeButtonPressed = true;
            volumeButton.setGraphic(mute);
            volume.setGraphic(no_speaker);
            volumeSlider.setValue(0);
        } // outer if
        else if (isVolumeButtonPressed) {
            isVolumeButtonPressed = false;
            volumeButton.setGraphic(volumeIcon);
            volume.setGraphic(speaker);
            volumeSlider.setValue(0.5);
        } // if ends
    } // method ends here

    // this method handles volume icon for popup for corresponding volumeSlider value
    public void setMuteViaVolumeSlider() {
        double value = volumeSlider.getValue();
        if (value == 0) {
           volumeButton.setGraphic(mute);
           volume.setGraphic(no_speaker);
        } else if (value > 0) {
            volumeButton.setGraphic(volumeIcon);
            volume.setGraphic(speaker);
        } // if ends
        delay.play(); // this will close popup after 3 seconds
    } // method ends

    // method for volume
    public void showVolumePane() {
        popup.show(volume, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT);
        delay.play(); // this will close the popup after 3 seconds if not interacted
    } // method ends

    // this method resets live counting of seconds and sets to default
    public void resetLiveCountSeconds() {
        liveSconds = 0; // resetting seconds
        liveMinute = 0; // resetting minute
        countLiveSeconds = 0;  // resetting live seconds
        Live.set(String.format("%02d:%02d", liveMinute, liveSconds));
    }

    // invoking mediaPlayer to get timeline
    public void invokeMedia() {
        // this following code is updating slider
        soundLoader.mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            MediaPlayer.Status status = soundLoader.mediaPlayer.getStatus(); // getting the mediaplayer's status

            if (!isDragging && status == MediaPlayer.Status.PLAYING) {
                Platform.runLater(() -> {
                    slider.setValue(newValue.toSeconds());
                });
            }
        });

        soundLoader.mediaPlayer.setOnError(() -> {
            soundLoader.mediaPlayer.stop();
            soundLoader.mediaPlayer.dispose();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText("Please reset song or restart app.");
            alert.initOwner(mainStage);
            alert.showAndWait();
        });

        slider.setOnMousePressed(event -> { isDragging = true; });
        slider.setOnMouseReleased(event -> setNewTimeline());
    } // method ends

    // method for new select value via slider
    public void setNewTimeline() {
        isDragging = false;
        if(isSongLoaded) {
            soundLoader.mediaPlayer.seek(Duration.seconds(slider.getValue()));
            double tempValue = slider.getValue();
            countLiveSeconds = (int) tempValue; // remember it for later
            liveSconds = (int) tempValue % 60;
            liveMinute = 0;

            for (i = 0; i < ((int) tempValue - liveSconds) / 60; i++) {
                liveMinute++;
            }
            Live.set(String.format("%02d:%02d", liveMinute, liveSconds));
        } // if ends
    } // method ends

    // method for calculating music length
    public void calculateAndSetLength() {
        minute = 0;
        seconds = 0;
        liveMinute = 0;
        liveSconds = 0;
        duration = soundLoader.media.getDuration();

        secondsInDuration = (int) duration.toSeconds(); // converting in seconds

        seconds = secondsInDuration % 60; // extracting remaining seconds for :15

        for(i = 0; i < (secondsInDuration - seconds) / 60; i++) {
            minute++;
        }
        if(minute >= 1) {
            totalDurationInSeconds = (60 * minute) + seconds;
        } else {
            totalDurationInSeconds = seconds;
        }
        Length.set(String.format("%02d:%02d", minute, seconds));
        Live.set(String.format("%02d:%02d", liveMinute, liveSconds));
        slider.setValue(0);
        slider.setMax(soundLoader.media.getDuration().toSeconds());

        // giving slider the DurationStringConverter object that extends StringConverter<Double> abstract class
        // to customize the bubble content to format MM:SS
        // it automatically calls toString() method and passes it its current value to get formatted in MM:SS
        slider.setLabelFormatter(new DurationStringConverter()); // this line is important

    } // method ends here

    // get main stage reference
    public void setStage(Stage stage) {
        this.mainStage = stage;
    } // ends

    // get main scene reference
    public void setScene(Scene scene) {
        this.mainScene = scene;
    } // ends

    // action listener for playlist button
    public void switchToPlaylistScene() {
        if(!isSceneCreated) {
            isSceneCreated = true;
            try {
                // creating playlist scene
                FXMLLoader loader = new FXMLLoader(MainSceneController.class.getResource("/com/example/amplify/PlaylistScene.fxml"));
                Parent root = loader.load();
                playlistScene = new Scene(root, 500, 600);
                root.requestFocus(); // requesting focus for key events

                if (appSettings.getTheme().equals("Light Theme")) {
                    playlistScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/PlaylistScene-light-theme.css")));
                } else if (appSettings.getTheme().equals("Dark Theme")) {
                    playlistScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/PlaylistScene-dark-theme.css")));
                }

                // loading playlist controller
                playlistController = loader.getController();
                playlistController.setStage(mainStage); // sending stage reference only one time
                playlistController.setScene(mainScene); // sending scene reference only one time
                playlistController.setController(this); // sending main scene controller referance
                playlistController.changeComboBoxLineColor(appSettings.getTheme()); // changing the color of combobox line
                playlistController.loadIcons(appSettings.getTheme());

                // activating playlist scene
                mainStage.setScene(playlistScene);
                mainStage.show();
                isMainSceneOn = false; // this ensures that fullyLoadSong() method can be called when playlist scene is active
                sendSongInfoToPlaylistScene(); // send info after the scene is live it is crucial
                // key listener for mini play pause
                playlistScene.setOnKeyPressed(event -> { // key event handler
                    if (event.isControlDown() && event.getCode() == KeyCode.P) {
                        playlistController.miniPlayPauseController();
                    }
                    // this if for deleting playlist via key event
                    if (event.isShiftDown() && event.getCode() == KeyCode.DELETE) {
                        String selection = playlistController.playlists.getValue();
                        if (selection != null) {
                           if (selection.equals("LIKED SONGS")) { // if this ensures that liked songs playlist cannot be deleted
                               Alert alert = new Alert(Alert.AlertType.WARNING);
                               alert.initOwner(mainStage);
                               alert.setTitle("Warning");
                               alert.setHeaderText("LIKED SONGS Playlist cannot be deleted");
                               alert.setContentText("Try deleting different playlist");
                               alert.showAndWait();
                           } else { // this else block deletes playlist
                               boolean confirmation = getConfirmationForDeletingPlaylist(selection, " playlist");
                               if (confirmation) { // this if only deletes playlist after user confirmation
                                   playlistController.playlists.getItems().remove(selection);
                                   playlistController.playlists.setValue(null);
                                   // delete from database too later
                                   selection = selection.toLowerCase();
                                   selection = selection.replace(" ", "_");
                                   try (Connection connection = DriverManager.getConnection(url)){ // databas connection
                                       String sql1 = "DROP TABLE " + selection + ";"; // deleting playlist table
                                       String sql2 = "DELETE FROM all_playlists WHERE playlists = (?);"; // deleting table name from all_playlist table

                                       Statement statement = connection.createStatement();

                                       PreparedStatement preparedStatement = connection.prepareStatement(sql2);
                                       preparedStatement.setString(1, selection);

                                       statement.execute(sql1);
                                       preparedStatement.executeUpdate();
                                   } catch (SQLException e) {
                                       throw new RuntimeException(e);
                                   }
                               } // if
                           } // else ends
                        } // if ends here that checks if selection is not null
                    } // event handler for deleting playlist ends here

                    // event handler for deleting song
                    if (event.isControlDown() && event.getCode() == KeyCode.DELETE) {
                        if (playlistController.isPlaylistLoaded) { // this if ensures that song can be only deleted when playlist is loaded
                            Song song = playlistController.listView.getSelectionModel().getSelectedItem();
                            boolean confirmation = getConfirmationForDeletingPlaylist(song.title, "song");
                            if (confirmation) { // if for only confirmation
                                String selection = playlistController.playlists.getValue();
                                if (selection.equals("LIKED SONGS")) {
                                    deleteSong(selection, song.filepath);
                                    likedList.remove(song.filepath);
                                    opendPlaylist.remove(song.filepath);
                                    playlistController.objectsOfOpendPlaylist.remove(song);
                                } else {
                                    deleteSong(selection, song.filepath);
                                    opendPlaylist.remove(song.filepath);
                                    playlistController.objectsOfOpendPlaylist.remove(song);
                                }
                                if (playlistController.objectsOfOpendPlaylist.isEmpty()) { // restting everything to nothig when there is no son in listview
                                    timer.stop();
                                    soundLoader.mediaPlayer.dispose();
                                    slider.setMax(100);
                                    slider.setValue(0);
                                    Length.set("00:00");
                                    Live.set("00:00");

                                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
                                    banner.setImage(image);
                                    Title.set("(Song Title)");
                                    Artist.set("Artist Name");
                                    playlistController.setBanner(image);
                                    playlistController.setTitle("Song Title");
                                    playlistController.setArtist("Artist Name");
                                    // setting to default when playlist is empty
                                    playlistController.helperForPlayPause = true;
                                    helperForPlayPause = false;
                                    isSongLoaded = false;
                                    playlistController.isSongLoaded = false;
                                    playpause.setGraphic(play);
                                    playlistController.miniplaypause.setGraphic(playlistController.miniPlay);
                                } // end
                            } // if ends
                        } // if of isPlaylistLoaded ends
                    } // if for deleting song in playlist via key event
                }); // event handler ends here
            } catch (Exception e) {
                System.out.println("Failed to load playlist scene");
                System.out.println(e.fillInStackTrace());
                throw  new RuntimeException(e);
            }
        } else {
            mainStage.setScene(playlistScene);
            mainStage.show();
            isMainSceneOn = false; // this ensures that fullyLoadSong() method can be called when playlist scene is active
            sendSongInfoToPlaylistScene(); // sent info after the scene is live it is crucial
        } // else ends
    } // method ends here

    // this method deletes song from database
    public void deleteSong(String playlist, String songPath) {
        try (Connection connection = DriverManager.getConnection(url)){
            playlist = playlist.toLowerCase();
            playlist = playlist.replace(" ", "_");
            String sql1 = "DELETE FROM " + playlist + " WHERE file_paths = (?);";
            String sql2 = "INSERT INTO deleted_songs (file_paths)" +
                    "VALUES (?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, songPath);
            preparedStatement1.executeUpdate();

            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setString(1, songPath);
            boolean confirmation = checkIfSongIsUsedMoreThanOnce(songPath);

            if (confirmation) {
                preparedStatement2.executeUpdate();
            }

        } catch (SQLException sqlException) {
            System.out.println("Error " + sqlException.getMessage());
        }
    } // method ends

    // this method gets user confirmation before deleting
    public boolean getConfirmationForDeletingPlaylist(String actionOn, String actionOf) {
        Alert alertExit = new Alert(Alert.AlertType.CONFIRMATION);
        alertExit.setTitle("Delete " + actionOf);
        alertExit.setHeaderText("Are you sure about deleting the " + actionOf + " " +actionOn +"?");
        alertExit.setContentText("Please confirm your action.");
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alertExit.getButtonTypes().setAll(deleteButton, cancelButton);
        alertExit.initOwner(mainStage);
        return alertExit.showAndWait().orElse(cancelButton) == deleteButton;
    } // method ends

    // method for sending song info to playlist scene
    public void sendSongInfoToPlaylistScene() {
        if (isSongLoaded) {
            playlistController.setTitle(soundLoader.title);
            playlistController.setArtist(soundLoader.artist);
            playlistController.setBanner(soundLoader.albumArt);
            playlistController.setListView(tellWhatToDoAddOrRemove);
            for (Song song: songsShouldBeRemoved) { // it removes corrupted song from listview
                playlistController.objectsOfOpendPlaylist.remove(song);
            }
        } else {
            playlistController.setTitle("Song Title");
            playlistController.setArtist("Artist Name");
            playlistController.setBanner(art);
        }
        playlistController.setEssentials(isPlaying, helperForPlayPause, isSongLoaded, isPaused);
    } // method ends

    // action listener for add song button
    public void loadSongAddress() {
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // get file name for checking (e.g., charlie puth - attention.mp3)
            String filename = selectedFile.getName().toLowerCase(Locale.ROOT);
            if (filename.endsWith(".mp3") || filename.endsWith(".wav") || filename.endsWith(".asc")) {
                filePath = selectedFile.toURI().toString();
                isSongLoaded = true;
                isExists = true;
                loadSong();
                checkLikedSong();
                soundLoader.mediaPlayer.setOnReady(this::calculateAndSetLength);
                invokeMedia();

                if (isShuffled) { // turnig shuffle to off
                    shuffle.setSelected(false);
                    isShuffled = false;
                } // if ends

                // this if unloads the selected playlist
                if (playlistController!= null && playlistController.isPlaylistLoaded) {
                    // setting combobox to null as no playlist is selected
                    playlistController.playlists.getSelectionModel().clearSelection();
                    playlistController.playlists.setValue(null);
                    playlistController.objectsOfOpendPlaylist.clear(); // removing all songs from listview
                    opendPlaylist.clear(); // removing opendplaylist songs
                    playlistController.loadedPlaylist = "Empty";
                    playlistController.isPlaylistLoaded = false;
                } // if ends

            } // inner if ends
        } // main if ends

    } // method ends here

    // method for extracting metadata
    public void loadSong() {
        if (isPlaying) { // this stops the currently playing song
            soundLoader.mediaPlayer.stop();
            soundLoader.mediaPlayer.dispose(); // clears resources
        } // if ends

        soundLoader.openSong(filePath); // loading new song
        Title.set(soundLoader.title);
        Artist.set(soundLoader.artist);
        banner.setImage(soundLoader.albumArt);
        this.soundLoader.mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty()); // binding slider with volume
        if (helperForPlayPause) { // this plays the newly loaded song
            soundLoader.mediaPlayer.play();
        } // if ends
        countLiveSeconds = 0;
    } // method ends

    // action listener for play and pause
    public void playAndPauseController() {
        if (isSongLoaded) {
            if (!helperForPlayPause) {
                playpause.setGraphic(pause);
                soundLoader.mediaPlayer.play();
                timer.start();
                isPlaying = true;
                helperForPlayPause = true;
                isPaused = false;
            }
            else if (helperForPlayPause) {
                playpause.setGraphic(play);
                timer.stop();
                soundLoader.mediaPlayer.pause();
                helperForPlayPause = false;
                isPaused = true;
            }
        } // outer if
    } // method ends here

    // this method goes backward in playlist
    public void backwardButton() {
        letItPlay = false; // setting it false so listview listener can load selected song
        if (!opendPlaylist.isEmpty()) {
            if (playlistIndex == 0) {
                playlistIndex = opendPlaylist.size() - 1;
            } else {
                playlistIndex--;
            }
            isMainSceneOn = true; // it ensures that fullyLoadSong() method should not be called when main scene is active
            fullyLoadSong(); // this method loads all details of song
            playlistController.listView.getSelectionModel().select(playlistIndex);
        } // if ends
    } // method ends

    // this method goes forward in playlist
    public void forwardButton() {
        letItPlay = false; // setting it false so listview listener can load selected song
        if (!opendPlaylist.isEmpty()) {
            if (playlistIndex == opendPlaylist.size() - 1) {
                playlistIndex = 0;
            } else {
                playlistIndex++;
            }
            isMainSceneOn = true; // it ensures that fullyLoadSong() method should not be called when main scene is active
            fullyLoadSong(); // this method loads all details of song
            playlistController.listView.getSelectionModel().select(playlistIndex);
        } // if ends
    } // meyhod ends

    // this method shows error msg based on active scene
    public void showErrorAndPrepareRemovalOfSong() {
        /* The error message is shown based on active scene */
        if (isMainSceneOn) { // if maine scene is active, then this will display
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setHeaderText("Unable to load song");
            alert.setContentText("Maybe the song is deleted or moved to different location");
            alert.showAndWait();
            songsShouldBeRemoved.add(playlistController.objectsOfOpendPlaylist.get(playlistIndex));
            deleteCorruptedSongFromDatabase();
        } else { // if playlist is active then this will display
            playlistController.removeCorruptedSongAndShowError(playlistIndex); // this method is in playlist scene
            deleteCorruptedSongFromDatabase();
        }
    } // method ends

    // this method is for deleting corrupted songs from databse
    public void deleteCorruptedSongFromDatabase() {
        String playlistName = playlistController.loadedPlaylist;
        playlistName = playlistName.toLowerCase();
        playlistName = playlistName.replace(" ", "_");
        DeleteFilePaths deleteFilePaths = new DeleteFilePaths(erroredSong, playlistName);
        deleteFilePaths.start();
        erroredSong.clear(); // clearing arraylist for next use (if)
    } // method ends

    public void fullyLoadSong() {
        if (!opendPlaylist.isEmpty()) { // this if checks file existance only if playlist is full
            filePath = opendPlaylist.get(playlistIndex);
            isExists = playlistController.checkingFileExistance(filePath);
        }
        if (opendPlaylist.isEmpty()) { // base case for if playlist becoms empty
            Title.set("(Song Title)");
            Artist.set("Artist Name");
            banner.setImage(art);
            isSongLoaded = false;
            return;
        }
        if (isExists) { // base case for recursive function
            isSongLoaded = true;
            if (soundLoader.mediaPlayer != null)
                soundLoader.mediaPlayer.volumeProperty().unbind(); // unbinding volume property to avoid null pointer before loading new song
            loadSong(); // this loads song details
            invokeMedia();
            checkLikedSong();
            soundLoader.mediaPlayer.setOnReady(this::calculateAndSetLength);
            return;
        } else {
            erroredSong.add(filePath); // add errored song for deleting from database
            opendPlaylist.remove(filePath); // remove errored song from open playlist
            showErrorAndPrepareRemovalOfSong(); // to show and remove song
            // to increase playlist index
            if (playlistIndex == opendPlaylist.size() - 1) {
                playlistIndex = 0;
            } else {
                playlistIndex++;
            }
            fullyLoadSong(); // recursive call if unable to load song
        }
    } // method ends

    public boolean checkIfSongIsUsedMoreThanOnce(String str) {
        boolean result = false;
        if (songsInMusicFolder.containsKey(str)) {
            int count = songsInMusicFolder.get(str);
            if (count > 1) {
                count--;
                songsInMusicFolder.put(str, count);
                updateSongsTable(str); // updates the song count if more than 1
                result = false; // false means the song is used in more than one playlist
            } else if (count == 1){
                deleteSongsTableSingleCountSong(str);
                result = true; // true means the song is used in only one playlist so it can be deleted
            }
        }
        return result;
    } // method ends here

    public void deleteSongsTableSingleCountSong(String songPath) {
       try (Connection connection = DriverManager.getConnection(url)){
           String sql = "DELETE FROM songsInAppData WHERE songPath = ?;";
           PreparedStatement preparedStatement = connection.prepareStatement(sql);
           preparedStatement.setString(1, songPath);
           preparedStatement.executeUpdate();
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    } // method ends

    public void updateSongsTable(String songPath) {
        if (songsInMusicFolder.containsKey(songPath)) {
            int newCount = songsInMusicFolder.get(songPath); // you have to update the songsInMusicFolder first
            try (Connection connection = DriverManager.getConnection(url)) {
                String sql = "UPDATE songsInAppData SET songCount = ? WHERE songPath = ?;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(2, songPath);
                preparedStatement.setInt(1, newCount);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } // if ends
    } //  method ends

    // method for settings
    public void setSettings() { // method starts
        if (showSettings == null) {
            showSettings = new Dialog<>();

            showSettings.initOwner(mainStage);
            showSettings.setTitle("Settings");

            VBox content = new VBox();
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(290, 200);

            Label settingLabel = new Label("App Theme");
            Label timerLabel = new Label("Sleep Timer");

            settingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            timerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            option1 = new RadioButton("Light Theme");
            option2 = new RadioButton("Dark Theme");

            ToggleGroup group = new ToggleGroup();
            option1.setToggleGroup(group);
            option2.setToggleGroup(group);
            // setting the selected or default theme selected
            if (appSettings.getTheme().equals("Light Theme")) {
                option1.setSelected(true);
            } else if (appSettings.getTheme().equals("Dark Theme")) {
                option2.setSelected(true);
            } // end

            // adding listeners to togglegroup
            group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                if (newToggle != null) {
                    RadioButton selected = (RadioButton) newToggle; // casting toggle group selected to radiobutton

                    if ("Light Theme".equals(selected.getText())) {
                        // clearing the previous theme and loading dark theme
                        mainScene.getStylesheets().clear();
                        mainScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/MainScene-light-theme.css")));

                        appSettings.setTheme("Light Theme");
                        appSettings.saveSettings(); // updating the json file

                        // changing icons to match theme
                        loadIcons("Light Theme");

                        if (playlistController != null) { // changing default theme for playlistscene
                            // clearing previous theme and applying dark theme to playlist scene
                            playlistScene.getStylesheets().clear();
                            playlistScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/PlaylistScene-light-theme.css")));
                            playlistController.changeComboBoxLineColor("Light Theme");

                            // changing icons to match theme
                            playlistController.loadIcons("Light Theme");
                        } // end
                    } else if ("Dark Theme".equals(selected.getText())) {
                        // clearing the previous theme and loading dark theme
                        mainScene.getStylesheets().clear();
                        mainScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/MainScene-dark-theme.css")));

                        appSettings.setTheme("Dark Theme");
                        appSettings.saveSettings(); // updating the json file

                        // changing the icons to match dark theme
                        loadIcons("Dark Theme");

                        if (playlistController != null) { // changing theme for playlist scene
                            // clearing previous theme and applying dark theme to playlist scene
                            playlistScene.getStylesheets().clear();
                            playlistScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/PlaylistScene-dark-theme.css")));
                            playlistController.changeComboBoxLineColor("Dark Theme");

                            // changing icon to match theme
                            playlistController.loadIcons("Dark Theme");
                        } // if ends
                    } // dark theme if
                } // end
            });

            VBox.setMargin(option1, new Insets(10, 0, 5, 0)); // setting margine to make UI clean
            VBox.setMargin(timerLabel, new Insets(10, 0, 0, 0)); // setting margin to tiler label

            minuteSelection = new JFXComboBox<>(); // creating sleep timer
            minuteSelection.getItems().addAll("5 minutes", "10 minutes", "15 minutes", "30 minutes", "45 minutes", "1 hour", "End of track");
            minuteSelection.setPromptText("Set timer");

            sleepButton = new JFXButton();
            sleepButton.setPrefWidth(20);
            sleepButton.setPrefHeight(24);
            sleepButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/clockOff.png")))));
            sleepButton.getStyleClass().add("transparent-button");

            HBox sleeperBox = new HBox(10, minuteSelection, sleepButton); // creating HBox

            content.getChildren().addAll(settingLabel, option1, option2, timerLabel, sleeperBox);

            showSettings.getDialogPane().setContent(scrollPane);
            showSettings.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        } // if ends

        showSettings.show();
    } // method ends

    // getter for Setting object
    public void setSettingObject(Settings settings) {
        this.appSettings = settings;
    } // end

    public void loadIcons(String theme) {
       if (theme.equals("Light Theme")) {
           play.setImage(null);
           play.setImage(new Image(getClass().getResourceAsStream("/icons/play.png")));
           pause.setImage(null);
           pause.setImage(new Image(getClass().getResourceAsStream("/icons/pause.png")));
           back.setImage(null);
           back.setImage(new Image(getClass().getResourceAsStream("/icons/backward.png")));
           fast.setImage(null);
           fast.setImage(new Image(getClass().getResourceAsStream("/icons/forward.png")));
       } else if (theme.equals("Dark Theme")) {
           play.setImage(null);
           play.setImage(new Image(getClass().getResourceAsStream("/icons/darkPlay.png")));
           pause.setImage(null);
           pause.setImage(new Image(getClass().getResourceAsStream("/icons/darkPause.png")));
           back.setImage(null);
           back.setImage(new Image(getClass().getResourceAsStream("/icons/darkBackward.png")));
           fast.setImage(null);
           fast.setImage(new Image(getClass().getResourceAsStream("/icons/darkFastward.png")));
       }
    } // end method

    public void deleteSongsForDeletedPlaylist(String str) {
        String sql = "CREATE TABLE IF NOT EXISTS deleted_songs (" +
                "file_paths TEXT NOT NULL UNIQUE);";
        String sql1 = "INSERT INTO deleted_songs (file_paths)" +
                "VALUES (?);";
        String sql2 = "SELECT file_paths FROM " + str + ";";

        try(Connection connection = DriverManager.getConnection(url)) {
            Statement statement = connection.createStatement();
            Statement statement1 = connection.createStatement();

            ResultSet resultSet = statement1.executeQuery(sql2); // loads songs from playlist

            ArrayList<String> songsInPlaylist = new ArrayList<>();

            while (resultSet.next()) {
                songsInPlaylist.add(resultSet.getString("file_paths"));
            }

            if (!songsInPlaylist.isEmpty()) {
                statement.execute(sql); // creating deleted song  table;

                for (String key : songsInPlaylist) {

                    if (songsInMusicFolder.containsKey(key)) {
                        Integer value = songsInMusicFolder.get(key);

                        if (value >= 2) {
                            songsInMusicFolder.put(key, value--); // update new songCount value first
                            updateSongsTable(key);
                        } else {
                            songsInMusicFolder.remove(key);
                            deleteSongsTableSingleCountSong(key);
                            PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                            preparedStatement.setString(1, key);
                            preparedStatement.executeUpdate();

                        } // end
                    } // end
                } // outer loop ends
            } // main if ends
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    } // method ends

} // class ends here
