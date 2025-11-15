package com.example.amplify;

import com.example.sound.LyricsLine;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricsController {
    @FXML
    JFXSlider slider;

    @FXML
    JFXButton playAndpause, backward, forward, back, addLyrics, change, remove;

    @FXML
    Label title, artist, liveSeconds, totalSeconds, middleLyrics;

    @FXML
    AnchorPane lyricsSceneFXML, baseOfLyrics, addLyricsLayout;

    Stage mainStage;
    Scene mainScene;
    MainSceneController mainSceneController;
    SimpleStringProperty songName, artistName, Lenght, Live, MainLyrics;
    FontIcon backIcon, changeIcon, removeIcon;

    boolean helperForPlayPause = false;
    boolean lyricsLoaded = false;
    FileChooser fileChooser;

    ImageView playIcon, pauseIcon, forwardIcon, backwardIcon;

    Pattern pattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)");

    List<LyricsLine> lyrics;
    Map<String, String> lyricsInStored;
    String url = "jdbc:sqlite:" + System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appdata.db";

    public void initialize() {
        playIcon = new ImageView();
        pauseIcon = new ImageView();
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select a lyrics file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Lyrics files", "*.lrc"));

        lyrics = new ArrayList<>();
        lyricsInStored = new LinkedHashMap<>();
        loadSavedLyrics();
        middleLyrics.setVisible(false);

        playAndpause.setOnAction(event -> playAndPauseButton());
        slider.setOnMousePressed(event -> { mainSceneController.isDragging = true;});
        slider.setOnMouseReleased(event -> setNewTimeline());
        back.setOnAction(event -> switchScene());
        addLyrics.setOnAction(event -> selectAndAddLyrics());
        change.setOnAction(event -> changeTheLyricsFile());
        remove.setOnAction(event -> removeTheLyricsFile());

        songName = new SimpleStringProperty();
        title.textProperty().bind(songName);
        artistName = new SimpleStringProperty();
        artist.textProperty().bind(artistName);
        Lenght = new SimpleStringProperty();
        totalSeconds.textProperty().bind(Lenght);
        Live = new SimpleStringProperty();
        liveSeconds.textProperty().bind(Live);
        MainLyrics = new SimpleStringProperty();
        middleLyrics.textProperty().bind(MainLyrics);
        middleLyrics.setWrapText(true);
        middleLyrics.setMaxWidth(457);

        backIcon = new FontIcon(FontAwesomeSolid.REPLY);
        backIcon.setIconSize(32);
        back.setGraphic(backIcon);

        changeIcon = new FontIcon(FontAwesomeSolid.EXCHANGE_ALT);
        changeIcon.setIconSize(15);
        change.setGraphic(changeIcon);

        removeIcon = new FontIcon(FontAwesomeSolid.UNLINK);
        removeIcon.setIconSize(15);
        remove.setGraphic(removeIcon);

        remove.setVisible(false);
        remove.setDisable(true);

        change.setVisible(false);
        change.setDisable(true);

        backward.setVisible(false);
        backward.setDisable(true);

        forward.setVisible(false);
        forward.setDisable(true);

        Tooltip changTip = new Tooltip("Change Lyrics");
        change.setTooltip(changTip);
        Tooltip removeTip = new Tooltip("Remove Lyrics");
        remove.setTooltip(removeTip);

    } // initialize ended

    public void loadSavedLyrics() {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "SELECT song, lyrics FROM Lyrics;";
            String sql1 = "DELETE FROM Lyrics WHERE song = ?;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String song = resultSet.getString("song");
                String lyrics = resultSet.getString("lyrics");

                File file = new File(lyrics);
                if (file.exists()) {
                    lyricsInStored.put(song, lyrics);
                } else {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                    preparedStatement.setString(1, song);
                    preparedStatement.executeUpdate();
                } // end
            } // loop ends

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } // method end

    public void setController(MainSceneController mainSceneController) {
        this.mainSceneController = mainSceneController;
    }

    public void setEssentials(boolean helperForPlayPause) {
        this.helperForPlayPause = helperForPlayPause;
        if (!helperForPlayPause) {
            playAndpause.setGraphic(playIcon);
        } else {
            playAndpause.setGraphic(pauseIcon);
        }
    } // method ends

    public void setTitleName(String title) {
        songName.set(title);
    }
    public void setArtistName(String artist) {
        artistName.set(artist);
    }
    public void setStage(Stage stage) {this.mainStage = stage;}
    public void setScene(Scene scene) {this.mainScene = scene;}

    public void setSliderValue(double start, double end) {
        slider.setValue(start);
        slider.setMax(end);
    } // method ends

    public void formatSlider() {slider.setLabelFormatter(new DurationStringConverter());}

    public void setMinuteAndSecond(int minutes, int seconds, int liveMinutes, int liveSeconds) {
        Lenght.set(String.format("%02d:%02d", minutes, seconds));
        Live.set(String.format("%02d:%02d", liveMinutes, liveSeconds));
    } // method ends

    public void loadIcons(String theme) {
        if (theme.equals("Light Theme")) {
            playIcon.setImage(null);
            playIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png"))));
            pauseIcon.setImage(null);
            pauseIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png"))));
        } else if (theme.equals("Dark Theme")) {
            playIcon.setImage(null);
            playIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/darkPlay.png"))));
            pauseIcon.setImage(null);
            pauseIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/darkPause.png"))));
        }
    } // method ends

    public void playAndPauseButton() {
        if (mainSceneController.isSongLoaded) {
            if (helperForPlayPause) {
                playAndpause.setGraphic(playIcon);
                mainSceneController.helperForPlayPause = true;
                helperForPlayPause = false;
            } else if (!helperForPlayPause) {
                playAndpause.setGraphic(pauseIcon);
                mainSceneController.helperForPlayPause = false;
                helperForPlayPause = true;
            }
            mainSceneController.playAndPauseController();
        }
    } // method ends

    public void setNewTimeline() {
        double newTime = slider.getValue();
        mainSceneController.slider.setValue(newTime);
        mainSceneController.setNewTimeline();
    } // method ends

    public void switchScene() {
        mainStage.setScene(mainScene);
        mainStage.show();
    } // method ends

    public void loadLyrics(String filePath) {
        lyrics.clear(); // clearing the lyrics for reuse
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){ // "C:\\Users\\atifs\\Downloads\\Attention - Charlie Puth.lrc"
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    // Extract captured groups
                    int minutes = Integer.parseInt(matcher.group(1));
                    int seconds = Integer.parseInt(matcher.group(2));
                    String fractionPart = matcher.group(3); // optional
                    String text = matcher.group(4).trim(); // lyrics text

                    double fraction = 0.0;
                    if (fractionPart != null) {
                        fraction = Double.parseDouble("0." + fractionPart);
                    }

                    double totalSeconds = minutes * 60 + seconds + fraction;

                    lyrics.add(new LyricsLine(totalSeconds, text));
                    // Sort by time (safety)
                    lyrics.sort(Comparator.comparingDouble(lyricsLine -> lyricsLine.time));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    } // method ends

    public void selectAndAddLyrics() {
        if (!mainSceneController.opendPlaylist.isEmpty()) {
            addLyrics.setDisable(true);
            File selectedFile = fileChooser.showOpenDialog(mainStage);
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase(Locale.ROOT);
                if (fileName.endsWith(".lrc")) {
                    String lyricsFolder = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "Lyrics";

                    Path targetFolderPath = Paths.get(lyricsFolder); // getting the path of lyrics folder
                    Path targetPath = targetFolderPath.resolve(selectedFile.getName()); // creating the full path for copied lyrics
                    Path sourcePath = selectedFile.toPath(); // getting the path of the selected

                    String finalPath = targetPath.toString();
                    File file = new File(finalPath);
                    if (!file.exists()) {
                        try {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING); // copying the lyrics into app's data
                            lyricsInStored.put(mainSceneController.filePath, finalPath);
                            storeLyricsIntoDatabase(mainSceneController.filePath, finalPath);
                            loadLyrics(finalPath);
                            addLyricsLayout.setVisible(false);
                            middleLyrics.setVisible(true);
                            lyricsLoaded = true;

                            change.setDisable(false);
                            change.setVisible(true);
                            remove.setDisable(false);
                            remove.setVisible(true);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } // if ends
                } // inner if 2 ended
            } // inner if 1 ended
            addLyrics.setDisable(false);
        } else {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(mainStage);
            alert.setTitle("Add Lyrics");

            ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
            errorIcon.setFitHeight(54);
            errorIcon.setFitWidth(54);
            HBox hBox = new HBox(15);
            hBox.setPrefSize(390,85);
            hBox.setPadding(new Insets(20, 0,0,20));

            VBox vBox = new VBox(1);
            Label title = new Label("Cannot add lyrics!");
            title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
            Label content = new Label("Adding lyrics requires a song selected within a loaded playlist context.");
            content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
            content.setWrapText(true);

            vBox.getChildren().addAll(title, content);

            hBox.getChildren().addAll(errorIcon, vBox);
            alert.getDialogPane().setContent(hBox);
            alert.setHeaderText(null);

            ButtonType ok = new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().add(ok);

            alert.showAndWait();
        }
    } // method ends

    public void storeLyricsIntoDatabase(String songPath, String lyricsPath) {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "INSERT INTO Lyrics (song, lyrics)" +
                    "VALUES (?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, songPath);
            preparedStatement.setString(2, lyricsPath);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } // method ended

    public void changeTheLyricsFile() {
        change.setDisable(true);
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            String filePath = selectedFile.toString();
            if (lyricsInStored.containsKey(mainSceneController.filePath)) {
                File storedFile = new File(lyricsInStored.get(mainSceneController.filePath));
                if (storedFile.getName().equals(selectedFile.getName())) {
                    Alert alert = new Alert(Alert.AlertType.NONE);
                    alert.initOwner(mainStage);
                    alert.setTitle("Lyrics File");

                    ImageView errorIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/notification.png"))));
                    errorIcon.setFitHeight(54);
                    errorIcon.setFitWidth(54);
                    HBox hBox = new HBox(15);
                    hBox.setPrefSize(395,85);
                    hBox.setPadding(new Insets(20, 0,0,20));

                    VBox vBox = new VBox(1);
                    Label title = new Label("Duplicate file detected!");
                    title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
                    Label content = new Label("This file appears to be the same as the" +
                            " currently linked. Please unlink the current" +
                            " file to replace it.");
                    content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                    content.setWrapText(true);
                    vBox.getChildren().addAll(title, content);
                    HBox.setMargin(errorIcon, new Insets(10, 0, 0, 0));
                    hBox.getChildren().addAll(errorIcon, vBox);
                    alert.getDialogPane().setContent(hBox);
                    alert.setHeaderText(null);

                    ButtonType ok = new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().add(ok);
                    alert.showAndWait();
                    change.setDisable(false);
                    return;
                } else {
                    String lyricsFolder = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "Lyrics";

                    Path targetFolderPath = Paths.get(lyricsFolder); // getting the path of lyrics folder
                    Path targetPath = targetFolderPath.resolve(selectedFile.getName()); // creating the full path for copied lyrics
                    Path sourcePath = selectedFile.toPath(); // getting the path of the selected

                    String finalPath = targetPath.toString();
                    File file = new File(finalPath);
                    if (!file.exists()) {
                        try {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            lyricsInStored.put(mainSceneController.filePath, finalPath);

                            Files.delete(storedFile.toPath());

                            updateLyricsTable(finalPath);
                            loadLyrics(finalPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } // else ends
            } // if ended
        } // end
        change.setDisable(false);
    } // method ends

    public void updateLyricsTable(String newFilePath) {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "UPDATE Lyrics SET lyrics = ? " +
                    "WHERE song = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1 , newFilePath);
            preparedStatement.setString(2, mainSceneController.filePath);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } // method ends

    public void removeTheLyricsFile() {
        boolean confirmation = getConfirmation(mainStage);

        if (confirmation) {
            deleteLyrics(mainSceneController.filePath);
        }

    } // method ends

    public static boolean getConfirmation(Stage stage) {
        Alert alertExit = new Alert(Alert.AlertType.NONE);
        ImageView exitIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/question.png"))));
        exitIcon.setFitWidth(54);
        exitIcon.setFitHeight(54);
        alertExit.setTitle("Unlink Lyrics");
        alertExit.setHeaderText(null);
        HBox hBox = new HBox(15);
        VBox vBox = new VBox(1);
        Label title = new Label("Remove lyrics file from this song?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("Unlinking will permanently delete the lyrics associated with this track.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        content.setWrapText(true);
        vBox.getChildren().addAll(title, content);

        HBox.setMargin(exitIcon, new Insets(10, 0, 0, 0));
        hBox.getChildren().addAll(exitIcon, vBox);
        hBox.setPrefSize(430,70);
        alertExit.getDialogPane().setContent(hBox);

        ButtonType exitButton = new ButtonType("Yes, unlink", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("No, keep", ButtonBar.ButtonData.CANCEL_CLOSE);
        alertExit.getButtonTypes().setAll(exitButton, cancelButton);
        alertExit.initOwner(stage);
        return alertExit.showAndWait().orElse(cancelButton) == exitButton;
    } // method ends

    public void deleteLyrics(String file) {
        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "DELETE FROM Lyrics WHERE song = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, file);
            preparedStatement.executeUpdate();

            String currentLyrics = lyricsInStored.get(file);
            File deletedLyrics = new File(currentLyrics);
            Files.delete(deletedLyrics.toPath());

            // setting everything to default
            lyricsLoaded = false;
            addLyricsLayout.setVisible(true);
            middleLyrics.setVisible(false);

            change.setDisable(true);
            change.setVisible(false);
            remove.setDisable(true);
            remove.setVisible(false);

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    } // method ends

} // class ends
