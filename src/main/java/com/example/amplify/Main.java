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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Objects;

public class Main extends Application {
    static String url;

    @Override
    public void start(Stage stage) throws Exception {
        // FXML load section
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/example/amplify/MainScene.fxml"));
        Parent root = loader.load();
        Scene mainScene = new Scene(root, 500,600);
        root.requestFocus(); // requesting focus for key events
        mainScene.getStylesheets().add(String.valueOf(getClass().getResource("/com/example/amplify/mainScene.css")));
        // loading the controller class
        MainSceneController mainSceneController = loader.getController();
        // sending stage and scene reference to controller class
        mainSceneController.setStage(stage);
        mainSceneController.setScene(mainScene);

        // stage setup
        stage.setScene(mainScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo.png"))));
        stage.setTitle("AmplifyMax");
        stage.show();

        createDatabaseIfNotExists(stage); // creating sqlite database

        DeleteSongs deleteSongs = new DeleteSongs(); // creating thread object
        deleteSongs.start(); // starting the thread to deleted song from Music folder

        addLikedlistToPlaylistTable(); // adding lined songs table name to all playlist names table

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
        Alert alertExit = new Alert(Alert.AlertType.CONFIRMATION);
        alertExit.setTitle("Exit App");
        alertExit.setHeaderText("Are you sure?");
        alertExit.setContentText("Please confirm your action.");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alertExit.getButtonTypes().setAll(yesButton, noButton);
        alertExit.initOwner(stage);
        return alertExit.showAndWait().orElse(noButton) == yesButton;
    } // method ends

    public static void createDatabaseIfNotExists(Stage stage) {
        String userDir = System.getProperty("user.dir") + File.separator + "AmplifyMusic";

        String basePath = System.getProperty("user.dir");
        File amplifyMusicdir = new File(basePath, "AmplifyMusic");

        if (!amplifyMusicdir.exists()) {
            boolean created = amplifyMusicdir.mkdirs();
            String amplifyMusicdirPath = System.getProperty("user.dir") + File.separator + "AmplifyMusic";
            File musicdir = new File(amplifyMusicdirPath, "Music");
            if (!musicdir.exists()) {
                boolean created1 = musicdir.mkdirs();
            }
        } // if ends

        String dbPath = userDir + File.separator + "appdata.db";
        url = "jdbc:sqlite:" + dbPath; // full path of writable database

        try (Connection connection = DriverManager.getConnection(url)){
            // crating a table for all playlist names
            String sql1 = "CREATE TABLE IF NOT EXISTS all_playlists (" +
                    "playlists TEXT NOT NULL UNIQUE);";
            // creating liked song table
            String sql2 = "CREATE TABLE IF NOT EXISTS liked_songs (" +
                    "file_paths TEXT NOT NULL UNIQUE);";
            // creating table for deleted songs
            String sql3 = "CREATE TABLE IF NOT EXISTS deleted_songs (" +
                    "file_paths TEXT NOT NULL UNIQUE);";
            // creating table for counting song used in playlist for deleting
            String sql4 = "CREATE TABLE IF NOT EXISTS songs (" +
                    "list TEXT NOT NULL," +
                    "count INTEGER);";

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
