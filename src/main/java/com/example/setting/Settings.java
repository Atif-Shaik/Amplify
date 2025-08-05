package com.example.setting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Settings {
    private String theme;

    // default constructor
    public Settings() {}

    public Settings(String theme) {
        this.theme = theme;
    } // end

    // getters & setters
    public String getTheme() {
        return theme;
    } // end

    public void setTheme(String theme) {
        this.theme = theme;
    } // end

    public void saveSettings() {
        String settingFilePath = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appSettings.json";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(settingFilePath)){
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } // method ends

} // class ends
