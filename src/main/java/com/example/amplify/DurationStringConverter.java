package com.example.amplify;

import javafx.util.StringConverter;

public class DurationStringConverter extends StringConverter<Double> {


    @Override
    public String toString(Double aDouble) { // champ method
        // this method takes slider's current value and return it into MM:SS format
        if (aDouble == null) {
            return "00:00";
        }
        int totalSconds = aDouble.intValue(); // converting double value to int to calculate MM:SS
        int minute = totalSconds / 60;
        int seconds = totalSconds % 60;
        return String.format("%02d:%02d",minute,seconds); // sending slider's value in MM:SS to be displayed in bubble
    } // champ method ends here

    @Override
    public Double fromString(String s) {
        return 0.0;
    }
}
