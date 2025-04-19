module com.example.amplify {
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.jfoenix;
    requires javafx.media;
    requires jaudiotagger;
    requires java.desktop;
    requires java.sql;

    opens com.example.amplify to javafx.fxml, com.jfoenix;
    exports com.example.amplify;
}