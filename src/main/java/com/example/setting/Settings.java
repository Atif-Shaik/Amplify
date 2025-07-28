package com.example.setting;

public class Settings {
    private String theme;
    private String miniAnchorTheme;
    private String miniAnchorBorder;
    private String popupTheme;
    private String comboboxTheme;

    // default constructor
    public Settings() {}

    public Settings(String theme, String miniAnchorTheme, String miniAnchorBorder, String popupTheme, String comboboxTheme) {
        this.theme = theme;
        this.miniAnchorTheme = miniAnchorTheme;
        this.miniAnchorBorder = miniAnchorBorder;
        this.popupTheme = popupTheme;
        this.comboboxTheme = comboboxTheme;
    } // end

    // getters & setters
    public String getTheme() {
        return theme;
    } // end

    public String getMiniAnchorTheme() {
        return miniAnchorTheme;
    } // end

    public String getMiniAnchorBorderTheme() {
        return miniAnchorBorder;
    } // end

    public String getPopupTheme() { return popupTheme; }

    public String getComboboxTheme() { return comboboxTheme; }

    public void setTheme(String theme) {
        this.theme = theme;
    } // end

} // class ends
