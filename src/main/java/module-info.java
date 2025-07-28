module com.example.amplify {
    requires javafx.fxml;
    requires javafx.web;
    requires com.jfoenix;
    requires javafx.media;
    requires jaudiotagger;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;

    opens com.example.amplify to javafx.fxml, com.jfoenix;
    opens com.example.setting to com.google.gson;
    exports com.example.amplify;
}