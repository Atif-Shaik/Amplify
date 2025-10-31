package com.example.amplify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;

import com.example.setting.Settings;
import com.example.setting.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Objects;

public class Main extends Application {
    static String url;
    MainSceneController mainSceneController;

    @Override
    public void start(Stage stage) throws Exception {
        // FXML load section
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/example/amplify/MainScene.fxml"));
        Parent root = loader.load();
        Scene mainScene = new Scene(root, 500,600);
        root.requestFocus(); // requesting focus for key

        // loading the controller class
        mainSceneController = loader.getController();
        // sending stage and scene reference to controller class
        mainSceneController.setStage(stage);
        mainSceneController.setScene(mainScene);

        // stage setup
        stage.setScene(mainScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo.png"))));
        stage.setTitle("AmplifyMax");
        stage.show();

        DeleteSongs deleteSongs = new DeleteSongs(); // creating thread object
        deleteSongs.start(); // starting the thread to deleted song from Music folder

        createDatabaseIfNotExists(stage); // creating sqlite database
        Settings settings = SettingsManager.loadSettings(); // creating setting object with SettingManager's loadSettings() method

        if (settings.getTheme().equals("Light Theme")) {
            mainScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/MainScene-light-theme.css")));
        } else if (settings.getTheme().equals("Dark Theme")) {
            mainScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/MainScene-dark-theme.css")));
        }

        mainSceneController.setSettingObject(settings); // sending setting object to MainSceneController for further action if needed
        mainSceneController.loadIcons(settings.getTheme());

        addLikedlistToPlaylistTable(); // adding liked_songs table name to all_playlists names table for the first time (only to crate default liked_song table).

        // adding closing confirmation
        stage.setOnCloseRequest(event -> {
            boolean userConfirmation = getConfirmation(stage);
            if (userConfirmation) {
                // code for future
            } else {
                event.consume();
            }
        }); // end

        // Key listener for play pause
        mainScene.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.P) {
                mainSceneController.playAndPauseController();
            }
        }); // end

        mainScene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                mainSceneController.backwardButton();
            }
            if (event.getCode() == KeyCode.RIGHT) {
                mainSceneController.forwardButton();
            }
        });

    } // start method ends

    // method for alert confirmation dialog
    public static boolean getConfirmation(Stage stage) {
        Alert alertExit = new Alert(Alert.AlertType.NONE);
        ImageView exitIcon = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/question.png"))));
        exitIcon.setFitWidth(54);
        exitIcon.setFitHeight(54);
        alertExit.setTitle("Exit App");
        alertExit.setHeaderText(null);
        HBox hBox = new HBox(15);
        hBox.setPadding(new Insets(20, 0,0,20));
        VBox vBox = new VBox(1);
        Label title = new Label("Are you sure you want to exit?");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: blue; -fx-font-weight: bold");
        Label content = new Label("This will close the application.");
        content.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
        vBox.getChildren().addAll(title, content);

        hBox.getChildren().addAll(exitIcon, vBox);
        hBox.setPrefSize(390,70);
        alertExit.getDialogPane().setContent(hBox);
        
        ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alertExit.getButtonTypes().setAll(exitButton, cancelButton);
        alertExit.initOwner(stage);
        return alertExit.showAndWait().orElse(cancelButton) == exitButton;
    } // method ends

    public static void createDatabaseIfNotExists(Stage stage) {
        String userDir = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic";

        String basePath = System.getenv("LOCALAPPDATA");
        File amplifyMusicdir = new File(basePath, "AmplifyMusic");

        if (!amplifyMusicdir.exists()) {
            boolean created = amplifyMusicdir.mkdirs();
            String amplifyMusicdirPath = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic";
            File musicdir = new File(amplifyMusicdirPath, "Music");
            if (!musicdir.exists()) {
                boolean created1 = musicdir.mkdirs();
            }
        } // if ends

        String dbPath = userDir + File.separator + "appdata.db";
        url = "jdbc:sqlite:" + dbPath; // full path of writable database

        try (Connection connection = DriverManager.getConnection(url)){
            // crating a table for all_playlist names
            String sql1 = "CREATE TABLE IF NOT EXISTS all_playlists (" +
                    "playlists TEXT NOT NULL UNIQUE);";
            // creating liked song table
            String sql2 = "CREATE TABLE IF NOT EXISTS liked_songs (" +
                    "file_paths TEXT NOT NULL UNIQUE);";
            // creating table for deleted songs
            String sql3 = "CREATE TABLE IF NOT EXISTS deleted_songs (" +
                    "file_paths TEXT NOT NULL UNIQUE);";
            // creating table for counting song used in playlist for deleting
            // old Table name -> songs columns list, count
            String sql4 = "CREATE TABLE IF NOT EXISTS songsInAppData (" +
                    "songPath TEXT NOT NULL UNIQUE," +
                    "songCount INTEGER);";

            Statement statement = connection.createStatement();
            statement.execute(sql1);
            statement.execute(sql2);
            statement.execute(sql3);
            statement.execute(sql4);
        } catch (SQLException e) {
            System.out.println(url);
            System.out.println("Failed to connect with database");
            throw new RuntimeException(e);
        } // catch
    } // method ends

    public static void addLikedlistToPlaylistTable() {
        String sql1 = "INSERT INTO all_playlists (playlists)" +
                "VALUES (?);";
        try (Connection connection = DriverManager.getConnection(url)){
            PreparedStatement preparedStatement = connection.prepareStatement(sql1);
            preparedStatement.setString(1, "liked_songs");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // if liked_songs table is already created, INSERT won't work and catch block will execute.
            // if this block should be empty to prevent crash
        }
    } // method ends

    @Override
    public void stop() { // this run when the app closes
        if (mainSceneController.scheduler != null) { // this if is important to release scheduler resources
            mainSceneController.sleepTask.cancel(false);
            mainSceneController.scheduler.shutdownNow();
        }
    } // method ends

    public static void main(String[] args) {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) { // getting log property to turn off jaudiotagger's log
            LogManager.getLogManager().readConfiguration(is); // setting log property
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Application.launch(Main.class, args); // launching app
    } // method ends
} // class ends here
