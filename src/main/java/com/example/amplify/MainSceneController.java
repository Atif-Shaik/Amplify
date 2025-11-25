package com.example.amplify;

import com.example.bluetooth.AudioDevicePoller;
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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;


import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

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
    @FXML
    HBox bluetothInfo;

    JFXButton volumeButton, sleepButton;
    JFXComboBox<String> minuteSelection;
    Settings appSettings;

    int lastIndex = 0;
    LinkedHashSet<Integer> removedSongIndex;
    int playlistIndex = 0;
    int countLiveSeconds = 0; // the count veriable counts the real time
    int i;
    int liveSconds = 0, liveMinute = 0;
    int minute, seconds, totalDurationInSeconds, secondsInDuration;
    Duration duration;
    long sleepTimer = 0, selectedTimer;

    SimpleStringProperty Title, Artist, Length, Live, Bluetooth;
    AnimationTimer timer;

    Stage mainStage;
    Scene mainScene, playlistScene, lyricsScene;
    PlaylistController playlistController;
    LyricsController lyricsController;
    ImageView play, back, fast, pause;
    Image art;
    FontIcon  addSongIcon, playlistIcon, no_speakerIcon, dislikeIcon, setting, muteIcon, likeIcon, volumeIcon, speakerIcon, lyricsIcon, bluetooth;
    LinkedList<String> opendPlaylist;
    LinkedList<String> likedList;
    AnchorPane popupContent;
    JFXPopup popup;
    Slider volumeSlider;
    Label volumePercent, bluetoothLabel;

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
    boolean setTimer = false;
    boolean endTrackSelection = false;
    boolean isLyricsSceneCreated = false;
    boolean showBluetoothInfo = true;
    boolean turnOffBluetoothUpdate = false;
    Song removedSongObject;
    String tellWhatToDoAddOrRemove;
    PauseTransition delay, delay1;

    ScheduledExecutorService scheduler;// = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> sleepTask;

    ArrayList<String> erroredSong;
    ArrayList<String> invalidSongTrace;
    ArrayList<Song> songsShouldBeRemoved;
    LinkedHashMap<String, Integer> songsInMusicFolder;
    FileChooser fileChooser;
    SoundLoader soundLoader;
    public AudioDevicePoller devicePoller;


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
        delay1 = new PauseTransition(Duration.seconds(1));

        fileChooser.setTitle("Select an audio file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files","*.mp3", "*.wav", "*.aac"));

        play = new ImageView();
        pause = new ImageView();
        back = new ImageView();
        fast = new ImageView();

        // load custom icons for buttons
        art = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
       
        // loading icons from Icon libraries
        playlistIcon = new FontIcon(BootstrapIcons.MUSIC_NOTE_LIST);
        playlistIcon.setIconSize(32);

        addSongIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        addSongIcon.setIconSize(32);

        likeIcon = new FontIcon(FontAwesomeSolid.THUMBS_UP);
        likeIcon.setIconSize(24);

        dislikeIcon = new FontIcon(FontAwesomeSolid.THUMBS_DOWN);
        dislikeIcon.setIconSize(24);

        setting = new FontIcon(BootstrapIcons.GEAR_FILL);
        setting.setIconSize(24);

        speakerIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        speakerIcon.setIconSize(24);

        volumeIcon = new FontIcon(FontAwesomeSolid.VOLUME_DOWN);
        volumeIcon.setIconSize(24);

        muteIcon = new FontIcon(FontAwesomeSolid.VOLUME_OFF);
        muteIcon.setIconSize(24);

        no_speakerIcon = new FontIcon(FontAwesomeSolid.VOLUME_MUTE);
        no_speakerIcon.setIconSize(24);

        lyricsIcon = new FontIcon(FontAwesomeSolid.MUSIC);
        lyricsIcon.setIconSize(24);

        // setting volume functionality section
        volumeSlider.setPrefSize(150, 40);
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.5);

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
        lyricsAndBanner.setGraphic(lyricsIcon);
        settings.setGraphic(setting);
        addSong.setGraphic(addSongIcon);
        playlist.setGraphic(playlistIcon);
        backward.setGraphic(back);
        forward.setGraphic(fast);
        volume.setGraphic(speakerIcon);
        likeAndDislike.setGraphic(dislikeIcon);

        // add css styles to buttons
        volumeButton.getStyleClass().add("transparent-button");

        // add action listeners to buttons
        lyricsAndBanner.setOnAction(event -> switchToLyricsScene());
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
        delay1.setOnFinished(event -> {
            if (turnOffBluetoothUpdate == false) {
                bluetothInfo.setVisible(true);
            }
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
                    if (endTrackSelection) { // if for sleep Timer
                        sleepTimer++;
                        if (sleepTimer == selectedTimer) {
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
                            sleepTimer = 0;
                            setTimer = false;
                            minuteSelection.getSelectionModel().clearSelection();
                            sleepButton.setText("Set Timer");
                            endTrackSelection = false;
                            isLooped = false;
                            loop.setSelected(false);
                            isShuffled = false;
                            shuffle.setSelected(false);
                            showEndSleepTimer();
                        }
                    } // end
                    if (liveSconds == 60) { // this if converts seconds to minute for displaying
                        liveMinute++;
                        liveSconds = 0;
                    } // inner 1 if ends
                    Live.set(String.format("%02d:%02d", liveMinute, liveSconds));

                    if (lyricsController != null) lyricsController.Live.set(String.format("%02d:%02d", liveMinute, liveSconds));

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
                        if (lyricsController != null) { // same reason as above
                            lyricsController.helperForPlayPause = true;
                            lyricsController.playAndPauseButton();
                        }
                        isPaused = true;
                        resetLiveCountSeconds();
                    } // inner 2 if ends
                    if (countLiveSeconds == totalDurationInSeconds && isLooped) { // this if loops the song when on
                        soundLoader.mediaPlayer.seek(Duration.seconds(0));
                        resetLiveCountSeconds();
                    } // inner 3 if ends
                    if (countLiveSeconds == totalDurationInSeconds && isShuffled && opendPlaylist.size() > 1) { // this if shuffles the playlist
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

        bluetooth = new FontIcon();
        bluetooth.setIconSize(20);
        bluetooth.setIconColor(Paint.valueOf("white"));

        bluetoothLabel = new Label();
        Bluetooth = new SimpleStringProperty("Not connected");
        bluetoothLabel.textProperty().bind(Bluetooth);
        bluetoothLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
        slider.setOnMouseDragged(e -> {showBluetoothInfo = false; bluetothInfo.setVisible(false); delay1.stop();});
        slider.setOnMouseReleased(e -> {showBluetoothInfo = true; delay1.play();});

        bluetothInfo.setSpacing(5);
        bluetothInfo.getChildren().addAll(bluetooth, bluetoothLabel);

       // loadBluetoohInfo(); // loading bluetooth info

       devicePoller = new AudioDevicePoller();

       devicePoller.setOnSucceeded(event -> {
           String deviceName = devicePoller.getValue();
           updateBluetoothUI(deviceName);
       });

       devicePoller.setOnFailed(event -> {
           Bluetooth.set("Detection Error");
           devicePoller.getException().printStackTrace();
       });
       devicePoller.start();
    } // initialize method ends ****************************************************************************************

    public void checkLikedSong() {
        for (String path: likedList) {
            if (path.equals(filePath)) {
                isLiked = true;
                likeAndDislike.setGraphic(likeIcon);
                break;
            } else {
                isLiked = false;
                likeAndDislike.setGraphic(dislikeIcon);
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
        Alert likeAlert = new Alert(Alert.AlertType.NONE);
        likeAlert.initOwner(mainStage);
        ImageView notificationIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
        notificationIcon.setFitWidth(54);
        notificationIcon.setFitHeight(54);
        likeAlert.setTitle("Liked songs");
        likeAlert.setHeaderText(null);
        HBox hBox = new HBox(15);
        hBox.setPadding(new Insets(20, 0,0,20));
        hBox.setPrefSize(390,70);
        VBox vBox = new VBox(1);
        ButtonType gotIt = new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE);
        likeAlert.getDialogPane().getButtonTypes().add(gotIt);

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

                    Alert alert = new Alert(Alert.AlertType.NONE);
                    alert.initOwner(mainStage);
                    alert.setTitle("Like List");

                    ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
                    errorIcon.setFitHeight(54);
                    errorIcon.setFitWidth(54);
                    HBox hBox1 = new HBox(15);
                    hBox1.setPrefSize(415,70);
                    hBox1.setPadding(new Insets(20, 0,0,20));

                    VBox vBox1 = new VBox(1);
                    Label title = new Label("Duplicate song detected!");
                    title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                    Label content = new Label("It appears this song is already in your 'Liked' collection.");
                    content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                    vBox1.getChildren().addAll(title, content);

                    hBox1.getChildren().addAll(errorIcon, vBox1);
                    alert.getDialogPane().setContent(hBox1);
                    alert.setHeaderText(null);

                    ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().add(ok);

                    if (likedList.contains(filePath)) {
                        alert.showAndWait();
                        return; // omiting following code of method (crucial)
                    }
                } // if ends

                if (songsInMusicFolder.containsKey(filePath)) {
                    int cont = songsInMusicFolder.get(filePath); // getting the current count
                    songsInMusicFolder.put(filePath, cont + 1); // update the song count
                    updateSongsTable(filePath);
                } else {
                    addNewSongTrace(filePath); // addew song trace
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
                likeAndDislike.setGraphic(likeIcon);
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
                            // playlistController.listView.getSelectionModel().select(playlistIndex);
                        } // end
                    } // if ends
                } // main if

                Label title = new Label("Song added to liked list!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Happy listening!");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                vBox.getChildren().addAll(title, content);
                hBox.getChildren().addAll(notificationIcon, vBox);
                likeAlert.getDialogPane().setContent(hBox);
                likeAlert.showAndWait();
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
                likeAndDislike.setGraphic(dislikeIcon);

                if (songsInMusicFolder.containsKey(filePath)) {
                    int count = songsInMusicFolder.get(filePath);
                    if (count > 1) {
                        songsInMusicFolder.put(filePath, count - 1);
                        updateSongsTable(filePath); // updating the song count
                    } else if (count == 1) {
                        deleteSongsTableSingleCountSong(filePath);
                        songsInMusicFolder.remove(filePath);

                        PreparedStatement preparedStatement1 = connection.prepareStatement(sql2);
                        preparedStatement1.setString(1,filePath);
                        preparedStatement1.executeUpdate();
                    }
                } // if end

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

                Label title = new Label("Song removed from liked list!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Happy listening!");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                vBox.getChildren().addAll(title, content);
                hBox.getChildren().addAll(notificationIcon, vBox);
                likeAlert.getDialogPane().setContent(hBox);
                likeAlert.showAndWait();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } // try catch ends
        } // else if ends
    } // method ends here

    // action listener for volumeButton
    public void volumeButtonController() {
        if (!isVolumeButtonPressed) {
            isVolumeButtonPressed = true;
            volumeButton.setGraphic(muteIcon);
            volume.setGraphic(no_speakerIcon);
            volumeSlider.setValue(0);
        } // outer if
        else if (isVolumeButtonPressed) {
            isVolumeButtonPressed = false;
            volumeButton.setGraphic(volumeIcon);
            volume.setGraphic(speakerIcon);
            volumeSlider.setValue(0.5);
        } // if ends
    } // method ends here

    // this method handles volume icon for popup for corresponding volumeSlider value
    public void setMuteViaVolumeSlider() {
        double value = volumeSlider.getValue();
        if (value == 0) {
           volumeButton.setGraphic(muteIcon);
           volume.setGraphic(no_speakerIcon);
        } else if (value > 0) {
            volumeButton.setGraphic(volumeIcon);
            volume.setGraphic(speakerIcon);
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
        if (lyricsController != null) lyricsController.Live.set(String.format("%02d:%02d", liveMinute, liveSconds));
    } // method ends

    // invoking mediaPlayer to get timeline
    public void invokeMedia() {
        // this following code is updating slider
        soundLoader.mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            MediaPlayer.Status status = soundLoader.mediaPlayer.getStatus(); // getting the mediaplayer's status

            if (!isDragging && status == MediaPlayer.Status.PLAYING) {
                Platform.runLater(() -> {
                    slider.setValue(newValue.toSeconds());
                    if (lyricsController != null) { // this updates the slider value is lyrics scene
                        lyricsController.slider.setValue(newValue.toSeconds());
                    }
                });
            } // if end
            if (lyricsController != null && status == MediaPlayer.Status.PLAYING && lyricsController.lyricsLoaded == true) {
                double currentSeconds = newValue.toSeconds();

                for (int i = 0; i < lyricsController.lyrics.size() - 1; i++) {
                    double start = lyricsController.lyrics.get(i).time;
                    double next = lyricsController.lyrics.get(i +1).time;

                    // if current time falls between two lyric lines
                    if (currentSeconds >= start && currentSeconds < next) {
                        lyricsController.MainLyrics.set(lyricsController.lyrics.get(i).text);
                        break;
                    }
                }

                // if current time falls between two lyric lines
                if (currentSeconds >= lyricsController.lyrics.get(lyricsController.lyrics.size() - 1).time) {
                    lyricsController.MainLyrics.set(lyricsController.lyrics.get(lyricsController.lyrics.size() - 1).text);
                }
            } // if ended
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

        slider.setOnMousePressed(event -> {
            isDragging = true;
        });

        slider.setOnMouseDragged(event -> {
            showBluetoothInfo = false;  bluetothInfo.setVisible(false); delay1.stop();  // this is for hiding bluetooth detail
        });

        slider.setOnMouseReleased(event -> {
            setNewTimeline();
            showBluetoothInfo = true;
            delay1.play(); // this is for showing bluetooth detail
        });
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
            if (endTrackSelection) { // this will help for en track sleep timmer
                sleepTimer = countLiveSeconds;
            }
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

        if (lyricsController != null) { // this will change the slider value for lyrics scene
            lyricsController.setSliderValue(0, soundLoader.media.getDuration().toSeconds());
            lyricsController.formatSlider();
            lyricsController.setMinuteAndSecond(minute, seconds, liveMinute, liveSconds);
        }
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
        addSong.setDisable(true);
        File selectedFile = fileChooser.showOpenDialog(mainStage);
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

                if (lyricsController != null) {
                    setLyricsSceneToDefault();
                }

            } // inner if ends
        } // main if ends
        addSong.setDisable(false);
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

        if (lyricsController != null) { // this changes the song details for lyrics scene
            lyricsController.songName.set(soundLoader.title);
            lyricsController.artistName.set(soundLoader.artist);
        } // end

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

    // this method only count the songs
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
            Dialog<Void> showSettings = new Dialog<>();
            showSettings.initOwner(mainStage);
            showSettings.setTitle("Settings");

            VBox content = new VBox();
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefWidth(400);
            scrollPane.setPrefHeight(239);

            JFXButton help = new JFXButton("Help center");
            JFXButton policy = new JFXButton("Terms and Privacy policy");
            JFXButton invite = new JFXButton("Share Application");
            JFXButton reset = new JFXButton("Reset Application");
            help.setPrefWidth(366);
            help.setAlignment(Pos.CENTER_LEFT);
            help.getStyleClass().add("button-style-for-settings");
            help.setOnAction(event -> openHelpWindow());
            FontIcon helpIcon = new FontIcon(FontAwesomeSolid.QUESTION_CIRCLE);
            helpIcon.setIconSize(30);
            help.setGraphic(helpIcon);

            reset.setPrefWidth(366);
            reset.setAlignment(Pos.CENTER_LEFT);
            reset.getStyleClass().add("button-style-for-settings");
            reset.setOnAction(event -> resetApp());
            FontIcon resetIcon = new FontIcon(FontAwesomeSolid.REDO_ALT);
            resetIcon.setIconSize(30);
            reset.setGraphic(resetIcon);

            policy.setPrefWidth(366);
            policy.setAlignment(Pos.CENTER_LEFT);
            policy.getStyleClass().add("button-style-for-settings");
            FontIcon policyIcon = new FontIcon(FontAwesomeSolid.SHIELD_ALT);
            policyIcon.setIconSize(30);
            policy.setGraphic(policyIcon);
            policy.setOnAction(e -> openLinks("https://atif-shaik.github.io/AmplifMax-privacy-policy/"));

            invite.setPrefWidth(366);
            invite.setAlignment(Pos.CENTER_LEFT);
            invite.getStyleClass().add("button-style-for-settings");
            FontIcon inviteIcon = new FontIcon(BootstrapIcons.SHARE_FILL);
            inviteIcon.setIconSize(30);
            invite.setGraphic(inviteIcon);
            invite.setOnAction(e -> shareDialog());

            RadioButton option1 = new RadioButton("Light Theme");
            option1.setStyle("-fx-font-size: 14px;");
            option1.setPrefWidth(355);
            RadioButton option2 = new RadioButton("Dark Theme");
            option2.setPrefWidth(355);
            option2.setStyle("-fx-font-size: 14px;");

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

                        if (lyricsController != null) {
                            lyricsScene.getStylesheets().clear();
                            lyricsScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/LyricsScene-light-theme.css")));
                            lyricsController.loadIcons(appSettings.getTheme());
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

                        if (lyricsController != null) {
                            lyricsScene.getStylesheets().clear();
                            lyricsScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/LyricsScene-dark-theme.css")));
                            lyricsController.loadIcons(appSettings.getTheme());
                        } // end
                    } // dark theme if
                } // end
            });

            VBox.setMargin(option1, new Insets(0, 0, 5, 0)); // setting margine to make UI clean
            VBox.setMargin(help, new Insets(5, 0, 0, 0));
            if (minuteSelection == null) {
                minuteSelection = new JFXComboBox<>(); // creating sleep timer
                minuteSelection.getItems().addAll("5 minutes", "10 minutes", "15 minutes", "30 minutes", "45 minutes", "1 hour", "End of track");
                minuteSelection.setPromptText("Set timer");
                minuteSelection.setPrefWidth(200);
                minuteSelection.setStyle("-fx-font-size: 16px;");
            } //

            if (sleepButton == null) {
                sleepButton = new JFXButton("Set Timer");
                sleepButton.setPrefWidth(110);
                sleepButton.setStyle("-fx-background-color: transparent; -fx-border-color:black; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 16px;");
                sleepButton.setOnAction(event -> {
                if (!setTimer && isSongLoaded) {
                        String value = minuteSelection.getValue();
                    if (value != null && !value.isEmpty() && isSongLoaded) {
                            setTimer = true;
                            sleepButton.setText("Stop Timer");
                       switch (value) {
                        case "5 minutes":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(5);
                            break;
                        case "10 minutes":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(10);
                            break;
                        case "15 minutes":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(15);
                            break;
                        case "30 minutes":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(30);
                            break;
                        case "45 minutes":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(45);
                            break;
                        case "1 hour":
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                            startSleepTimer(60);
                            break;
                        case "End of track":
                            endTrackSelection = true;
                            sleepTimer = countLiveSeconds;
                            selectedTimer = totalDurationInSeconds;
                            break;
                        } // switch end
                    }
                } else {
                    if (scheduler != null) {
                        sleepTask.cancel(false);
                        scheduler.shutdownNow();
                    }
                    setTimer = false;
                    minuteSelection.getSelectionModel().clearSelection();
                    sleepButton.setText("Set Timer");
                }
            });
        } // if ends

        HBox sleeperBox = new HBox(10, minuteSelection, sleepButton); // creating HBox
        VBox.setMargin(sleeperBox, new Insets(2,0,0,12));

        TitledPane titledPane = new TitledPane("App Theme", new VBox(option1, option2));
        titledPane.setExpanded(false);
        titledPane.setPrefWidth(366);

        TitledPane titledPane1 = new TitledPane("Sleep Timer", new VBox(sleeperBox));
        titledPane1.setExpanded(false);
        titledPane1.setPrefWidth(366);

        Accordion accordion = new Accordion(titledPane, titledPane1);
        VBox vBox = new VBox(accordion);
        content.getChildren().addAll(vBox, help, policy, reset, invite);
        showSettings.getDialogPane().setContent(scrollPane); // scrollPane
        showSettings.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        showSettings.show();
    } // method ends

    public void openHelpWindow() {
        Dialog<Void> helpWindow = new Dialog<>();
        helpWindow.initOwner(mainStage);
        helpWindow.setTitle("Help Center");
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox();
        content.setPrefSize(455, 490);

        ImageView helpIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/help.png"))));

        Label helpTitle = new Label("How can we help you?");
        helpTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 36px; -fx-text-fill: blue;");

        Label heading1 = new Label("\uD83C\uDFB5 AmplifyMax – Help Guide");
        heading1.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: blue;");

        Label sub1 = new Label("\uD83D\uDCCC Overview");
        sub1.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: blue;");

        Text t1 = new Text("AmplifyMax is an offline music player designed for simplicity and performance. It allows you to manage your music library, create playlists, and enjoy features like shuffle, loop, shortcuts, and themes.");
        t1.setWrappingWidth(430);
        t1.setStyle("-fx-font-size: 18px;");

        Label sub2 = new Label("▶ Getting Started");
        sub2.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: blue;");

        Text s1_1 = new Text("• Play Music: ");
        s1_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st1_1 = new Text("Select a song from your library or playlist\n   and press the ▶ button (or Ctrl + p).\n");
        st1_1.setStyle("-fx-font-size: 18px;");

        Text s1_2 = new Text("• Pause/Resume: ");
        s1_2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st1_2 = new Text("Click the ⏸ button (or Ctrl + p).\n");
        st1_2.setStyle("-fx-font-size: 18px;");

        Text s1_3 = new Text("• Volume: ");
        s1_3.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st1_3 = new Text("Adjust using the volume slider.");
        st1_3.setStyle("-fx-font-size: 18px;");

        TextFlow t2 = new TextFlow(s1_1, st1_1, s1_2, st1_2, s1_3, st1_3);

        Label sub3 = new Label("\uD83C\uDFB6 Lyrics");
        sub3.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: blue;");

        Text s7_1 = new Text("• Add Lyrics : ");
        s7_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st6_1 = new Text("Pick an .lrc file to attach to the song.\n");
        st6_1.setStyle("-fx-font-size: 18px;");

        Text s7_2 = new Text("• Change Lyrics : ");
        s7_2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st6_2 = new Text("Replace the current lyrics with a new\n   file.\n");
        st6_2.setStyle("-fx-font-size: 18px;");

        Text s7_3 = new Text("• Remove Lyrics : ");
        s7_3.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st6_3 = new Text("Detach the lyrics from the song.");
        st6_3.setStyle("-fx-font-size: 18px;");

        TextFlow t7 = new TextFlow(s7_1, st6_1, s7_2, st6_2, s7_3, st6_3);

        Label sub4 = new Label("\uD83D\uDD04 Playback Controls");
        sub4.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: blue;");

        String s2 = "• Loop: Repeats the current song." +
                "\n" +
                "• Shuffle: Plays songs randomly.";

        Text s2_1 = new Text("• Loop: ");
        s2_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st2_1 = new Text("Repeats the current song.\n");
        st2_1.setStyle("-fx-font-size: 18px;");

        Text s2_2 = new Text("• Shuffle: ");
        s2_2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st2_2 = new Text("Plays songs randomly.");
        st2_2.setStyle("-fx-font-size: 18px;");

        TextFlow t3 = new TextFlow(s2_1, st2_1, s2_2, st2_2);

        Label sub5 = new Label("\uD83D\uDCC2 Playlists");
        sub5.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: blue;");

        Text s3_1 = new Text("• Create Playlist: ");
        s3_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st3_1 = new Text("Go to Playlist > Create New Playlist.\n");
        st3_1.setStyle("-fx-font-size: 18px;");

        Text s3_2 = new Text("• Add Songs: ");
        s3_2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st3_2 = new Text("Click on add button and select an audio\n   file.\n");
        st3_2.setStyle("-fx-font-size: 18px;");

        Text s3_3 = new Text("• Delete Songs: ");
        s3_3.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st3_3 = new Text("Right-click a song and select Delete.\n");
        st3_3.setStyle("-fx-font-size: 18px;");

        Text s3_4 = new Text("• Delete Playlist: ");
        s3_4.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st3_4 = new Text("Go to Playlist Management > look for\n   playlist > press Delete button.\n");
        st3_4.setStyle("-fx-font-size: 18px;");

        Text s3_5 = new Text("• Rename Playlist: ");
        s3_5.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st3_5 = new Text("Go to Playlist Management > look\n   for playlist > press Remane button.");
        st3_5.setStyle("-fx-font-size: 18px;");

        TextFlow t4 = new TextFlow(s3_1, st3_1, s3_2, st3_2, s3_3, st3_3, s3_4, st3_4, s3_5, st3_5);

        Label sub6 = new Label("\uD83C\uDFA8 Appearance");
        sub6.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: blue;");

        Text s4_1 = new Text("• Dark/Light Theme: ");
        s4_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text st4_1 = new Text("Toggle from Settings > Theme.");
        st4_1.setStyle("-fx-font-size: 18px;");

        TextFlow t5 = new TextFlow(s4_1, st4_1);

        Label sub7 = new Label("❓ FAQ / Troubleshooting");
        sub7.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: blue;");

        String s4 = "Q: Why won’t my song play?\n" +
                "A: Make sure it’s in a supported format (MP3, WAV, etc.).";

        Text s5_1 = new Text("Q: Why won’t my song play?\n");
        s5_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue;");
        Text s5_2 = new Text("A: ");
        s5_2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Text st5_1 = new Text("Make sure it’s in a supported format (MP3, WAV,\n     ASC).");
        st5_1.setStyle("-fx-font-size: 18px;");

        TextFlow t6 = new TextFlow(s5_1, s5_2, st5_1);

        Label sub8 = new Label("Contact us");
        sub8.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: blue;");

        Text s6_1 = new Text("Have a question or facing an issue? We're here to \nhelp! Contact us anytime for support or feedback.");
        s6_1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Hyperlink hyperlink = new Hyperlink("Email: assist.atifstudios@gmail.com");
        hyperlink.setOnAction(e -> openLinks("mailto:assist.atifstudios@gmail.com"));
        hyperlink.setStyle("-fx-font-size: 18px;");

        JFXButton insta = new JFXButton();
        insta.setPrefSize(30, 30);
        insta.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        FontIcon instaIcon = new FontIcon(FontAwesomeBrands.INSTAGRAM);
        instaIcon.setIconSize(30);
        insta.setGraphic(instaIcon);
        insta.setOnAction(e -> openLinks("https://www.instagram.com/atif_sk_92"));

        JFXButton linkedIn = new JFXButton();
        linkedIn.setPrefSize(30, 30);
        linkedIn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        FontIcon linkedInIcon = new FontIcon(FontAwesomeBrands.LINKEDIN_IN);
        linkedInIcon.setIconSize(30);
        linkedIn.setGraphic(linkedInIcon);
        linkedIn.setOnAction(e -> openLinks("https://www.linkedin.com/in/shaik-atif-05a965355"));

        JFXButton gitHub = new JFXButton();
        gitHub.setPrefSize(30, 30);
        gitHub.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        FontIcon gitHubIcon = new FontIcon(FontAwesomeBrands.GITHUB);
        gitHubIcon.setIconSize(30);
        gitHub.setGraphic(gitHubIcon);
        gitHub.setOnAction(e -> openLinks("https://github.com/Atif-Shaik"));

        HBox socials = new HBox(20);
        socials.getChildren().addAll(insta, linkedIn, gitHub);

        VBox.setMargin(helpIcon, new Insets(10, 0, 0, 162));
        VBox.setMargin(sub8, new Insets(10, 0 , 0, 0));
        VBox.setMargin(helpTitle, new Insets(20, 0, 10, 40));
        VBox.setMargin(heading1, new Insets(15, 0, 0, 0));
        VBox.setMargin(hyperlink, new Insets(0, 0, 0, -5));

        content.getChildren().addAll(helpIcon, helpTitle, heading1, sub1, t1, sub2, t2, sub3, t7, sub4, t3, sub5, t4, sub6, t5, sub7, t6, sub8, s6_1, hyperlink, socials);

        scrollPane.setContent(content);
        helpWindow.getDialogPane().setContent(scrollPane);
        helpWindow.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        helpWindow.showAndWait();
    } // end method

    // this method opens links pressed by users
    public void openLinks(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setHeaderText(null);
            alert.setContentText("Unable to open browser!");
            alert.showAndWait();
        }
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

    private void startSleepTimer(int minutes) {
        if (sleepTask != null && !sleepTask.isDone()) {
            // Cancel any existing task first
            sleepTask.cancel(false);
        }

        // schedule new task
        sleepTask = scheduler.schedule(() -> { // this block will execute after the selected sleep timer finished
            Platform.runLater(() -> {
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
                minuteSelection.getSelectionModel().clearSelection();
                sleepButton.setText("Set Timer");
                sleepTask.cancel(false);
                scheduler.shutdownNow();
                showEndSleepTimer();
            });
        }, minutes, TimeUnit.MINUTES);
    } // method ends

    public void showEndSleepTimer() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Shuffle Error");
        ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
        errorIcon.setFitWidth(54);
        errorIcon.setFitHeight(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(390,70);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Time's up!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("The sleep timer has turned off your music.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(errorIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(ok);

        alert.show();
    } // method ends

    public void shareDialog() {
        Dialog<Void> shareWindow = new Dialog<>();
        shareWindow.initOwner(mainStage);
        shareWindow.setTitle("Share App");
        VBox contentBox = new VBox();
        contentBox.setPrefSize(400, 350);
        contentBox.setAlignment(Pos.TOP_CENTER);

        ImageView shareIcon = new ImageView(new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("/icons/shareImage.png")))));
        shareIcon.setFitWidth(150);
        shareIcon.setFitHeight(150);

        Label label1 = new Label("Let your friends discover your");
        label1.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label label2 = new Label("favorite music app!");
        label2.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label label3 = new Label("Tap the \"Copy Link\" and spread the rhythm!");
        label3.setStyle("-fx-font-size: 16px;");
        JFXButton copyButton = new JFXButton("Copy Link");
        copyButton.setPrefSize(120, 60);
        copyButton.getStyleClass().add("copy");
        Label link = new Label("https://apps.microsoft.com/detail/9N03D6ZR5NBJ?hl=en-us&gl=IN&ocid=pdpshare");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString("I've been using AmplifyMAX, a clean, fast offline music player for desktop! Give it a try:\n" + link.getText());
            content.putUrl(link.getText());
            clipboard.setContent(content);
            copyButton.setText("Copied");
            copyButton.setDisable(true);
        });

        VBox.setMargin(label1, new Insets(15, 0, 0, 0));
        VBox.setMargin(copyButton, new Insets(15, 0, 0, 0));
        contentBox.getChildren().addAll(shareIcon, label1, label2, label3, copyButton);

        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        shareWindow.getDialogPane().getButtonTypes().add(close);
        shareWindow.getDialogPane().setContent(contentBox);

        Button closeButton = (Button) shareWindow.getDialogPane().lookupButton(close);
        closeButton.setManaged(false);
        shareWindow.show();
    } // method ends

    public void switchToLyricsScene() {
        if (!isLyricsSceneCreated) {
            isLyricsSceneCreated = true;
            try {
                FXMLLoader loader = new FXMLLoader(MainSceneController.class.getResource("/com/example/amplify/LyricsScene.fxml"));
                Parent parent = loader.load();
                lyricsController = loader.getController();
                lyricsScene = new Scene(parent, 500, 600);

                if (appSettings.getTheme().equals("Light Theme")) {
                    lyricsScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/LyricsScene-light-theme.css")));
                } else if (appSettings.getTheme().equals("Dark Theme")) {
                    lyricsScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/LyricsScene-dark-theme.css")));
                }
                lyricsScene.setOnKeyPressed(event -> {
                    if (event.isControlDown() && event.getCode() == KeyCode.P) {
                        lyricsController.playAndPauseButton();
                    }
                });
                lyricsController.loadIcons(appSettings.getTheme());
                lyricsController.setController(this);
                sendSongInfoToLyricsScene();
                mainStage.setScene(lyricsScene);
                mainStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendSongInfoToLyricsScene();
            mainStage.setScene(lyricsScene);
            mainStage.show();
        }

    } // method ends

    public void sendSongInfoToLyricsScene() {
        if (isSongLoaded && !opendPlaylist.isEmpty()) { // this still needed
            lyricsController.setStage(mainStage);
            lyricsController.setScene(mainScene);
            lyricsController.setTitleName(soundLoader.title);
            lyricsController.setArtistName(soundLoader.artist);
            lyricsController.setSliderValue(slider.getValue(), soundLoader.media.getDuration().toSeconds());
            lyricsController.formatSlider();
            lyricsController.setMinuteAndSecond(minute, seconds, liveMinute, liveSconds);
            // this if load the lyrics
            if (lyricsController.lyricsInStored.containsKey(filePath)) {
                String lyricsFile = lyricsController.lyricsInStored.get(filePath);
                File file = new File(lyricsFile);
                if (file.exists()) {
                    lyricsController.lyricsLoaded = true; // make it true to show thw lyrics
                    lyricsController.addLyricsLayout.setVisible(false);
                    lyricsController.middleLyrics.setVisible(true);
                    lyricsController.loadLyrics(lyricsController.lyricsInStored.get(filePath));

                    lyricsController.change.setDisable(false);
                    lyricsController.change.setVisible(true);
                    lyricsController.remove.setDisable(false);
                    lyricsController.remove.setVisible(true);
                } else {
                    setLyricsSceneToDefault();
                    deleteLyricsFromDatabase();
                }
            } else { // reverse action
                setLyricsSceneToDefault();
            }
        } else {
            lyricsController.setStage(mainStage);
            lyricsController.setScene(mainScene);
            lyricsController.setTitleName("(Song Title)");
            lyricsController.setArtistName("Artist Name");
            lyricsController.setSliderValue(slider.getValue(), 100);
            lyricsController.setMinuteAndSecond(minute, seconds, liveMinute, liveSconds);
        }
        lyricsController.setEssentials(helperForPlayPause);
    } // method end

    public void loadTheNextLyrics() {
        if (lyricsController != null) {
            boolean lyricsExistsInFolder = lyricsController.lyricsInStored.containsKey(filePath);
            if (lyricsExistsInFolder) {
                String lyricsFile = lyricsController.lyricsInStored.get(filePath);
                File file = new File(lyricsFile);
                if (file.exists()) { // this will check the live status of the lyrics file
                    lyricsController.MainLyrics.set(""); // clearing the last string of lyrics
                    lyricsController.lyricsLoaded = true; // make it true to show thw lyrics
                    lyricsController.addLyricsLayout.setVisible(false);
                    lyricsController.middleLyrics.setVisible(true);
                    lyricsController.loadLyrics(lyricsController.lyricsInStored.get(filePath));

                    lyricsController.change.setDisable(false);
                    lyricsController.change.setVisible(true);
                    lyricsController.remove.setDisable(false);
                    lyricsController.remove.setVisible(true);
                } else {
                    setLyricsSceneToDefault();
                    deleteLyricsFromDatabase();
                } // end
            } else {
                setLyricsSceneToDefault();
            } // end
        } // main if ended
    } // method ends

    public void setLyricsSceneToDefault() {
        lyricsController.lyricsLoaded = false;
        lyricsController.addLyricsLayout.setVisible(true);
        lyricsController.middleLyrics.setVisible(false);

        lyricsController.change.setDisable(true);
        lyricsController.change.setVisible(false);
        lyricsController.remove.setDisable(true);
        lyricsController.remove.setVisible(false);
    } // method ends

    public void deleteLyricsFromDatabase() {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "DELETE FROM Lyrics WHERE song = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, filePath);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lyricsController.lyricsInStored.remove(filePath);
    } // method ends

    public void resetApp() {
        boolean confirmation = getResetConfirmation(mainStage);
        if (confirmation) {

            Path dataPath = Paths.get(System.getenv("LOCALAPPDATA"), "AmplifyMusic");

            if (Files.exists(dataPath)) {
                try {
                    Files.walk(dataPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    showResetMessage();
                    Platform.exit(); // closing the app
                    System.exit(0); // closing the app
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } // end

        } // end
    } // method ends

    public void showResetMessage() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Data Reset Complete");

        ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
        errorIcon.setFitHeight(54);
        errorIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(420,70);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("App data has been reset successfully!");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("Please close and relaunch the app to start fresh.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(errorIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType ok = new ButtonType("Close App", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(ok);

        alert.showAndWait();
    } // method ends

    public static boolean getResetConfirmation(Stage stage) {
        Alert alertExit = new Alert(Alert.AlertType.NONE);
        ImageView exitIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/question.png"))));
        exitIcon.setFitWidth(54);
        exitIcon.setFitHeight(54);
        alertExit.setTitle("Reset App");
        alertExit.setHeaderText(null);
        HBox hBox = new HBox(15);
        hBox.setPadding(new Insets(20, 0,0,20));
        VBox vBox = new VBox(1);
        Label title = new Label("Are you sure you want to reset?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("All user data, including settings, playlist, and cached information, will be wiped and restored to default.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        content.setWrapText(true);
        vBox.getChildren().addAll(title, content);

        HBox.setMargin(exitIcon, new Insets(10, 0, 0, 0));
        hBox.getChildren().addAll(exitIcon, vBox);
        hBox.setPrefSize(410,90);
        alertExit.getDialogPane().setContent(hBox);

        ButtonType exitButton = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alertExit.getButtonTypes().setAll(exitButton, cancelButton);
        alertExit.initOwner(stage);
        return alertExit.showAndWait().orElse(cancelButton) == exitButton;
    } // method ends

    public void updateBluetoothUI(String name) {
        String deviceName = name.trim().toLowerCase();
        boolean isBluetooth =
                deviceName.contains("bluetooth") ||
                        deviceName.contains("bt") ||
                        deviceName.contains("a2dp") ||
                        deviceName.contains("hands-free") ||
                        deviceName.contains("earbuds") ||
                        deviceName.contains("headphones");
        if (isBluetooth) {
            if (deviceName.contains("headphones")) {
                bluetooth.setIconCode(MaterialDesignH.HEADPHONES);
                if (showBluetoothInfo) bluetothInfo.setVisible(true);
                Bluetooth.set(name);
                turnOffBluetoothUpdate = false;
            } else if (deviceName.contains("earbuds")) {
                bluetooth.setIconCode(BootstrapIcons.EARBUDS);
                if (showBluetoothInfo) bluetothInfo.setVisible(true);
                Bluetooth.set(name);
                turnOffBluetoothUpdate = false;
            } else {
                bluetooth.setIconCode(MaterialDesignB.BLUETOOTH);
                if (showBluetoothInfo) bluetothInfo.setVisible(true);
                Bluetooth.set(name);
                turnOffBluetoothUpdate = false;
            }
        } else {
            Bluetooth.set("");
            bluetothInfo.setVisible(false);
            turnOffBluetoothUpdate = true;
        }
    } // method ends

    public void shutdownBluetoothListener() {
        if (devicePoller != null) { // this stops the bluetooth lister
            devicePoller.cancel();
        }
    } // method ends

    public void shutdownSleepTimer() {
        if (scheduler != null) { // this if is important to release scheduler resources
            sleepTask.cancel(false);
            scheduler.shutdownNow();
        }
    } // method ends

} // class ends here
