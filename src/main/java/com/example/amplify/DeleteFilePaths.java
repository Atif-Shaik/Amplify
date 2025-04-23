package com.example.amplify;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class DeleteFilePaths extends Thread{
    ArrayList<String> arrayList;
    String url = "jdbc:sqlite:" + System.getProperty("user.home") + File.separator + ".amplifydata" + File.separator + "appdata.db";
    String playlist;

    public DeleteFilePaths(ArrayList<String> arrayList, String playlist) {
        this.arrayList = new ArrayList<>(arrayList);
        this.playlist = playlist;
    }

    public void run() {

        try (Connection connection = DriverManager.getConnection(url)){
            String sql = "DELETE FROM " + playlist + " WHERE file_paths = (?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (String str : this.arrayList) {
                preparedStatement.setString(1, str);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    } // run ends
} // class ends
