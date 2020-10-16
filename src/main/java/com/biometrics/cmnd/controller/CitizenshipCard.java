package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.*;
import com.biometrics.cmnd.common.model.*;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.kbjung.abis.neurotec.biometrics.fx.FingerViewNode;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@FxmlView("CitizenshipCard.fxml")
public class CitizenshipCard implements Initializable {

    @FXML
    private TableView<Subject> tableView;
    @FXML
    private TextField nid, phone, email, firstname, lastname, district, city, country, province, street, zip, birthday;
    @FXML
    private RadioButton male, female;
    @FXML
    private Button chooseImage, save;
    @FXML
    private StackPane imageview, fingerView;

    private File fileImage;
    private File fileFinger;
    private FileChooser fc;
    private NSubject subject;

    @Autowired
    private final SubjectService subjectService;

    @Autowired
    private final RecognitionService recognitionService;


    private final NBiometricClient client;
    private final FaceViewNode faceViewNode;
    private final FingerViewNode fingerViewNode;

    private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();

    public CitizenshipCard(SubjectService subjectService, NBiometricClient client, RecognitionService recognitionService) {
        super();
        this.faceViewNode = new FaceViewNode();
        this.fingerViewNode = new FingerViewNode();
        this.subjectService = subjectService;
        this.client = client;
        this.recognitionService = recognitionService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.imageview.getChildren().add(faceViewNode);
        this.imageview.setAlignment(Pos.CENTER);
        this.fingerView.getChildren().add(fingerViewNode);
        this.fingerView.setAlignment(Pos.CENTER);
    }

    @FXML
    public void scan(ActionEvent event) throws IOException {
        fc = new FileChooser();
        fileFinger = fc.showOpenDialog(null);
        if (fileFinger != null) {
            NImage image = NImage.fromFile(fileFinger.getAbsolutePath());
            NSubject subject = new NSubject();
            NFinger finger = new NFinger();
            finger.setImage(image);
            subject.getFingers().add(finger);
            this.fingerViewNode.setFinger(finger);
            createTemplate();
        }
    }

    private void createTemplate() {
        subject = new NSubject();
        NFinger finger = new NFinger();
        finger.setImage(fingerViewNode.getFinger().getImage());
        subject.getFingers().add(finger);
        client.createTemplate(subject, null, templateCreationHandler);
    }

    @FXML
    public void chooseImage(ActionEvent event) throws IOException {
        fc = new FileChooser();
        fileImage = fc.showOpenDialog(null);
        if (fileImage != null) {
            NImage image = NImage.fromFile(fileImage.getAbsolutePath());
            NSubject subject = new NSubject();
            NFace face = new NFace();
            face.setImage(image);
            subject.getFaces().add(face);
            this.faceViewNode.setFace(face);
        }
    }

    @FXML
    public void save(ActionEvent event) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        BioGraphy bioGraphy = BioGraphy.builder()
                .firstName(getFirstName())
                .lastName(getLastName())
                .nid(getNid())
                .gender(Gender.valueOf(getGender()))
                .birthDate(LocalDate.parse(getBirthday()))
                .build();
        Address address = Address.builder()
                .street(getStreet())
                .city(getCity())
                .district(getDistrict())
                .province(getProvince())
                .country(getCountry())
                .zip(getZip())
                .build();
        Contact contact = Contact.builder()
                .address(address)
                .phoneNumber(getPhoneNumber())
                .email(getEmail())
                .build();


        // create list images convert to base64
        List<String> base64Image = new ArrayList<>();
        base64Image.add(NImageUtils.imageFileToBase64String(fileImage.getAbsolutePath()));
        base64Image.add(NImageUtils.imageFileToBase64String(fileFinger.getAbsolutePath()));

        // list images have bioType insert to SubjectDto
        List<Image> images = new ArrayList<>();
        Image faceImage = Image.builder()
                .quality(100)
                .bioType(BioType.FACE)
                .format(ImageFormat.JPG)
                .base64Image(base64Image.get(0))
                .enabled(true)
                .pose(Pose.FACE_FRONT)
                .build();

        Image fingerImage = Image.builder()
                .quality(100)
                .bioType(BioType.FINGER)
                .format(ImageFormat.JPG)
                .base64Image(base64Image.get(1))
                .enabled(true)
                .pose(Pose.FINGER_RIGHT_THUMB)
                .build();

        images.add(faceImage);
        images.add(fingerImage);

        SubjectDto.CreateReq dto = SubjectDto.CreateReq.builder()
                .bioGraphy(bioGraphy)
                .contact(contact)
                .images(images)
                .build();

        // create subject save to folder uploads and insert image to database
        NSubject fileSubject = new NSubject();
//        NSubject fileSubject = recognitionService.extractTemplate(base64Image.get(0), dto.getImages().get(0).getFormat());
//        NSubject fingerSubject = recognitionService.extractTemplate(base64Image.get(1), dto.getImages().get(1).getFormat());

        for (int i = 0; i < dto.getImages().size(); i++) {
            fileSubject = recognitionService.extractTemplate(images.get(i).getBase64Image(), dto.getImages().get(i).getFormat());

            String imagePath = generateFaceImagePath();
            String filePath = "./uploads/" + imagePath;
            Path path = Paths.get(filePath);
            if (Files.notExists(path)) {
                Files.createDirectories(Paths.get(filePath));
            }
            if (dto.getImages().get(i).getBioType().equals(BioType.FACE)) {
                String fileName = dto.getBioGraphy().getNid() + "_face." + dto.getImages().get(i).getFormat().name();

                switch (dto.getImages().get(i).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        fileSubject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        fileSubject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        fileSubject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        fileSubject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }
                int quality = fileSubject.getFaces().get(1).getObjects().get(i).getQuality();

                ImageInfo imageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(quality)
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();
                //                i need fix it
                byte[] template = fileSubject.getTemplateBuffer().toByteArray();
                Subject subject = subjectService.create(dto, imageInfo, template);
                saveAlert(subject);
            } else if (dto.getImages().get(i).getBioType().equals(BioType.FINGER)) {

                String fileName = dto.getBioGraphy().getNid() + "_finger." + dto.getImages().get(i).getFormat().name();

                switch (dto.getImages().get(i).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        fileSubject.getFingers().get(i).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        fileSubject.getFingers().get(i).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        fileSubject.getFingers().get(i).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        fileSubject.getFingers().get(i).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }

                int quality = fileSubject.getFingers().get(1).getObjects().get(i).getQuality();

                ImageInfo imageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(quality)
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();

//                i need fix it
                byte[] template = fileSubject.getTemplateBuffer().toByteArray();
                Subject subject = subjectService.create(dto, imageInfo, template);
                saveAlert(subject);
            }
//            byte[] template = fileSubject.getTemplateBuffer().toByteArray();
//            Subject subject = subjectService.create(dto, imageInfo, template);
//            saveAlert(subject);
        }
        clearFields();
    }

    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }

    private void saveAlert(Subject subject) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User saved successfully.");
        alert.setHeaderText(null);
        alert.setContentText("successfully");
        alert.showAndWait();
    }

    private void clearFields() {
        imageview.getChildren().clear();
        province.clear();
        email.clear();
        nid.clear();
        phone.clear();
        firstname.clear();
        lastname.clear();
        district.clear();
        city.clear();
        country.clear();
        zip.clear();
        street.clear();
        birthday.clear();
        male.setSelected(true);
        female.setSelected(false);
    }

    private String getGenderTitle(String gender) {
        return (gender.equals("MALE")) ? "his" : "her";
    }

    public String getGender() {
        return male.isSelected() ? "MALE" : "FEMALE";
    }

    public String getFirstName() {
        return firstname.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    public String getLastName() {
        return lastname.getText();
    }

    public String getDistrict() {
        return district.getText();
    }

    public String getCity() {
        return city.getText();
    }

    public String getCountry() {
        return country.getText();
    }

    public String getProvince() {
        return province.getText();
    }

    public String getStreet() {
        return street.getText();
    }

    public String getZip() {
        return zip.getText();
    }

    public String getBirthday() {
        return birthday.getText();
    }

    public String getNid() {
        return nid.getText();
    }

    public String getPhoneNumber() {
        return phone.getText();
    }

    private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, Object> {

        @Override
        public void completed(final NBiometricStatus result, final Object attachment) {
            Platform.runLater(() -> {
                if (result == NBiometricStatus.OK) {
                    updateTemplateCreationStatus(true);
                }
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {

        }
    }

    private void updateTemplateCreationStatus(boolean created) {
        if (created) {
            fingerViewNode.setFinger(subject.getFingers().get(0));
        } else {
            fingerViewNode.setFinger(null);
        }
    }

}
