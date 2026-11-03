package com.hotel.desktop;

/**
 * Non-modular entry point for JavaFX application execution.
 * Avoids JavaFX runtime module path restrictions when launched directly or packaged as a JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        if ("screenshots".equalsIgnoreCase(System.getProperty("app.mode"))
                || (args != null && args.length > 0 && "screenshots".equalsIgnoreCase(args[0]))) {
            com.hotel.desktop.visual.VisualScreenshotGenerator.main(args);
        } else {
            MainApp.main(args);
        }
    }
}

