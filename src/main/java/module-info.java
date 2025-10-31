module com.example.amplify {
    requires javafx.fxml;
    requires javafx.web;
    requires com.jfoenix;
    requires javafx.media;
    requires jaudiotagger;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.feather;
    requires org.kordamp.ikonli.bootstrapicons;

    opens com.example.amplify to javafx.fxml, com.jfoenix;
    opens com.example.setting to com.google.gson;
    exports com.example.amplify;
}