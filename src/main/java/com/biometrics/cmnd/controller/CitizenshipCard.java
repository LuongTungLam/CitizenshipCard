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
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFingerViewBase;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    @FXML
    private ComboBox listScan;

    private boolean scanning;
    private File fileFace;
    private File fileFinger;
    private FileChooser fc;
    private NSubject nSubject;

    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

    @Autowired
    private final SubjectService subjectService;

    private final NBiometricClient client;
    private final FaceViewNode faceViewNode;
    private final FingerViewNode fingerViewNode;
    private final NDeviceManager deviceManager;


    public CitizenshipCard(SubjectService subjectService, NBiometricClient client) {
        super();
        this.faceViewNode = new FaceViewNode();
        this.fingerViewNode = new FingerViewNode();
        this.subjectService = subjectService;
        this.client = client;
        this.nSubject = new NSubject();
        client.setUseDeviceManager(true);
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listScan.valueProperty().addListener(new FingerSelectionListener());
        this.imageview.getChildren().add(faceViewNode);
        this.imageview.setAlignment(Pos.CENTER);
        this.fingerView.getChildren().add(fingerViewNode);
        this.fingerView.setAlignment(Pos.CENTER);
        updateScannerList();
    }

    public void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            listScan.getItems().add((NFingerScanner) device);
        }
        NFingerScanner scanner = (NFingerScanner) client.getFingerScanner();
        if (scanner == null) {
            listScan.getSelectionModel().selectFirst();
        } else if (scanner != null) {
            listScan.getSelectionModel().select(scanner);
        }
    }

    @FXML
    private void chooseImage(ActionEvent event) throws IOException {
        fc = new FileChooser();
        fileFace = fc.showOpenDialog(null);
        if (fileFace != null) {
            NImage image = NImage.fromFile(fileFace.getAbsolutePath());
            NFace face = new NFace();
            face.setImage(image);
            nSubject.getFaces().add(face);
            this.faceViewNode.setFace(face);
        }
    }

    @FXML
    private void liveScan(ActionEvent event) throws IOException {
        NFinger finger = new NFinger();
        nSubject.getFingers().add(finger);
        fingerViewNode.setFinger(finger);
        fingerViewNode.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE), nSubject);
        client.performTask(task, null, captureCompletionHandler);
        scanning = true;

        finger.setPosition(NFPosition.RIGHT_THUMB);
        finger.setImage(nSubject.getFingers().get(0).getImage());
    }


    @FXML
    private void scan(ActionEvent event) throws IOException {
//        fingerView.getChildren().clear();
        fc = new FileChooser();
        fileFinger = fc.showOpenDialog(null);
        if (fileFinger != null) {
            NImage image = NImage.fromFile(fileFinger.getAbsolutePath());
            NFinger finger = new NFinger();
            // should set finger position
            finger.setPosition(NFPosition.RIGHT_THUMB);
            finger.setImage(image);
            nSubject.getFingers().add(finger);
            this.fingerViewNode.setFinger(finger);
        }
    }

    @FXML
    private void save(ActionEvent event) throws Exception {
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

        NBiometricTask task = client.createTask(
                EnumSet.of(NBiometricOperation.DETECT_SEGMENTS,
                        NBiometricOperation.ASSESS_QUALITY,
                        NBiometricOperation.CREATE_TEMPLATE),
                nSubject);
        client.performTask(task);

        System.out.println(task.getStatus().name());

        if (task.getStatus() != NBiometricStatus.OK) {
            // error exception;
        }

        // create list images convert to base64
        List<String> base64Image = new ArrayList<>();
        if (fileFinger == null) {
            base64Image.add(NImageUtils.imageFileToBase64String(fileFace.getAbsolutePath()));
            base64Image.add(NImageUtils.NImageToBase64String(nSubject.getFingers().get(1).getImage()));
        } else {
            base64Image.add(NImageUtils.imageFileToBase64String(fileFace.getAbsolutePath()));
            base64Image.add(NImageUtils.imageFileToBase64String(fileFinger.getAbsolutePath()));
        }

        // Get face and finger image quality
        int faceImageQuality = Byte.toUnsignedInt(nSubject.getFaces().get(1).getObjects().get(0).getQuality());
        int fingerImageQuality = Byte.toUnsignedInt(nSubject.getFingers().get(1).getObjects().get(0).getQuality());

        // list images have bioType insert to SubjectDto
        List<Image> images = new ArrayList<>();
        Image faceImage = Image.builder()
                .quality(faceImageQuality)
                .bioType(BioType.FACE)
                .format(ImageFormat.JPG)
                .base64Image(base64Image.get(0))
                .enabled(true)
                .pose(Pose.FACE_FRONT)
                .build();

        Image fingerImage = Image.builder()
                .quality(fingerImageQuality)
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

        // create directory for save face and image files.
        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }

        List<ImageInfo> imageInfos = new ArrayList<>();

        for (int i = 0; i < dto.getImages().size(); i++) {

            if (dto.getImages().get(i).getBioType().equals(BioType.FACE)) {
                String fileName = dto.getBioGraphy().getNid() + "_face." + dto.getImages().get(i).getFormat().name();

                switch (dto.getImages().get(1).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        nSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        nSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        nSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        nSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }

                ImageInfo faceImageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(faceImageQuality)
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();
                imageInfos.add(faceImageInfo);

            } else if (dto.getImages().get(i).getBioType().equals(BioType.FINGER)) {

                String fileName = dto.getBioGraphy().getNid() + "_finger." + dto.getImages().get(i).getFormat().name();

                switch (dto.getImages().get(i).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        nSubject.getFingers().get(1).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        nSubject.getFingers().get(1).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        nSubject.getFingers().get(1).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        nSubject.getFingers().get(1).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }
                ImageInfo fingerImageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(fingerImageQuality)
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();
                imageInfos.add(fingerImageInfo);
            }
        }
        byte[] template = nSubject.getTemplateBuffer().toByteArray();
        Subject subject = subjectService.create(dto, imageInfos, template);
        saveAlert(subject);
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
            fingerViewNode.setFinger(nSubject.getFingers().get(0));
        } else {
            fingerViewNode.setFinger(null);
        }
    }

    private class FingerSelectionListener implements ChangeListener<NFingerScanner> {
        @Override
        public void changed(ObservableValue<? extends NFingerScanner> observableValue, NFingerScanner nFingerScanner, NFingerScanner newFinger) {
            client.setFingerScanner(newFinger);
        }
    }

    private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            Platform.runLater(() -> {
                scanning = false;
                updateShownImage();
            });
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {

        }
    }

    private void updateShownImage() {
        fingerViewNode.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
    }
}
