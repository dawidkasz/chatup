package com.chatup.chatup_client.controller;

import com.chatup.chatup_client.web.AuthClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable {
    private static LoginViewController INSTANCE;
    @FXML
    public Text invalidCredentialsText;

    @FXML
    public Button loginButton;

    @FXML
    public TextField usernameField;

    @FXML
    public TextField passwordField;

    public LoginViewController() {
        INSTANCE = this;
    }

    public LoginViewController getInstance() {
        return INSTANCE;
    }

    @FXML
    public void onLoginButtonClicked(ActionEvent e) throws IOException {
        if(AuthClient.login(usernameField.getText().trim(), passwordField.getText().trim())) {
            switchToChatView(e, AuthClient.getToken());
        }
        else {
            invalidCredentialsText.setText("Invalid Credentials");
            usernameField.clear();
            passwordField.clear();
        }

    }
    void switchToChatView(ActionEvent e, String token) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatup-view.fxml"));
        loader.setController(new ChatViewController(token));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Roboto+Slab");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
