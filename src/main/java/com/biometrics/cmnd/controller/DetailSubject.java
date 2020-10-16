package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.subject.entity.Subject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@FxmlView("DetailSubject.fxml")
public class DetailSubject {
    @FXML
    private Label id;

    @FXML
    private Button back;

    public void setSubject(Subject subject){
        id.setText(String.valueOf(subject.getId()));
    }

    public void back(ActionEvent event) throws IOException {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("AllSubject.fxml"));
        Parent allSubject = loader.load();
        Scene scene = new Scene(allSubject);
        AllSubject subject = loader.getController();
        subject.listSubject();
        stage.setScene(scene);
    }
}
