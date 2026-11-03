package com.hotel.desktop.controller;

import com.hotel.desktop.model.MessageResponse;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.util.ViewNavigator;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button loginLinkButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private VBox successContainer;
    @FXML private Label successLabel;

    private final AuthService authService;

    public RegisterController() {
        this.authService = new AuthService();
    }

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        hideAlerts();
    }

    @FXML
    public void handleRegister() {
        hideAlerts();

        String firstName = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField.getText() != null ? lastNameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (firstName.isEmpty()) {
            showError("Please enter your first name.");
            firstNameField.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            showError("Please enter your last name.");
            lastNameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (password == null || password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match. Please verify both password fields.");
            confirmPasswordField.requestFocus();
            return;
        }

        setLoading(true);

        Task<MessageResponse> registerTask = new Task<>() {
            @Override
            protected MessageResponse call() throws Exception {
                // Strict rule: Registration always assigns standard USER role on the backend
                return authService.register(firstName, lastName, email, password);
            }
        };

        registerTask.setOnSucceeded(event -> {
            setLoading(false);
            showSuccess("Registration successful! Redirecting to sign in...");
            // Switch to login view after short delay or direct navigation
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    ViewNavigator.showLoginView();
                });
            }).start();
        });

        registerTask.setOnFailed(event -> {
            setLoading(false);
            Throwable ex = registerTask.getException();
            String msg = ex instanceof ApiException ? ex.getMessage() : "Registration failed. Please check your details and try again.";
            showError(msg);
        });

        Thread thread = new Thread(registerTask, "register-task-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleNavigateLogin() {
        ViewNavigator.showLoginView();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        registerButton.setDisable(loading);
        firstNameField.setDisable(loading);
        lastNameField.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        if (loading) {
            registerButton.setText("Creating Account...");
        } else {
            registerButton.setText("Create Guest Account");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        successContainer.setVisible(false);
        successContainer.setManaged(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successContainer.setVisible(true);
        successContainer.setManaged(true);
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    private void hideAlerts() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
        successContainer.setVisible(false);
        successContainer.setManaged(false);
    }
}

