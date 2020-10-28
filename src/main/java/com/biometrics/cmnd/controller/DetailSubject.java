package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.nxView.FaceViewLittle;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.neurotec.biometrics.client.NBiometricClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Component
@FxmlView("DetailSubject.fxml")
public class DetailSubject implements Initializable {
    @FXML
    private Label id;
    @FXML
    private TextField city, nid, firstName, lastName, country, district, street, zip, email, phone;
    @FXML
    private DatePicker birthday;
    @FXML
    private RadioButton male, female;
    @FXML
    private ImageView faceImage;
    @FXML
    private Button back;
    @FXML
    private StackPane facePane;
    private final FaceViewLittle viewFaces;
    private boolean control = false ;
    public DetailSubject() {
        super();
        this.viewFaces = new FaceViewLittle();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        birthday.setConverter(new StringConverter<LocalDate>() {

            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateTimeFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateTimeFormatter) : null;
            }
        });
        facePane.setAlignment(Pos.CENTER);
    }

    public void setSubject(SubjectDto.SubjectRes subject) throws IOException {
        id.setText(String.valueOf(subject.getSubjectId()));
        city.setText(subject.getContact().getAddress().getCity());
        nid.setText(subject.getBioGraphy().getNid());
        firstName.setText(subject.getBioGraphy().getFirstName());
        lastName.setText(subject.getBioGraphy().getLastName());
        country.setText(subject.getContact().getAddress().getCountry());
        district.setText(subject.getContact().getAddress().getDistrict());
        street.setText(subject.getContact().getAddress().getStreet());
        zip.setText(subject.getContact().getAddress().getZip());
        email.setText(subject.getContact().getEmail());
        phone.setText(subject.getContact().getPhoneNumber());
        birthday.setValue(subject.getBioGraphy().getBirthDate());

        switch (subject.getBioGraphy().getGender()) {
            case MALE:
                male.setSelected(true);
                break;
            case FEMALE:
                female.setSelected(true);
                break;
            default:
                male.setSelected(false);
                female.setSelected(false);
                break;
        }
        for (int i = 0; i < subject.getSubjectImages().size(); i++) {
            if (subject.getSubjectImages().get(i).getImageInfo().getBioType().equals(BioType.FACE)) {
                File file = new File("uploads" + subject.getSubjectImages().get(i).getImageInfo().getImageUrl());
                Image image = new Image(file.toURI().toURL().toExternalForm(), 180, 250, false, true);
                faceImage = new ImageView(image);
                facePane.getChildren().add(faceImage);
            }
        }
        controlSubject();
    }

    private void controlSubject() {
        city.setEditable(control);
        nid.setEditable(control);
        firstName.setEditable(control);
        lastName.setEditable(control);
        country.setEditable(control);
        district.setEditable(control);
        street.setEditable(control);
        zip.setEditable(control);
        email.setEditable(control);
        phone.setEditable(control);
        birthday.setEditable(control);
        male.setDisable(!control);
        female.setDisable(!control);
    }

    @FXML
    private void edit(ActionEvent event) {
        control = true;
        controlSubject();
    }

    @FXML
    private void save(ActionEvent event) {
    }
}
