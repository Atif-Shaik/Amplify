package com.example.setting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SettingsManager {
    private static final String settingFilePath = System.getenv("LOCALAPPDATA") + File.separator + "AmplifyMusic" + File.separator + "appSettings.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Settings loadSettings() {
        try {
            if (Files.exists(Paths.get(settingFilePath))) { // this reads the settings from json
                Reader reader = new FileReader(settingFilePath);
                Settings settings = gson.fromJson(reader, Settings.class);
                reader.close();
                return settings;
            } else {
                Settings defaultSetting = new Settings("Light Theme"); // setting default theme
                saveSettings(defaultSetting);
                return defaultSetting;
            }
        } catch (IOException e) {
            return new Settings();
        }
    } // method ends

    // this saves the settings in json file of not exist in default form
    public static void saveSettings(Settings settings) {
        try (Writer writer = new FileWriter(settingFilePath)){
            gson.toJson(settings, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } // method ends

} // class ends
