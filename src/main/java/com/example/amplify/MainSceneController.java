package com.example.amplify;

import com.example.sound.Song;
import com.example.sound.SoundLoader;
import com.jfoenix.controls.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.*;
import java.util.*;

public class MainSceneController {
    @FXML
    JFXButton playpause, backward, forward, addSong, playlist, volume, likeAndDislike;
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

    JFXButton volumeButton;

    int lastIndex = 0;
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
    ImageView play, back, fast, add, list, pause, speaker, volumeIcon, mute, like, dislike;
    Image art;
    LinkedList<String> opendPlaylist;
    LinkedList<String> likedList;
    AnchorPane popupContent;
    JFXPopup popup;
    Slider volumeSlider;
    Label volumePercent;

    Random random;
    String url = "jdbc:sqlite:src/main/resources/DataBase/appdata.db";
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
    Song removedSongObject;
    String tellWhatToDoAddOrRemove;

    ArrayList<String> erroredSong;
    ArrayList<Song> songsShouldBeRemoved;
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
        erroredSong = new ArrayList<>();
        songsShouldBeRemoved = new ArrayList<>();
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

        fileChooser.setTitle("Select an audio file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files","*.mp3", "*.wav", "*.aac"));

        // load custom icons for buttons
        art = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
        play = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png"))));
        pause = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png"))));
        back = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/backward.png"))));
        fast = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/forward.png"))));
        add = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/add.png"))));
        list = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playlist.png"))));
        speaker = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/speaker.png"))));
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
        popupContent.getChildren().addAll(volumeButton, volumeSlider, volumePercent);

        // add custom icons to buttons
        playpause.setGraphic(play);
        addSong.setGraphic(add);
        playlist.setGraphic(list);
        backward.setGraphic(back);
        forward.setGraphic(fast);
        volume.setGraphic(speaker);
        likeAndDislike.setGraphic(dislike);

        // add css styles to buttons
        playlist.getStyleClass().add("custom-button");
        addSong.getStyleClass().add("custom-button");
        forward.getStyleClass().add("custom-effect1");
        backward.getStyleClass().add("custom-effect1");
        volume.getStyleClass().add("custom-effect1");
        likeAndDislike.getStyleClass().add("custom-effect1");

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
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(mainStage);
                alert.setTitle("Error");
                alert.setHeaderText("Shuffle requires more than one song in the playlist");
                alert.setContentText("Please add more songs to " + playlistController.playlists.getValue() + " playlist");
                alert.showAndWait();
                isShuffled = false;
                shuffle.setSelected(false);
            } // if ends
            if (isShuffled && opendPlaylist.isEmpty()){ // this if handles shuffle when playlist is not selected
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(mainStage);
                alert.setTitle("Error");
                alert.setHeaderText("Unable to shuffle playlist.");
                alert.setContentText("Please load a playlist to enable this feature.");
                alert.showAndWait();
                isShuffled = false;
                shuffle.setSelected(false);
            } // if ends
        }); // shuffle listener ends
        volume.setOnAction(event -> showVolumePane());
        volumeSlider.setOnMouseReleased(event -> setMuteViaVolumeSlider());
        volumeButton.setOnAction(event -> volumeButtonController());
        likeAndDislike.setOnAction(event -> likeAndDiskikeController());
        backward.setOnAction(event -> backwardButton());
        forward.setOnAction(event -> forwardButton());

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
                        soundLoader.mediaPlayer.seek(javafx.util.Duration.seconds(0));
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
        openLikedList(); // loading liked song list
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
    public void openLikedList() {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "SELECT file_paths FROM liked_songs;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String data = resultSet.getString("file_paths");
                likedList.add(data);
            } // loop ends
        } catch (SQLException e) {
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

    // this method is for like and dislike functionality
    public void likeAndDiskikeController() {
        Alert commonAlert = new Alert(Alert.AlertType.INFORMATION);
        commonAlert.initOwner(mainStage);
        commonAlert.setTitle("Liked songs");
        commonAlert.setHeaderText(null);
        if (!isLiked && isSongLoaded) {
            try (Connection connection = DriverManager.getConnection(url)){
                // writing file path of liked song to database
                String sql = "INSERT INTO liked_songs (file_paths)" +
                        "VALUES (?);";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, filePath);
                preparedStatement.executeUpdate();

                isLiked = true;
                likeAndDislike.setGraphic(like);
                likedList.add(filePath);

                if (playlistController != null) {
                    // this if adds the liked song to the listview if the liked songs list is selected
                    if (playlistController.loadedPlaylist.equals("LIKED SONGS"))  {
                        playlistController.soundLoader.openSong(filePath); // preparing song object
                        Song song = new Song(playlistController.soundLoader.albumArt, filePath, playlistController.soundLoader.title, playlistController.soundLoader.artist);
                        removedSongObject = song;
                        updateListview = true;
                        tellWhatToDoAddOrRemove = "Add";
                        opendPlaylist.add(filePath);
                        if (lastRemovedLikedSong.equals(filePath)) { // this if does not let the reload the song
                            dontReplay = false;
                        } // end
                    } // if ends
                } // main if
                commonAlert.setContentText("Song added to liked list!");
                commonAlert.showAndWait();
            } catch (SQLException e) {
                System.out.println(filePath);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(mainStage);
                alert.setTitle("Error");
                alert.setHeaderText("Unable to add Song to liked list!");
                alert.setContentText("Please contact us!");
                alert.showAndWait();
            } // try catch ends
        } // if ends
        else if (isLiked && isSongLoaded) {
            try (Connection connection = DriverManager.getConnection(url)){
                // deleting song from database
                String sql = "DELETE FROM liked_songs WHERE file_paths = (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, filePath);
                preparedStatement.executeUpdate();
                isLiked = false;
                likeAndDislike.setGraphic(dislike);
                if (playlistController != null) {
                    if (playlistController.loadedPlaylist.equals("LIKED SONGS")) {
                        letItPlay = true; // setting it true so that listview cannot load other song and this song can still finish its playback
                        dontReplay = false;
                        for (int i = 0; i < playlistController.objectsOfOpendPlaylist.size(); i++) {
                            Song file = playlistController.objectsOfOpendPlaylist.get(i);
                            updateListview = true;
                            removedSongObject = file;
                            tellWhatToDoAddOrRemove = "Remove";
                            if (file.filepath.equals(filePath)) {
                                likedList.remove(filePath);
                                opendPlaylist.remove(filePath);
                                lastRemovedLikedSong = filePath;
                                break;
                            }
                        } // loop ends
                    } // if inner
                } // main if
                commonAlert.setContentText("Song removed from liked list!");
                commonAlert.showAndWait();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(mainStage);
                alert.setTitle("Error");
                alert.setHeaderText("Unable to delete Song from liked list!");
                alert.setContentText("Please contact us!");
                alert.showAndWait();
            } // try catch ends
        } // else if ends
    } // method ends here

    // action listener for volumeButton
    public void volumeButtonController() {
        if (!isVolumeButtonPressed) {
            isVolumeButtonPressed = true;
            volumeButton.setGraphic(mute);
            volumeSlider.setValue(0);
        } // outer if
        else if (isVolumeButtonPressed) {
            isVolumeButtonPressed = false;
            volumeButton.setGraphic(volumeIcon);
            volumeSlider.setValue(0.5);
        } // if ends
    } // method ends here

    // this method handles volume icon for popup for corresponding volumeSlider value
    public void setMuteViaVolumeSlider() {
        double value = volumeSlider.getValue();
        if (value == 0) {
           volumeButton.setGraphic(mute);
        } else if (value > 0) {
            volumeButton.setGraphic(volumeIcon);
        } // if ends
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            popup.hide();
        });
        delay.play();
    } // method ends

    // method for volume
    public void showVolumePane() {
        popup.show(volume, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT);
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
            soundLoader.mediaPlayer.seek(javafx.util.Duration.seconds(slider.getValue()));
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
                FXMLLoader loader = new FXMLLoader(MainSceneController.class.getResource("PlaylistScene.fxml"));
                Parent root = loader.load();
                playlistScene = new Scene(root, 500, 600);
                root.requestFocus(); // requesting focus for key events
                playlistScene.getStylesheets().add(String.valueOf(getClass().getResource("PlaylistScene.css")));
                // loading playlist controller
                playlistController = loader.getController();
                playlistController.setStage(mainStage); // sending stage reference only one time
                playlistController.setScene(mainScene); // sending scene reference only one time
                playlistController.setController(this);
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
                System.out.println(e);
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
            String sql = "DELETE FROM " + playlist + " WHERE file_paths = (?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, songPath);
            preparedStatement.executeUpdate();

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
            for (Song song: songsShouldBeRemoved) {
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
            } // inner if ends
        } // if ends
        // this if unloads the selected playlist
        if (playlistController!= null && playlistController.isPlaylistLoaded) {
            // setting combobox to null
            playlistController.playlists.getSelectionModel().clearSelection();
            playlistController.playlists.setValue(null);
            playlistController.objectsOfOpendPlaylist.clear(); // removing all songs from listview
            opendPlaylist.clear();
            playlistController.loadedPlaylist = "Empty";
            playlistController.isPlaylistLoaded = false;
        } // if ends
        if (isShuffled) {
            shuffle.setSelected(false);
            isShuffled = false;
        }
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
        filePath = opendPlaylist.get(playlistIndex);
        if (!opendPlaylist.isEmpty()) { // this if checks file existance only if playlist is full
            isExists = playlistController.checkingFileExistance(filePath);
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


} // class ends here
