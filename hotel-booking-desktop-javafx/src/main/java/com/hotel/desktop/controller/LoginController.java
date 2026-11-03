package com.hotel.desktop.controller;

import com.hotel.desktop.model.User;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordButton;
    @FXML private Button loginButton;
    @FXML private Button registerLinkButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private VBox successContainer;
    @FXML private Label successLabel;

    private boolean isPasswordVisible = false;
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        // Keep PasswordField and TextField in sync
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPasswordVisible) {
                passwordTextField.setText(newVal);
            }
        });

        passwordTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) {
                passwordField.setText(newVal);
            }
        });

        hideAlerts();
    }

    @FXML
    public void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("Hide");
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("Show");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    public void handleLogin() {
        hideAlerts();

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (password == null || password.isEmpty()) {
            showError("Please enter your password.");
            if (isPasswordVisible) passwordTextField.requestFocus();
            else passwordField.requestFocus();
            return;
        }

        setLoading(true);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            setLoading(false);
            User user = loginTask.getValue();
            ViewNavigator.showMainLayout();
        });

        loginTask.setOnFailed(event -> {
            setLoading(false);
            Throwable ex = loginTask.getException();
            String msg = ex instanceof ApiException ? ex.getMessage() : "Unable to sign in. Please verify your credentials or server status.";
            showError(msg);
        });

        Thread thread = new Thread(loginTask, "login-task-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleNavigateRegister() {
        ViewNavigator.showRegisterView();
    }

    @FXML
    public void handleFillGuest() {
        fillCredentials("guest@hotel.com", "12345678");
    }

    @FXML
    public void handleFillOwner() {
        fillCredentials("owner@hotel.com", "12345678");
    }

    @FXML
    public void handleFillAdmin() {
        fillCredentials("admin@hotel.com", "12345678");
    }

    private void fillCredentials(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
        passwordTextField.setText(password);
        hideAlerts();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        passwordTextField.setDisable(loading);
        togglePasswordButton.setDisable(loading);
        if (loading) {
            loginButton.setText("Authenticating...");
        } else {
            loginButton.setText("Sign In");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        successContainer.setVisible(false);
        successContainer.setManaged(false);
    }

    public void showSuccess(String message) {
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

