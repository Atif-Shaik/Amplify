package com.example.amplify;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.sound.Song;
import com.example.sound.SoundLoader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

public class PlaylistController {
    @FXML
    JFXButton back, miniplaypause, load, add, create, manage;
    @FXML
    Label miniTitle, miniArtist, maxLabel;
    @FXML
    ImageView miniBanner;
    @FXML
    JFXComboBox<String> playlists;
    @FXML
    JFXListView<Song> listView;
    @FXML
    TextField input;
    @FXML
    AnchorPane playlistSceneFXML, miniAnchor;

    String selection;
    String loadedPlaylist = "Empty";
    MainSceneController mainSceneController;
    FileChooser fileChooser;
    SoundLoader soundLoader;
    String url = "jdbc:sqlite:" + System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appdata.db";
    SimpleStringProperty Title, Artist;
    public boolean isPlaying, isSongLoaded, isPaused;
    public boolean helperForPlayPause;
    public boolean isPlaylistLoaded = false;
    public boolean isCellCustomized = false;
    int invalidSongPath = 0, maxText = 18;
    ArrayList<String> removedFilePaths;
    ObservableList<Song> objectsOfOpendPlaylist;

    Stage mainStage;
    Scene mainScene;

    ImageView backIcon, miniPlay, miniPause, infoIcon, insta, linkedin;

    public void initialize() {
        soundLoader = new SoundLoader();
        fileChooser = new FileChooser();
        removedFilePaths = new ArrayList<>();

        objectsOfOpendPlaylist = FXCollections.observableArrayList();
        // initializing combo box
        playlists.setPromptText("Select Playlist");
        loadPlaylistNames();

        ContextMenu menu = new ContextMenu();
        MenuItem rename = new MenuItem("Rename playlist");
        MenuItem shuffle = new MenuItem("Shuffle the playlist");
        MenuItem delete = new MenuItem("Delete the playlist");
        menu.getItems().addAll(rename, shuffle, delete);

        //playlists.setContextMenu(menu);
        playlists.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
              //  playlists.getItems().clear();
                event.consume();
                System.out.println("Ok");
            }
        });

        // getting icons
        backIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/back.png"))));
        back.setGraphic(backIcon);

        infoIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/managePlaylist.png"))));
        manage.setGraphic(infoIcon);

        miniPlay = new ImageView();
        miniPause = new ImageView();

        miniplaypause.setGraphic(miniPlay); // this is important
        // styling section
        miniplaypause.getStyleClass().add("round-button");

        // add listener section
        back.setOnAction(event -> openMainScene());
        miniplaypause.setOnAction(event -> miniPlayPauseController());
        load.setOnAction(event -> loadPlaylist());
        listView.setItems(objectsOfOpendPlaylist); // binding observable list to listview
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (mainSceneController.dontReplay) { // this if ensures that the unliked song can still be played and it prevents loading new song while the unliked song is still playing
                    mainSceneController.playlistIndex = listView.getSelectionModel().getSelectedIndex();
                    isSongLoaded = true;
                    if (!mainSceneController.isMainSceneOn) { // this is important to prevent double loading song
                        mainSceneController.fullyLoadSong(); // loading the song details for main scene
                    }
                    if (mainSceneController.letItUpdateInlistviewListener) {
                        Title.set(newValue.title); // setting mini title
                        Artist.set(newValue.artist); // setting mini artist name
                        miniBanner.setImage(newValue.art); // setting mini banner

                    } else { // when the song is corrupted this will load the next song in playlist to mini bar
                        Title.set(mainSceneController.soundLoader.title);
                        Artist.set(mainSceneController.soundLoader.artist);
                        miniBanner.setImage(mainSceneController.soundLoader.albumArt);
                        mainSceneController.letItUpdateInlistviewListener = true;
                        listView.getSelectionModel().select(mainSceneController.playlistIndex);
                    }
                } // if ends

                mainSceneController.letItPlay = false;
                mainSceneController.dontReplay = true; // this line ensures that all other selected songs should be loaded
            } // if ends
        });
        create.setOnAction(event -> createPlaylistButtonController());
        add.setOnAction(event -> addSongs());
        manage.setOnAction(event -> playlistManagement());
        input.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > maxText) {
                input.setText(oldText);
                maxLabel.setVisible(true);
            } else {
                maxLabel.setVisible(false);
            }
        });
        input.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!input.isFocused() && !create.isFocused()) {
                input.clear();
            }
        });
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) create.requestFocus();
        });
        input.setTextFormatter( new TextFormatter<String>(change -> {
            String newText = change.getControlNewText(); // allow letters, numbers, and space
            if (newText.matches("[a-zA-Z0-9\\s]*")) {
                return  change; // accept
            }
            return null; // reject
        }));
        maxLabel.setVisible(false);
        input.setStyle("-fx-font-size: 15px;");

        fileChooser.setTitle("Select an audio file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files","*.mp3", "*.wav", "*.aac"));

        // binding section
        Title = new SimpleStringProperty("Song Title");
        miniTitle.textProperty().bind(Title);

        Artist = new SimpleStringProperty("Artist Name");
        miniArtist.textProperty().bind(Artist);

    } // initialize method ends here
/************************************************************************************************************************/

    public void addSongs() {
        if (isPlaylistLoaded) {
            add.setDisable(true);
            File selectedFile = fileChooser.showOpenDialog(mainStage);
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase(Locale.ROOT); // converting into string for if statement checking
                if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".asc")) {
                    String filePath;
                    try {
                        String musicFolder = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "Music";

                        Path targetFolderPath = Paths.get(musicFolder); // getting the path of music folder
                        Path targetPath = targetFolderPath.resolve(selectedFile.getName()); // creating the full path for copied song
                        Path sourcePath = selectedFile.toPath(); // getting the path of the selected

                        filePath = targetPath.toAbsolutePath().toUri().toString();

                        boolean isFileExists = addSongsToTable(filePath);
                        if (isFileExists) { // this if block is for when the selected song is already in database
                            Alert alert = new Alert(Alert.AlertType.NONE);
                            alert.initOwner(mainStage);
                            alert.setTitle("Add Song Error");

                            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                            errorIcon.setFitHeight(54);
                            errorIcon.setFitWidth(54);
                            HBox hBox = new HBox(15);
                            hBox.setPrefSize(420,70);
                            hBox.setPadding(new Insets(20, 0,0,20));

                            VBox vBox = new VBox(1);
                            Label title = new Label("This song is already in your playlist!");
                            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                            Label content = new Label("Please choose a different song.");
                            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                            vBox.getChildren().addAll(title, content);

                            hBox.getChildren().addAll(errorIcon, vBox);
                            alert.getDialogPane().setContent(hBox);
                            alert.setHeaderText(null);

                            ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                            alert.getButtonTypes().add(ok);

                            alert.showAndWait();
                        } else { // this else block add audio to all places
                            // this section checks if song is already in the Music folder
                            String str = targetPath.toString();

                            File file = new File(str);
                            if (!file.exists()) {
                                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING); // copying the song in app sandbox
                                mainSceneController.songsInMusicFolder.put(filePath,1);
                                addNewSongTrace(filePath);
                            }
                            else if (file.exists()) {
                                if (mainSceneController.songsInMusicFolder.containsKey(filePath)) {
                                    int num = mainSceneController.songsInMusicFolder.get(filePath);
                                    mainSceneController.songsInMusicFolder.put(filePath, num+1);
                                    mainSceneController.updateSongsTable(filePath);
                                }
                            } // else if ends copying song

                            mainSceneController.dontReplay = true; // this ensures that song can be loaded when clicked
                            soundLoader.openSong(filePath);
                            Song song = new Song(soundLoader.albumArt, filePath, soundLoader.title, soundLoader.artist);
                            objectsOfOpendPlaylist.add(song); // adding new song to listview
                            mainSceneController.opendPlaylist.add(filePath); // adding new song to opened playlist

                            String selection = playlists.getValue(); // this is for adding song to liked list

                            if (selection.equals("LIKED SONGS")) { // this if adds song to liked list data abd sets liked icon to like button
                                mainSceneController.likedList.add(filePath);
                                mainSceneController.isLiked = true;
                                mainSceneController.likeAndDislike.setGraphic(mainSceneController.like);
                            } // end
                        } // else ends
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } // if ends
            } // if ends
            add.setDisable(false);
        } else { // this else block gets executed when a playlist is not loaded
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(mainStage);
            alert.setTitle("Add Song Error");

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
            errorIcon.setFitHeight(54);
            errorIcon.setFitWidth(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,70);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("No playlist is currently loaded!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label("Please load a playlist to add songs.");
            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        } // main if else ended here
    } // method ends here

    public void addNewSongTrace(String songPath) {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "INSERT INTO songsInAppData (songPath, songCount)" +
                    "VALUES (?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, songPath);
            preparedStatement.setInt(2, 1);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    } // method ends

    // this method creates new playlist
    public void createPlaylistButtonController() {
        String userInput = input.getText();
        if (!userInput.isEmpty()) { // checking for empty input
            userInput = userInput.toUpperCase(); // converting to uppercase

            if (playlists.getItems().contains(userInput)) { // checking if playlist already exists
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.initOwner(mainStage);
                alert.setTitle("Create Playlist");

                ImageView questionIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                questionIcon.setFitHeight(54);
                questionIcon.setFitWidth(54);
                HBox hBox = new HBox(15);
                hBox.setPrefSize(430,70);
                hBox.setPadding(new Insets(20, 0,0,20));

                VBox vBox = new VBox(1);
                Label title = new Label("This playlist name is already taken!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Please create a different playlist.");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                vBox.getChildren().addAll(title, content);

                hBox.getChildren().addAll(questionIcon, vBox);
                alert.getDialogPane().setContent(hBox);
                alert.setHeaderText(null);

                ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(ok);

                alert.showAndWait();
                input.clear(); // clearing entered string
            } else {
                boolean confirmation = getConfirmationForPlaylistCreation(userInput);
                if (confirmation) {
                    addNewPlaylistTable(userInput); // creating new table
                    playlists.getItems().add(userInput.toUpperCase()); // adding playlist to combo box
                }
                input.clear(); // clearing entered string
            }
        } // main if ends here
    } // method ends

    // this method gets confirmation
    public boolean getConfirmationForPlaylistCreation(String playlistName) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(mainStage);
        alert.setTitle("Create Playlist");

        ImageView questionIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/question.png"))));
        questionIcon.setFitHeight(54);
        questionIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(430,70);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Would you like to create this playlist?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label(playlistName);
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(questionIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);
        return alert.showAndWait().orElse(noButton) == yesButton;
    } // method ends

    // this method adds selected song to table
    public boolean addSongsToTable(String file) {
        try (Connection connection = DriverManager.getConnection(url)){
            String selection = playlists.getValue();
            selection = selection.toLowerCase();
            selection = selection.replace(" ", "_");

            String sql = "INSERT INTO " + selection + " (file_paths)" +
                    "VALUES (?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, file);
            preparedStatement.executeUpdate();
            return false;
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            return true;
        }
    } // method ends

    // this method creates playlist table in database
    public void addNewPlaylistTable(String table) {
        table = table.replace(" ", "_");
        table = table.toLowerCase();
        try (Connection connection = DriverManager.getConnection(url)){
            String sql1 = "CREATE TABLE IF NOT EXISTS " + table + "(" +
                    "file_paths TEXT NOT NULL UNIQUE);";
            String sql2 = "INSERT INTO all_playlists (playlists)" +
                    "VALUES (?);";
            Statement statement = connection.createStatement();

            PreparedStatement preparedStatement = connection.prepareStatement(sql2);
            preparedStatement.setString(1,table);

            statement.execute(sql1);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } // method ends here

    // this method loads playlist into listview 
    public void loadPlaylist() {
        selection = playlists.getValue(); // getting playlist name from combo box
        if (selection == null) { // checks if selection is not null (means no playlist is selected)
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(mainStage);
            alert.setTitle("Load Error");

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
            errorIcon.setFitHeight(54);
            errorIcon.setFitWidth(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,70);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("No playlist has been selected!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label("Please select a playlist to begin playback.");
            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        } else  { // this else block loads all playlist
            if (!selection.equals(loadedPlaylist)) { // this if ensures that only unloaded playlist can be loaded
                if (!objectsOfOpendPlaylist.isEmpty()) { // clearing previous list's songs
                    objectsOfOpendPlaylist.clear();
                } // end
                if (!mainSceneController.opendPlaylist.isEmpty()) { // clearing previous opened playlist
                    mainSceneController.opendPlaylist.clear();
                } // end
                if (!isCellCustomized) { // this if ensures that the custom cell factory is only created once
                    customizeListView(); // calling cell factory for customization
                    isCellCustomized = true;
                } // if ends

                isSongLoaded = true;
                mainSceneController.isSongLoaded = true;
                isPlaylistLoaded = true;
                helperForPlayPause = false; // it should and must always be false here

                if (selection.equals("LIKED SONGS")) { // this if loads liked songs playlist in listview
                    loadSongsForListView("liked_songs"); // loading playlist
                    loadedPlaylist = "LIKED SONGS";
                    if (mainSceneController.opendPlaylist.size() == 1) {
                        mainSceneController.isShuffled = false;
                        mainSceneController.shuffle.setSelected(false);
                    }
                    showInvalidAudioFileMessage();
                    showEmptyPlaylistMessage();
                } else { // this else loads other playlists in listview
                    loadedPlaylist = selection; // adding loaded playlist name (all caps)
                    selection = selection.replace(" ", "_");
                    selection = selection.toLowerCase();
                    loadSongsForListView(selection); // loading new playlist
                    if (mainSceneController.opendPlaylist.size() == 1) {
                        mainSceneController.isShuffled = false;
                        mainSceneController.shuffle.setSelected(false);
                    }
                    showInvalidAudioFileMessage();
                    showEmptyPlaylistMessage();
                } // else if ends here

                listView.getSelectionModel().select(0); // this line sets the initial song after playlist is loaded
                // these following lines work for all playlist (it is must)
                mainSceneController.playlistIndex = 0; // setting index to 0 for default
                mainSceneController.fullyLoadSong(); // loading first song after playlist selection
            } else { // this else block displays message for currently loaded playlist
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.setTitle("Load Error");
                alert.initOwner(mainStage);

                ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
                errorIcon.setFitWidth(54);
                errorIcon.setFitHeight(54);
                HBox hBox = new HBox(15);
                hBox.setPrefSize(390,70);
                hBox.setPadding(new Insets(20, 0,0,20));

                VBox vBox = new VBox(1);
                Label title = new Label("This playlist is already loaded!");
                title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                Label content = new Label("Please select a different playlist.");
                content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                vBox.getChildren().addAll(title, content);

                hBox.getChildren().addAll(errorIcon, vBox);
                alert.getDialogPane().setContent(hBox);
                alert.setHeaderText(null);

                ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().add(ok);

                alert.showAndWait();
            } // nested if ends here
        } // else block ends here
    } // method ends

    public void showInvalidAudioFileMessage() {
        if (invalidSongPath > 0) { // if for displaying removed songs
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Warning");
            alert.initOwner(mainStage);

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
            errorIcon.setFitHeight(54);
            errorIcon.setFitWidth(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,70);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("Audio file is missing!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label();

            if (invalidSongPath == 1){
                content.setText(invalidSongPath + " song has been removed from the playlist.");
            } else {
                content.setText(invalidSongPath + " song has been removed from the playlist.");
            }

            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        }// if ends
    } // method ends

    public void showEmptyPlaylistMessage() {
        if (objectsOfOpendPlaylist.isEmpty()) { // checks for empty playlist
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Load Error");
            alert.initOwner(mainStage);

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/error.png"))));
            errorIcon.setFitWidth(54);
            errorIcon.setFitHeight(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,70);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("This playlist is empty!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label("Please add songs to begin playback.");
            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        }
    } // method ends

    public void loadSongsForListView(String name) {
        try (Connection connection = DriverManager.getConnection(url)){
           String sql = "SELECT file_paths FROM " + name + ";"; // name is table name
           Statement statement = connection.createStatement();
           ResultSet resultSet = statement.executeQuery(sql);

           if (!mainSceneController.opendPlaylist.isEmpty()) { // clears existing data for new data
               mainSceneController.opendPlaylist.clear();
           } // end

           int help = 0; // this ensures that only first song detail should br displayed in mini display bar
           // this loop adds items to listview from database
           boolean checkValidation = false;
           invalidSongPath = 0; // resetting the invalid song counter for next playlist
           while (resultSet.next()) {
               String path = resultSet.getString("file_paths");
               checkValidation = checkingFileExistance(path); // this method will check the validation of song
               if (checkValidation) { // if for opening only valid songs
                   soundLoader.openSong(path); // loading song details for preparing song object
                   Song song = new Song(soundLoader.albumArt, path, soundLoader.title, soundLoader.artist);
                   if (help == 0) {
                       Title.set(song.title);
                       Artist.set(song.artist);
                       miniBanner.setImage(song.art);
                   }
                   mainSceneController.opendPlaylist.add(path); // adding songs to opendPlaylist LinkedList
                   objectsOfOpendPlaylist.add(song); // adding song to listview via observableList (binding)
                   help = 1;
               } else {
                   invalidSongPath++; // counter for removed songs
                   removedFilePaths.add(path); // adding removed file paths for deleting file paths from database
               } // else ends
           } // loop ends
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (invalidSongPath > 0) { // removing corrupted file paths from databas
            DeleteFilePaths deleteFilePaths = new DeleteFilePaths(removedFilePaths,name);
            deleteFilePaths.start();
            removedFilePaths.clear();
        } // if ends
    } // method ends

    // this method is used for checking valid file path
    public boolean checkingFileExistance(String file)  {
        boolean result;
        try {
            URI uri = new URI(file);
            File audioFile = new File(uri);
            if (audioFile.exists()) {
                result = true;
            } else {
                result = false;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return result;
    } // method ends here

    // method for custom cell for list view
    public void customizeListView() {
        listView.setCellFactory(lv -> new JFXListCell<>() {
            ImageView image;
            Label title;
            Label artist;
            HBox hBox;
            VBox vBox;

            @Override
            public void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (song.getSelectedFilepath() != null) {
                        image = new ImageView(song.art);
                        image.setFitHeight(90);
                        image.setFitWidth(90);
                    } else {
                        image.setImage(null);
                    }
                    title = new Label(song.title);
                    title.setFont(Font.font("Candara", FontWeight.BOLD, FontPosture.ITALIC, 25));

                    artist = new Label(song.artist);
                    artist.setFont(Font.font("Arial", 15));

                    vBox = new VBox(title, artist);
                    hBox = new HBox(image, vBox);
                    hBox.getStyleClass().add("custom-cell");
                    setText(null); // this is important it does not add the default toString method's content of class
                    setGraphic(hBox);

                    ContextMenu menu = new ContextMenu();
                    MenuItem shuffle = new MenuItem("Shuffle the playlist");
                    shuffle.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/shuffle.png")))));
                    shuffle.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
                    shuffle.setOnAction(e -> shufflePlaylist());

                    MenuItem loop = new MenuItem("Loop the song");
                    loop.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/loop.png")))));
                    loop.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
                    loop.setOnAction(e -> loopTheSong());

                    MenuItem delete = new MenuItem("Delete the song");
                    delete.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/delete.png")))));
                    delete.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
                    delete.setOnAction( e -> removeSong());
                    menu.getItems().addAll(shuffle, loop, delete);
                    setContextMenu(menu);
                } // else ends

            } // method
        }); // cell factory ends here
    } // method ends

    public void shufflePlaylist() {
        if (!mainSceneController.isShuffled) {
           mainSceneController.isShuffled = true;
           mainSceneController.shuffle.setSelected(true);
           if (!(mainSceneController.opendPlaylist.size() == 1) && mainSceneController.isLooped) {
               mainSceneController.isLooped = false;
               mainSceneController.loop.setSelected(false);
           } // end (This if ensures that, if there is only one song in playlist and loop is on then when user hit shuffle button the action should not turn off the loop)

           if(!mainSceneController.opendPlaylist.isEmpty() && mainSceneController.opendPlaylist.size() > 1) {
               Alert alert = new Alert(Alert.AlertType.NONE);
               alert.initOwner(mainStage);
               alert.setTitle("Shuffle Mode");

               ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
               errorIcon.setFitHeight(54);
               errorIcon.setFitWidth(54);
               HBox hBox = new HBox(15);
               hBox.setPrefSize(390,70);
               hBox.setPadding(new Insets(20, 0,0,20));

               VBox vBox = new VBox(1);
               Label title = new Label("The playlist is now shuffled!");
               title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
               Label content = new Label("To turn it off, just tap the shuffle button again.");
               content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

               vBox.getChildren().addAll(title, content);

               hBox.getChildren().addAll(errorIcon, vBox);
               alert.getDialogPane().setContent(hBox);
               alert.setHeaderText(null);

               ButtonType ok = new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE);
               alert.getButtonTypes().add(ok);

               alert.showAndWait();
           }
        } else {
            mainSceneController.isShuffled = false;
            mainSceneController.shuffle.setSelected(false);
        }
    } // method end

    public void loopTheSong() {
        if (!mainSceneController.isLooped) {
            mainSceneController.isLooped = true;
            mainSceneController.loop.setSelected(true);
            mainSceneController.isShuffled = false;
            mainSceneController.shuffle.setSelected(false);

            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(mainStage);
            alert.setTitle("Loop Mode");

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
            errorIcon.setFitHeight(54);
            errorIcon.setFitWidth(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,70);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("This song will loop now!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label("To turn it off, just tap the loop button again.");
            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");

            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        } else {
            mainSceneController.isLooped = false;
            mainSceneController.loop.setSelected(false);
        }
    } // method ends

    public void removeSong() {
        boolean confirmation = confirmDeletion();
        if (confirmation) {
            Song song = listView.getSelectionModel().getSelectedItem();
            String selectedPlaylist = playlists.getValue();
            for (Map.Entry<String, Integer> entry : mainSceneController.songsInMusicFolder.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();

                if (key.equals(song.filepath)) {
                    if (value >= 2) {
                        value--;
                        mainSceneController.songsInMusicFolder.put(key, value);
                        mainSceneController.updateSongsTable(key);
                        deleteOnlySong(selectedPlaylist, key);
                    } else {
                        mainSceneController.deleteSongsTableSingleCountSong(key);
                        deleteSongAndSendItToDelete(selectedPlaylist, key);
                    }
                    break;
                }
            } // loop end

            if (selectedPlaylist.equals("LIKED SONGS")) {
                mainSceneController.likedList.remove(song.filepath);
                mainSceneController.opendPlaylist.remove(song.filepath);
                objectsOfOpendPlaylist.remove(song);
            } else {
                mainSceneController.opendPlaylist.remove(song.filepath);
                objectsOfOpendPlaylist.remove(song);
            }

            if (objectsOfOpendPlaylist.isEmpty()) { // resetting everything to default when there is no song in listview
                resetEverythingToDefault();
            } // if end
        } // main if
    } // method ends

    public void resetEverythingToDefault() {
        mainSceneController.timer.stop();
        if (mainSceneController.soundLoader.mediaPlayer != null) mainSceneController.soundLoader.mediaPlayer.dispose();
        mainSceneController.slider.setMax(100);
        mainSceneController.slider.setValue(0);
        mainSceneController.Length.set("00:00");
        mainSceneController.Live.set("00:00");
        mainSceneController.Title.set("(Song Title)");
        mainSceneController.Artist.set("Artist Name");

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/banner.png")));
        mainSceneController.banner.setImage(image);
        setBanner(image);
        setTitle("Song Title");
        setArtist("Artist Name");

        // setting to default when playlist is empty
        mainSceneController.helperForPlayPause = false;
        helperForPlayPause = true;
        isSongLoaded = false;
        mainSceneController.isSongLoaded = false;
        mainSceneController.playpause.setGraphic(mainSceneController.play);
        miniplaypause.setGraphic(miniPlay);
    } // method end

    // this method deletes song from database and send it for deletion
    public void deleteSongAndSendItToDelete(String playlist, String songPath) {
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
            preparedStatement2.executeUpdate();

        } catch (SQLException sqlException) {
            System.out.println("Error " + sqlException.getMessage());
        }
    } // method ends

    // this method deletes only file path from table
    public void deleteOnlySong(String playlist, String songPath) {
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

    public boolean confirmDeletion() {
        Song song = listView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.NONE);

        ImageView BannerIcon = new ImageView(song.art);
        BannerIcon.setFitHeight(54);
        BannerIcon.setFitWidth(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(430,70);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Would you like to delete this song?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label(song.title);
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(BannerIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);

        alert.setTitle("Delete song");
        alert.initOwner(mainStage);
        ButtonType delete = new ButtonType("Yes, delete", ButtonBar.ButtonData.YES);
        ButtonType cancel = new ButtonType("No, keep", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(delete, cancel);
        return alert.showAndWait().orElse(cancel) == delete;
    } // end

    // this method loads all playlist names
    public void loadPlaylistNames() {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "SELECT playlists FROM all_playlists;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String data = resultSet.getString("playlists");
                data = data.replace("_", " ");
                data = data.toUpperCase();
                playlists.getItems().add(data);
            } // loop

        } catch (SQLException e) {
            System.out.println(url);
            throw new RuntimeException(e);
        }
    } // method ends

    /***************         Getter section           ***************/
    public void setStage(Stage stage) {
        this.mainStage = stage;
    } // end
    // getter for scene
    public void setScene(Scene scene) {
        this.mainScene = scene;
    } // end
    // getter for title
    public void setTitle(String title) {
        Title.set(title);
    } // end
    // getter for artist name
    public void setArtist(String artist) {
        Artist.set(artist);
    } // end
    // getter for banner
    public void setBanner(Image image) {
        miniBanner.setImage(image);
    } // end
    // getter for mainSceneController
    public void setController(MainSceneController mainSceneController) {
        this.mainSceneController = mainSceneController;
    } // end
    public void setEssentials(boolean isPlaying, boolean helperForPlayPause, boolean isSongLoaded, boolean isPaused) {
        this.helperForPlayPause = helperForPlayPause;
        this.isSongLoaded = isSongLoaded;
        this.isPlaying = isPlaying;
        this.isPaused = isPaused;
        if (isSongLoaded) { // this ensures that play button only get changed when song is loaded
            if (isPlaying && !isPaused) {
                miniplaypause.setGraphic(miniPause);
            } else if (isPlaying && isPaused) {
                miniplaypause.setGraphic(miniPlay);
            }
        } // if ends
    } // ends
    public void setListView(String action) {
        if (mainSceneController.updateListview) {
            if (action.equals("Add")) {
                if (mainSceneController.clearAllRemovedSongs && !mainSceneController.removedSongIndex.isEmpty()) {
                    for (Integer i : mainSceneController.removedSongIndex) {
                        objectsOfOpendPlaylist.remove(i.intValue());
                    }
                }
                mainSceneController.dontReplay = false;
                objectsOfOpendPlaylist.add(mainSceneController.removedSongObject);
                listView.getSelectionModel().select(mainSceneController.removedSongObject);
                mainSceneController.playlistIndex = objectsOfOpendPlaylist.indexOf(mainSceneController.removedSongObject);
            } else if (action.equals("Remove")) {
                objectsOfOpendPlaylist.remove(mainSceneController.removedSongObject);
                listView.getSelectionModel().select(null);
            }
            mainSceneController.updateListview = false;
            mainSceneController.removedSongIndex.clear(); // clearing
        }
    } // ends

    // method for going back to main scene
    public void openMainScene() {
        mainStage.setScene(mainScene);
        mainStage.show();
    } // method ends here

    // method for mini play pause button
    public void miniPlayPauseController() {
        if (isSongLoaded) {
            if (helperForPlayPause) {
                helperForPlayPause = false; // should always be false here
                mainSceneController.helperForPlayPause = true; // should always be true here
                miniplaypause.setGraphic(miniPlay);
            } else if (!helperForPlayPause) {
                helperForPlayPause = true; // should always be true here
                mainSceneController.helperForPlayPause = false; // should always be false here
                miniplaypause.setGraphic(miniPause);
            }
            mainSceneController.playAndPauseController(); // calling the play pause controller from main scene
        } // if ends
    } // method ends

    public void playlistManagement() {
        Dialog<Void> playlistManagementDialog = new Dialog<>();
        playlistManagementDialog.setTitle("Manage Playlists");

        VBox allPlaylists = new VBox();
        allPlaylists.setPrefSize(360, 200);
        ScrollPane scrollPane = new ScrollPane(allPlaylists);
        scrollPane.setPrefSize(400,200);

        for (String playlist: playlists.getItems()) {
            final String playlistName = playlist;
            HBox leftHBox = new HBox();
            leftHBox.setPrefSize(280, 40);
            leftHBox.setAlignment(Pos.CENTER_LEFT);

            Label playlistLabel = new Label();
            playlistLabel.setStyle("-fx-font-weight: bold;");
            SimpleStringProperty label = new SimpleStringProperty();
            playlistLabel.textProperty().bind(label);
            label.set(playlist);

            leftHBox.getChildren().add(playlistLabel);

            HBox rightHBox = new HBox();
            rightHBox.setPrefSize(80,40);
            rightHBox.setAlignment(Pos.CENTER_RIGHT);
            HBox hBox = new HBox();
            if (!playlist.equals("LIKED SONGS")) {
                JFXButton deletButton = new JFXButton();
                deletButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/deletePlaylist.png")))));

                deletButton.setOnAction(event -> {
                    boolean toDo = getConfirmationFromUser(playlistName);
                    if (toDo) {
                        mainSceneController.deleteSongsForDeletedPlaylist(playlistName.toLowerCase().replace(" ", "_"));
                        String sql = "DROP TABLE IF EXISTS " + playlistName.toLowerCase().replace(" ", "_") + ";";
                        String sql2 = "DELETE FROM all_playlists WHERE playlists = (?);";
                        String sql3 = "SELECT file_paths FROM " + playlistName.toLowerCase().replace(" ", "_");
                        String sql4 = "INSERT INTO deleted_songs (file_paths)" +
                                "VALUES (?);";

                        try (Connection connection = DriverManager.getConnection(url)) {
                            Statement statement = connection.createStatement();
                            Statement statement1 = connection.createStatement();

                            // count the song trace before deleting the playlist table
                            ResultSet resultSet = statement1.executeQuery(sql3);
                            while (resultSet.next()) {
                                String path = resultSet.getString("file_paths");
                                if (mainSceneController.songsInMusicFolder.containsKey(path)) {
                                    Integer value = mainSceneController.songsInMusicFolder.get(path);
                                    if (value >= 2) {
                                        value--;
                                        mainSceneController.songsInMusicFolder.put(path, value);
                                        mainSceneController.updateSongsTable(path);
                                    } else {
                                        mainSceneController.songsInMusicFolder.remove(path);
                                        mainSceneController.deleteSongsTableSingleCountSong(path);

                                        PreparedStatement preparedStatement = connection.prepareStatement(sql4);
                                        preparedStatement.setString(1, path);
                                        preparedStatement.executeUpdate();
                                    }
                                } // if ended
                            } // loop end

                            PreparedStatement preparedStatement = connection.prepareStatement(sql2);
                            preparedStatement.setString(1, playlistName.toLowerCase().replace(" ", "_"));

                            preparedStatement.executeUpdate();
                            statement.execute(sql);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (loadedPlaylist.equals(playlistName)) {
                            objectsOfOpendPlaylist.clear();
                            resetEverythingToDefault();
                        }
                        allPlaylists.getChildren().remove(hBox); // removing playlist option from delete panel
                        playlists.getItems().remove(playlistName); // removing playlist from

                    } // end
                }); // delete button onAction ends

                deletButton.getStyleClass().add("transparent-button");

                JFXButton edit = new JFXButton();
                edit.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/edit.png")))));
                edit.setOnAction(event -> { // edit button onAction
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.initOwner(mainStage);
                    dialog.setTitle("Edit playlist name");
                    HBox box = new HBox(20);
                    box.setPrefSize(350,50);
                    Label max = new Label("max");
                    max.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: red;");
                    max.setVisible(false);

                    Button renameButton = new Button("Rename");
                    renameButton.setPrefSize(80,35);

                    TextField textField = new TextField();
                    textField.setPromptText("Enter new name");
                    textField.setPrefSize(240, 35);

                    renameButton.setOnAction(e -> { // this will rename the table
                        String newName = textField.getText();
                        if (!newName.isEmpty()) {
                            newName = newName.trim().toLowerCase().replace(" ", "_");
                            String sql = "ALTER TABLE " + playlistName.toLowerCase().replace(" ", "_") + " RENAME TO " + newName + ";";
                            String sql1 = "UPDATE all_playlists SET playlists = ? WHERE playlists = ?;";

                            try (Connection connection = DriverManager.getConnection(url)) {
                                Statement statement = connection.createStatement();

                                PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                                preparedStatement.setString(1, newName);
                                preparedStatement.setString(2, playlistName.toLowerCase().replace(" ", "_"));

                                statement.execute(sql);
                                preparedStatement.executeUpdate();
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            String selectedPlaylist = playlists.getValue();
                            int index = playlists.getItems().indexOf(playlistName);
                            if (index != -1) {
                                playlists.getItems().set(index, newName.toUpperCase().replace("_", " "));
                                if (selectedPlaylist.equals(playlistName))  {
                                    playlists.getSelectionModel().select(index); // if the renamed playlist is selected in combo box then this is crucial
                                    loadedPlaylist = newName.toUpperCase().replace("_", " ");
                                }
                            }
                            label.set(newName.toUpperCase().replace("_", " "));
                            dialog.close();
                        } // if ends
                    }); // rename button onAction end
                    StackPane base = new StackPane(textField, max);
                    StackPane.setAlignment(max, Pos.CENTER_RIGHT); // putting max label on TextField
                    StackPane.setMargin(max, new Insets(0, 6, 0, 0)); // adjusting margin

                    box.getChildren().addAll(base, renameButton);

                    textField.setOnKeyPressed( e -> { // this is to shift focus on rename button
                        if (textField.isFocused() && e.getCode() == KeyCode.ENTER) {
                            renameButton.requestFocus();
                        } else if (textField.isFocused() && e.getCode() == KeyCode.RIGHT) {
                            renameButton.requestFocus();
                        }
                    }); // end
                    textField.textProperty().addListener((obs, oldText, newText) -> {
                        if (newText.length() > maxText) {
                            textField.setText(oldText);
                            max.setVisible(true);
                        } else {
                            max.setVisible(false);
                        }
                    });
                    textField.setTextFormatter( new TextFormatter<String>(change -> {
                        String newText = change.getControlNewText(); // allow letters, numbers, and space
                        if (newText.matches("[a-zA-Z0-9\\s]*")) {
                            return  change; // accept
                        }
                        return null; // reject
                    }));
                    textField.setStyle("-fx-font-size: 15px;");

                    ButtonType close = ButtonType.CLOSE;
                    dialog.getDialogPane().getButtonTypes().add(close);
                    Button closeButton = (Button) dialog.getDialogPane().lookupButton(close);
                    closeButton.setManaged(false);
                    dialog.getDialogPane().setContent(box);
                    dialog.show();

                });

                edit.getStyleClass().add("transparent-button");
                rightHBox.getChildren().addAll(edit, deletButton);
            } // end

            hBox.getChildren().addAll(leftHBox, rightHBox);
            hBox.setPrefSize(300,40);

            allPlaylists.getChildren().add(hBox);
        } // loop ends

        playlistManagementDialog.initOwner(mainStage);
        playlistManagementDialog.getDialogPane().setContent(scrollPane);
        ButtonType close = ButtonType.CLOSE;
        playlistManagementDialog.getDialogPane().getButtonTypes().add(close);
        Button button = (Button) playlistManagementDialog.getDialogPane().lookupButton(close);

        playlistManagementDialog.show();
    } // method ends

    public boolean getConfirmationFromUser(String operand) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Delete playlist");

        ImageView questionIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/question.png"))));
        questionIcon.setFitWidth(54);
        questionIcon.setFitHeight(54);
        HBox hBox = new HBox(15);
        hBox.setPrefSize(430,70);
        hBox.setPadding(new Insets(20, 0,0,20));

        VBox vBox = new VBox(1);
        Label title = new Label("Would you like to delete this playlist?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("Once the playlist is deleted, it cannot be undone.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(questionIcon, vBox);
        alert.getDialogPane().setContent(hBox);
        alert.setHeaderText(null);
        
        ButtonType yes = new ButtonType("Yes, delete", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No, keep", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);
        alert.initOwner(mainStage);
        return alert.showAndWait().orElse(no) == yes;
    } // method ends

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

    public void removeCorruptedSongAndShowError(int index) {
        Platform.runLater(() -> { // this platform run later ensures that message is displayed after following process is finished
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setHeaderText("Unable to load song");
            alert.setContentText("Maybe the song is deleted or moved to different location");
            alert.showAndWait();
        });

        objectsOfOpendPlaylist.remove(index); // removing error song based on listview index
        mainSceneController.letItUpdateInlistviewListener = false;
        mainSceneController.songsShouldBeRemoved.clear();
    } // method ends

    public void changeComboBoxLineColor(String theme) {
        if (theme.equals("Light Theme")) {
            playlists.setFocusColor(Paint.valueOf("#4059A9")); // changing the color of underline of combobox
            playlists.setUnFocusColor(Paint.valueOf("#4059A9"));
        } else if (theme.equals("Dark Theme")) {
            playlists.setFocusColor(javafx.scene.paint.Paint.valueOf("#008000"));
            playlists.setUnFocusColor(Paint.valueOf("#008000"));
        }
    } // ends

    public void loadIcons(String theme) {
        if (theme.equals("Light Theme")) {
            miniPlay.setImage(null);
            miniPlay.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/miniplay.png"))));
            miniPause.setImage(null);
            miniPause.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/minipause.png"))));
        } else if (theme.equals("Dark Theme")) {
            miniPlay.setImage(null);
            miniPlay.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/darkMiniPlay.png"))));
            miniPause.setImage(null);
            miniPause.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/darkMiniPause.png"))));
        }
    } // method ends

} // class ends
