module com.example.amplify {
    requires javafx.fxml;
    requires javafx.web;
    requires com.jfoenix;
    requires javafx.media;
    requires jaudiotagger;
    requires java.desktop;
    requires java.sql;

    opens com.example.amplify to javafx.fxml, com.jfoenix;
    exports com.example.amplify;
}