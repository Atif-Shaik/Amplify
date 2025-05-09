package com.example.amplify;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PlaylistController {
    @FXML
    JFXButton back, miniplaypause, play, add, create, info;
    @FXML
    Label miniTitle, miniArtist;
    @FXML
    ImageView miniBanner;
    @FXML
    JFXComboBox<String> playlists;
    @FXML
    JFXListView<Song> listView;
    @FXML
    TextField input;

    String selection;
    String loadedPlaylist = "Empty";
    MainSceneController mainSceneController;
    FileChooser fileChooser;
    SoundLoader soundLoader;
    String url = "jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + "AmplifyMusic" + File.separator + "appdata.db";
    SimpleStringProperty Title, Artist;
    public boolean isPlaying, isSongLoaded, isPaused;
    public boolean helperForPlayPause;
    public boolean isPlaylistLoaded = false;
    public boolean isCellCustomized = false;
    int invalidSongPath = 0;
    ArrayList<String> removedFilePaths;
    ObservableList<Song> objectsOfOpendPlaylist;

    Stage mainStage;
    Scene mainScene;

    ImageView backIcon, miniPlay, miniPause, infoIcon, insta, linkedin, github;
    HBox socialmediaHolder;
    public void initialize() {
        soundLoader = new SoundLoader();
        fileChooser = new FileChooser();
        removedFilePaths = new ArrayList<>();

        objectsOfOpendPlaylist = FXCollections.observableArrayList();
        // initializing combo box
        playlists.setPromptText("Select Playlist");
        loadPlaylistNames();

        // getting icons
        backIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/back.png"))));
        back.setGraphic(backIcon);

        infoIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/info.png"))));
        info.setGraphic(infoIcon);

        miniPlay = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/miniplay.png"))));
        miniPause = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/minipause.png"))));
        miniplaypause.setGraphic(miniPlay);

        // styling section
        miniplaypause.getStyleClass().add("round-button");
        back.getStyleClass().add("custom-button");
        play.getStyleClass().add("custom-button");
        add.getStyleClass().add("custom-button");
        create.getStyleClass().add("custom-button");
        info.getStyleClass().add("custom-button");

        // add listener section
        back.setOnAction(event -> openMainScene());
        miniplaypause.setOnAction(event -> miniPlayPauseController());
        play.setOnAction(event -> playPlaylist());
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
        info.setOnAction(event -> showInfo());

        fileChooser.setTitle("Select an audio file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files","*.mp3", "*.wav", "*.aac"));

        // binding section
        Title = new SimpleStringProperty("Song Title");
        miniTitle.textProperty().bind(Title);

        Artist = new SimpleStringProperty("Artist Name");
        miniArtist.textProperty().bind(Artist);

        // tooltip section
        Tooltip playPauseTip = new Tooltip("Press Ctrl + P");
        miniplaypause.setTooltip(playPauseTip);

        Tooltip playPlaylistTip = new Tooltip("Play playlist");
        play.setTooltip(playPlaylistTip);

        Tooltip addTip = new Tooltip("Add song");
        add.setTooltip(addTip);

        Tooltip playlist = new Tooltip("Press \"Shift + Delete\" to delete playlist");
        playlists.setTooltip(playlist);

        Tooltip listViewTip = new Tooltip("Press \"Ctrl + Delete\" to delete song");
        listView.setTooltip(listViewTip);

        // preparing social media links
        insta = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/instagram.png"))));
        insta.setStyle("-fx-cursor: hand;");
        linkedin = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/linkedin.png"))));
        linkedin.setStyle("-fx-cursor: hand;");
        github = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/github.png"))));
        github.setStyle("-fx-cursor: hand;");

        JFXRippler instagramLink = new JFXRippler(insta);
        JFXRippler linkedinLink = new JFXRippler(linkedin);
        JFXRippler githubLink = new JFXRippler(github);

        instagramLink.setRipplerFill(Color.GRAY);
        instagramLink.setOnMouseClicked(e -> openLinks("https://www.instagram.com/atif_sk_92"));

        linkedinLink.setRipplerFill(Color.GRAY);
        linkedinLink.setOnMouseClicked(e -> openLinks("https://www.linkedin.com/in/shaik-atif-05a965355"));

        githubLink.setRipplerFill(Color.GRAY);
        githubLink.setOnMouseClicked(e -> openLinks("https://www.github.com/Atif-Shaik"));

        socialmediaHolder = new HBox(10, instagramLink, linkedinLink, githubLink);

    } // initialize method ends here
/************************************************************************************************************************/

    public void addSongs() {
        if (isPlaylistLoaded) {
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase(Locale.ROOT); // converting into string for if statement checking
                if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".asc")) {
                    String filePath;
                    try {
                        String musicFolder = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "Music";
                        Path targetFolderPath = Paths.get(musicFolder); // getting the path of music folder
                        Path targetPath = targetFolderPath.resolve(selectedFile.getName()); // creating the full path for copied song
                        Path sourcePath = selectedFile.toPath(); // getting the path of the selected song

                        // this section checks if song is already in the Music folder
                        String str = targetPath.toString();
                        File file = new File(str);
                        if (!file.exists()) Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING); // copying the song in app sandbox

                        filePath = targetPath.toUri().toString();

                        boolean isFileExists = addSongsToTable(filePath);
                        if (isFileExists) { // this if block is for when the selected song is already in database
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.initOwner(mainStage);
                            alert.setTitle("Error");
                            alert.setHeaderText("This song is already in your playlist");
                            alert.setContentText("Please choose a different song");
                            alert.showAndWait();
                        } else { // this else block add audio to all places
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
        } else { // this else block gets executed when a playlist is not loaded
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setTitle("Error");
            alert.setHeaderText("No playlist is currently loaded");
            alert.setContentText("Please play a playlist to add songs");
            alert.showAndWait();
        } // main if else ended here
    } // method ends here

    // this method creates new playlist
    public void createPlaylistButtonController() {
        String userInput = input.getText();
        if (!userInput.isEmpty()) { // checking for empty input
            userInput = userInput.toUpperCase(); // converting to uppercase
            for (String item : playlists.getItems()) {
                if (item.equals(userInput)) { // checking if playlist already exists
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(mainStage);
                    alert.setTitle("Error");
                    alert.setHeaderText("A playlist with this name already exists");
                    alert.setContentText("Please create a different playlist");
                    alert.showAndWait();
                    input.clear(); // clearing entered string
                    return;
                } // if end
            } // loop ends here
            boolean confirmation = getConfirmationForPlaylistCreation(userInput);
            if (confirmation) {
                addNewPlaylistTable(userInput);
                playlists.getItems().add(userInput.toUpperCase()); // adding playlist to combo box
            }
            input.clear(); // clearing entered string
        } // main if ends here
    } // method ends

    // this method gets confirmation
    public boolean getConfirmationForPlaylistCreation(String playlistName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(mainStage);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Proceed with creating the playlist \"" + playlistName + "\"");
        ButtonType yesButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
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


    public String convertStringForPlaylist(String str) {
        str = str.toLowerCase();
        str = str.replace(" ", "_");
        return str;
    } // method ends here

    // this method loads playlist into listview 
    public void playPlaylist() {
        selection = playlists.getValue(); // getting playlist name from combo box
        if (selection == null) { // checks if selection is not null (means no playlist is selected)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setTitle("Error");
            alert.setHeaderText("No playlist has been selected");
            alert.setContentText("Please select a playlist to begin playback");
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
                    showEmptyPlaylistMessage();
                } else { // this else loads other playlists in listview
                    loadedPlaylist = selection; // adding loaded playlist name (all caps)
                    selection = selection.replace(" ", "_");
                    selection = selection.toLowerCase();
                    loadSongsForListView(selection); // loading new playlist
                    showEmptyPlaylistMessage();
                } // else if ends here

                if (invalidSongPath > 0) { // if for displaying removed songs
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.initOwner(mainStage);
                    if (invalidSongPath == 1){
                        alert.setHeaderText(invalidSongPath + " song has been removed from the playlist");
                    } else {
                        alert.setHeaderText(invalidSongPath + " songs has been removed from playlist");
                    }
                    alert.setContentText("This may be because the song was moved or deleted");
                    alert.showAndWait();
                }// if ends
                listView.getSelectionModel().select(0); // this line sets the initial song after playlist is loaded
            } else { // this else block displays message for currently loaded playlist
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.initOwner(mainStage);
                alert.setHeaderText("This playlist is already loaded in the current view");
                alert.setContentText("Please select different playlist");
                alert.showAndWait();
            } // nested if ends here
            // these following lines work for all playlist (it is must)
            mainSceneController.playlistIndex = 0; // setting index to 0 for default
            mainSceneController.fullyLoadSong(); // loading first song after playlist selection
        } // else block ends here
    } // method ends

    public void showEmptyPlaylistMessage() {
        if (objectsOfOpendPlaylist.isEmpty()) { // checks for empty playlist
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.initOwner(mainStage);
            alert.setHeaderText("The current playlist is empty");
            alert.setContentText("Please add songs to begin playback");
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
                } // else ends

            } // method
        }); // cell factory ends here
    } // method ends

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

    public void showInfo() {
        Dialog<Void> infoDialog = new Dialog<>();
        infoDialog.setTitle("About Amplify-Music");

        VBox allContent = new VBox(10);
        Text heading1 = new Text("How Amplify-Music works?");
        heading1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-underline: true; ");

        Text appWork = getText();
        appWork.setWrappingWidth(360);

        Text heading2 = new Text("Bug and Glitch Reporting");
        heading2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-underline: true; ");

        Text reportText = new Text("Please report any bugs or glitches you encounter while using the application. Your feedback is valuable in helping us improve its performance and stability.");
        reportText.setWrappingWidth(370);
        reportText.setStyle("-fx-font-size: 18px;");

        Hyperlink emailLink = new Hyperlink("Email: atifshaik78692@gmail.com");
        emailLink.setOnAction(e -> openLinks("mailto:atifshaik78692@gmail.com"));
        emailLink.setStyle("-fx-font-size: 18px;");

        Text heading3 = new Text("Follow the developer");
        heading3.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-underline: true; ");

        Text heading4 = new Text("Resources Attribution");
        heading4.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-underline: true; ");

        Text subHeading1 = new Text("This app uses icons from Freepik.");
        subHeading1.setStyle("-fx-font-size: 18px;");

        Text subHeading2 = new Text("The original creators and copyright holders are acknowledged and appreciated.");
        subHeading2.setStyle("-fx-font-size: 18px;");
        subHeading2.setWrappingWidth(360);

        Hyperlink freepik = new Hyperlink("www.freepik.com");
        freepik.setOnAction(e -> openLinks("https://www.freepik.com"));
        freepik.setStyle("-fx-font-size: 18px;");

        Text heading5 = new Text("App version");
        heading5.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-underline: true;");

        Text version = new Text("1.0.0.0");
        version.setStyle("-fx-font-size: 18px;");

        allContent.getChildren().addAll(heading1, appWork, heading2, reportText, emailLink, heading3, socialmediaHolder, heading4, subHeading1, subHeading2, freepik, heading5, version);

        ScrollPane scrollPane = new ScrollPane(allContent);
        scrollPane.setPrefSize(400,400);

        infoDialog.initOwner(mainStage);
        infoDialog.getDialogPane().setContent(scrollPane);
        infoDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        infoDialog.showAndWait();
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

    private static Text getText() {
        Text appWork = new Text("This application facilitates local audio \nplayback by directly accessing files stored on the user's device. It's crucial to understand that the application relies on the file's original path for playback and playlist management. Therefore, any modifications to the audio file's location, such as renaming or moving it, or its deletion, will render it inaccessible to the application. Consequently, the affected song will be automatically removed from the playlist. To ensure uninterrupted playback and maintain playlist integrity, users are advised to store audio files in a designated, stable directory prior to importing them into the application.");
        appWork.setStyle("-fx-font-size: 18px; -fx-padding: 5px 0;");
        return appWork;
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

} // class ends
