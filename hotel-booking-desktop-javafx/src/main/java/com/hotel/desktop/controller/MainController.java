package com.hotel.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the initial welcome view.
 */
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label javaVersionLabel;

    @FXML
    private Label javafxVersionLabel;

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Status: JavaFX Desktop Client Running • Ready to connect to backend on port 5001");
        }
        if (javaVersionLabel != null) {
            javaVersionLabel.setText("Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        }
        if (javafxVersionLabel != null) {
            javafxVersionLabel.setText("JavaFX: " + System.getProperty("javafx.version", "21.0.2"));
        }
    }
}

