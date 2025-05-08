package com.example.amplify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DeleteSongs extends Thread {
    String url = "jdbc:sqlite:" + System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appdata.db";

    public void run() {
        try (Connection connection = DriverManager.getConnection(url)){
           String sql1 = "SELECT file_paths FROM deleted_songs;";
           String sql2 = "DELETE FROM deleted_songs;";
           Statement statement1 = connection.createStatement();
           ResultSet resultSet = statement1.executeQuery(sql1);

            while (resultSet.next()) {
                URI uri = new URI(resultSet.getString("file_paths")); // converting filepath to URI
                File file = new File(uri); // converting URI to file for checking if file exists
                if (file.exists()) {
                    Path path = Paths.get(uri);
                    Files.delete(path);
                    System.out.println(path);
                }
            } // loop ends

            Statement statement2 = connection.createStatement();
            statement2.execute(sql2); // clearing the table after all songs are deleted

        } catch (SQLException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
} // class ends
