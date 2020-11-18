package com.biometrics.cmnd.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@FxmlView("LoginBiometrics.fxml")
public class LoginBiometrics implements Initializable {
    @FXML
    private ImageView logo;

    @FXML
    private Circle btnClose;

    @FXML
    private Button btnScan, btnLogin;

    private final FxWeaver fxWeaver;

    public LoginBiometrics(FxWeaver fxWeaver){
        super();
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource() == btnLogin){
            homePage();
        }
    }



    @FXML
    private void handleMouseEvent(MouseEvent event) {
        if (event.getSource() == btnClose) {
            System.exit(0);
        }
    }

    private void homePage() {
        fxWeaver.loadController(HomeBiometrics.class).show();
        close();
    }

    private void close() {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.close();
    }
}
