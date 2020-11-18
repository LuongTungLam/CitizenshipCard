package com.biometrics.cmnd.common.validation;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.NumberValidator;
import com.jfoenix.validation.RegexValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.*;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Random;


public class Validation {

    public void isNumber(TextField input) {
        ValidationSupport support = new ValidationSupport();
        int number = Integer.parseInt(input.getText());
        support.registerValidator(input, Validator.createEmptyValidator("Not Number"));
    }

    public String isNull(TextField input) {
        ContextMenu validator = new ContextMenu();
        validator.getStyleClass().add("css/validStyle.css");
        validator.setAutoHide(true);
        if (input.getText().equals("")) {
            input.setStyle("-fx-border-color: red");
            validator.getItems().add(new MenuItem("Text is required"));
            validator.show(input, Side.TOP, 10, 0);
        }
        input.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> input0, Boolean inputOld, Boolean inputNew) {
                if (inputNew) {
                    input.setStyle(null);
                    validator.hide();
                }
            }
        });

        return input.getText();
    }

    public void isNullFx(JFXTextField input) {
        RequiredFieldValidator validator = new RequiredFieldValidator();
        input.getValidators().add(validator);
        validator.setMessage("Text is required");
        validator.setIcon(new FontIcon("fas-question-circle"));
        input.setStyle("-fx-font-size: 11");
        input.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> input0, Boolean inputOld, Boolean inputNew) {
                if (!inputNew) {
                    input.validate();
                }
            }
        });
    }

    public void isNumber(JFXTextField input) {
        NumberValidator numberValidator = new NumberValidator();
        input.getValidators().add(numberValidator);
        numberValidator.setMessage("Must be number");
        numberValidator.setIcon(new FontIcon("fas-question-circle"));
        input.setStyle("-fx-font-size: 11");
        input.focusedProperty().addListener((input0, inputOld, inputNew) -> {
            if (!inputNew) input.validate();
        });
    }

    public void isEmail(JFXTextField input) {
        RegexValidator validator = new RegexValidator();
        validator.setRegexPattern("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-z0-9]+([.][a-zA-Z]+)+");
        input.getValidators().add(validator);
        validator.setMessage("False format email");
        validator.setIcon(new FontIcon("fas-question-circle"));
        input.focusedProperty().addListener((input0, inputOld, inputNew) -> {
            if (!inputNew) input.validate();
        });
    }

    public void isPhone(JFXTextField input) {
        RegexValidator validator = new RegexValidator();
        validator.setRegexPattern("^0[0-9]{9}$");
        input.getValidators().add(validator);
        validator.setMessage("False format phone");
        validator.setIcon(new FontIcon("fas-question-circle"));
        input.focusedProperty().addListener((input0, inputOld, inputNew) -> {
            if (!inputNew) input.validate();
        });
    }

    public void numberCard(JFXTextField input) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1);

        // rest of 11 digits
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }
        input.setText(String.valueOf(sb));
    }

    public void cbbNull(JFXComboBox comboBox){
        RequiredFieldValidator validator = new RequiredFieldValidator();
        comboBox.getValidators().add(validator);
        validator.setMessage("Select Item");
        validator.setIcon(new FontIcon("fas-question-circle"));
        comboBox.focusedProperty().addListener((input0, inputOld, inputNew) -> {
            if (!inputNew) comboBox.validate();
        });
    }

    public void dpkNull(JFXDatePicker picker){
        RequiredFieldValidator validator = new RequiredFieldValidator();
        picker.getValidators().add(validator);
        validator.setMessage("Select Date");
        validator.setIcon(new FontIcon("fas-question-circle"));
        picker.focusedProperty().addListener((input0, inputOld, inputNew) -> {
            if (!inputNew) picker.validate();
        });
    }
}
