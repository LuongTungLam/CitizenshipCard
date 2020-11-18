package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.validation.Validation;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.jfoenix.controls.JFXTextField;
import com.neurotec.biometrics.client.NBiometricClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

@Component
@FxmlView("CreateSubject.fxml")
public class CreateSubject implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    @FXML
    private JFXTextField numberCard, email, phone, firstName, lastName, city, province, district, country, street, zip;
    @FXML
    private DatePicker birthday;
    @FXML
    private Button btnNext, btnNextStep, btnFile, btnScan, btnHelp, btnBack;
    @FXML
    private Pane frontPane, backPane;
    private final NBiometricClient client;
    private final Validation validation;

    public CreateSubject(SubjectService subjectService) {
        super();
        this.client = new NBiometricClient();
        this.subjectService = subjectService;
        this.validation = new Validation();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isNulls();
        isNumbers();
        isEmails();
        isPhones();
        numberCard.setDisable(true);
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource().equals(btnNext)) {
            if (email.getText().isEmpty() || phone.getText().isEmpty() || firstName.getText().isEmpty() || lastName.getText().isEmpty() || city.getText().isEmpty() || province.getText().isEmpty() || district.getText().isEmpty() || country.getText().isEmpty() || street.getText().isEmpty() || zip.getText().isEmpty() || birthday.getEditor().getText().isEmpty()) {
                return;
            } else {
                frontPane.toFront();
            }
        }
        if (event.getSource().equals(btnBack)) {
            backPane.toFront();
        }
    }

    @FXML
    private void handleMouseEvent(MouseEvent event) {

    }

    private void isNulls() {
        validation.isNullFx(numberCard);
        validation.isNullFx(email);
        validation.isNullFx(phone);
        validation.isNullFx(firstName);
        validation.isNullFx(lastName);
        validation.isNullFx(city);
        validation.isNullFx(province);
        validation.isNullFx(district);
        validation.isNullFx(country);
        validation.isNullFx(street);
        validation.isNullFx(zip);
    }

    private void isNumbers() {
        validation.isNumber(zip);
    }

    private void isEmails() {
        validation.isEmail(email);
    }

    private void isPhones() {
        validation.isPhone(phone);
    }

    private void numberCard() {
        validation.numberCard(numberCard);
    }
}
