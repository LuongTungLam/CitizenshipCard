package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.*;
import com.biometrics.cmnd.common.model.Gender;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.model.ImageInfo;
import com.biometrics.cmnd.common.model.Pose;
import com.biometrics.cmnd.common.nxView.FaceViewLittle;
import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.biometrics.cmnd.common.validation.Validation;
import com.biometrics.cmnd.subject.coutryDB.entityDB.City;
import com.biometrics.cmnd.subject.coutryDB.entityDB.Country;
import com.biometrics.cmnd.subject.coutryDB.entityDB.District;
import com.biometrics.cmnd.subject.coutryDB.entityDB.Province;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.CityRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.CountryRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.DistrictRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.ProvinceRepository;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.jfoenix.controls.*;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
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
@FxmlView
public class NewProfileBioSSO implements Initializable {
    @Autowired
    private final SubjectService subjectService;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final Validation validation;
    private NSubject subject;
    private NFace face;
    private File fileFace;
    private FileChooser fileChooser;
    private final FaceViewLittle faceView;
    private final FingerViewLittle mirrorView, fingerprintView1, fingerprintView2, fingerprintView3, fingerprintView4, fingerprintView5, fingerprintView6, fingerprintView7, fingerprintView8, fingerprintView9, fingerprintView10;
    private final NBiometricClient client;
    private List<NFPosition> listMF = new ArrayList<>();
    private List<Image> images = new ArrayList<>();
    private List<FingerViewLittle> listViewFinger = new ArrayList<>();
    private List<StackPane> listStackPane = new ArrayList<>();
    private boolean scanning;
    private final SegmentHandler segmentHandler = new SegmentHandler();
    private final NDeviceManager deviceManager;

    @FXML
    private ListView<NFPosition> listMFP;
    @FXML
    private StackPane stpFace, mirrorFingerprint, fingerprint1, fingerprint2, fingerprint3, fingerprint4, fingerprint5, fingerprint6, fingerprint7, fingerprint8, fingerprint9, fingerprint10;
    @FXML
    private Pane paneFront, paneBack;
    @FXML
    private Button btnFile, btnNext, btnScan, btnBack, btnSave;
    @FXML
    private JFXComboBox cbbCountry, cbbCity, cbbProvince, cbbDistrict, cbbDeviceFinger;
    @FXML
    private JFXTextField tfFirstName, tfLastName, tfPhone, tfEmail, tfStreet, tfZip, tfNumberCard;
    @FXML
    private JFXDatePicker dpkDOB;
    @FXML
    private Label lblRThumb, lblRIndex, lblRMiddle, lblRRing, lblRLittle, lblLThumb, lblLIndex, lblLMiddle, lblLRing, lblLLittle;
    @FXML
    private JFXRadioButton male, female;


    public NewProfileBioSSO(SubjectService subjectService, NBiometricClient client, CountryRepository countryRepository, CityRepository cityRepository, ProvinceRepository provinceRepository, DistrictRepository districtRepository) {
        super();
        this.client = client;
        client.setUseDeviceManager(true);
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
        this.subjectService = subjectService;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
        this.validation = new Validation();
        this.faceView = new FaceViewLittle();
        this.fileChooser = new FileChooser();
        this.subject = new NSubject();
        this.mirrorView = new FingerViewLittle();
        this.fingerprintView1 = new FingerViewLittle();
        this.fingerprintView2 = new FingerViewLittle();
        this.fingerprintView3 = new FingerViewLittle();
        this.fingerprintView4 = new FingerViewLittle();
        this.fingerprintView5 = new FingerViewLittle();
        this.fingerprintView6 = new FingerViewLittle();
        this.fingerprintView7 = new FingerViewLittle();
        this.fingerprintView8 = new FingerViewLittle();
        this.fingerprintView9 = new FingerViewLittle();
        this.fingerprintView10 = new FingerViewLittle();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbbDeviceFinger.valueProperty().addListener(new FingerSelectionListener());
        initListMFP();
        initCBB();
        isNulls();
        dpkNulls();
        cbbNulls();
        isNumbers();
        isEmails();
        isPhones();
        initStackPanes();
        initFingerView();
        initStackPanesFinger();
        updateScannerList();
        initPosition();
    }

    private void initPosition() {
        fingerprintView1.setPosition(NFPosition.RIGHT_THUMB);
        fingerprintView2.setPosition(NFPosition.RIGHT_INDEX_FINGER);
        fingerprintView3.setPosition(NFPosition.RIGHT_MIDDLE_FINGER);
        fingerprintView4.setPosition(NFPosition.RIGHT_RING_FINGER);
        fingerprintView5.setPosition(NFPosition.RIGHT_LITTLE_FINGER);
        fingerprintView6.setPosition(NFPosition.LEFT_THUMB);
        fingerprintView7.setPosition(NFPosition.LEFT_INDEX_FINGER);
        fingerprintView8.setPosition(NFPosition.LEFT_MIDDLE_FINGER);
        fingerprintView9.setPosition(NFPosition.LEFT_RING_FINGER);
        fingerprintView10.setPosition(NFPosition.LEFT_LITTLE_FINGER);
    }

    // All Function
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        // function add Face and Information
        if (event.getSource().equals(btnNext)) {
            if (subject.getFaces().isEmpty() || tfFirstName.getText().isEmpty() || tfLastName.getText().isEmpty() || dpkDOB.getValue() == null || tfPhone.getText().isEmpty() || tfEmail.getText().isEmpty() || cbbCountry.getSelectionModel().isEmpty() || cbbCity.getSelectionModel().isEmpty() || cbbProvince.getSelectionModel().isEmpty() || cbbDistrict.getSelectionModel().isEmpty() || tfStreet.getText().isEmpty() || tfZip.getText().isEmpty()) {
                if (subject.getFaces().isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Face Image is required");
                        alert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Text is null");
                        alert.showAndWait();
                    });
                }
            } else {
                paneBack.toFront();
            }
        }
        // next step to scanning
        if (event.getSource().equals(btnBack)) {
            paneFront.toFront();
        }

        // open file
        if (event.getSource().equals(btnFile)) {
            fileFace = fileChooser.showOpenDialog(null);
            if (fileFace != null) {
                NImage image = NImage.fromFile(fileFace.getAbsolutePath());
                face = new NFace();
                face.setImage(image);
                subject.getFaces().add(face);
                this.faceView.setFace(face);
            }
        }

        // scanning finger
        if (event.getSource().equals(btnScan)) {
            updateControls();
            NFinger finger = new NFinger();
            subject.getFingers().add(finger);
            mirrorView.setFinger(finger);
            mirrorView.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
            segment();
            scanning = true;
        }

        if (event.getSource().equals(btnSave)) {
            if (subject.getFingers().isEmpty()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Fingerprint is required");
                    alert.showAndWait();
                });
            } else {
                // build subject
                buildSubject();
            }
        }
    }

    private void segment() {
        client.setFingersDeterminePatternClass(true);
        client.setFingersCalculateNFIQ(true);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE), subject);
        client.performTask(task, null, segmentHandler);
    }


    private void showSegments() {
        int indexFinger = subject.getFingers().size();

        if (listMF.size() < indexFinger) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successfully.");
            alert.setHeaderText(null);
            alert.setContentText("Enough number of fingers");
            alert.showAndWait();
            return;
        }

        switch (indexFinger) {
            case 1:
                setSegmentInfo(subject.getFingers().get(0), listMF.get(0), listViewFinger.get(0), listStackPane.get(0), lblRThumb);
                break;
            case 2:
                setSegmentInfo(subject.getFingers().get(1), listMF.get(1), listViewFinger.get(1), listStackPane.get(1), lblRIndex);
                break;
            case 3:
                setSegmentInfo(subject.getFingers().get(2), listMF.get(2), listViewFinger.get(2), listStackPane.get(2), lblRMiddle);
                break;
            case 4:
                setSegmentInfo(subject.getFingers().get(3), listMF.get(3), listViewFinger.get(3), listStackPane.get(3), lblRRing);
                break;
            case 5:
                setSegmentInfo(subject.getFingers().get(4), listMF.get(4), listViewFinger.get(4), listStackPane.get(4), lblRLittle);
                break;
            case 6:
                setSegmentInfo(subject.getFingers().get(5), listMF.get(5), listViewFinger.get(5), listStackPane.get(5), lblLThumb);
                break;
            case 7:
                setSegmentInfo(subject.getFingers().get(6), listMF.get(6), listViewFinger.get(6), listStackPane.get(6), lblLIndex);
                break;
            case 8:
                setSegmentInfo(subject.getFingers().get(7), listMF.get(7), listViewFinger.get(7), listStackPane.get(7), lblLMiddle);
                break;
            case 9:
                setSegmentInfo(subject.getFingers().get(8), listMF.get(8), listViewFinger.get(8), listStackPane.get(8), lblLRing);
                break;
            case 10:
                setSegmentInfo(subject.getFingers().get(9), listMF.get(9), listViewFinger.get(9), listStackPane.get(9), lblLLittle);
                break;

        }
    }

    private void setSegmentInfo(NFinger finger, NFPosition position, FingerViewLittle fingerView, StackPane stackPane, Label label) {
        finger.setPosition(position);
        String lblPosition = finger.getPosition().toString();
        if (lblPosition.equals(NFPosition.RIGHT_THUMB.name()) || lblPosition.equals(NFPosition.LEFT_THUMB.name())) {
            label.setTextFill(Color.WHITE);
            label.setBackground(new Background(new BackgroundFill(Color.rgb(255, 106, 106, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        } else if (lblPosition.equals(NFPosition.LEFT_INDEX_FINGER.name()) || lblPosition.equals(NFPosition.RIGHT_INDEX_FINGER.name())) {
            label.setTextFill(Color.WHITE);
            label.setBackground(new Background(new BackgroundFill(Color.rgb(84, 255, 159, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        } else {
            label.setTextFill(Color.BLACK);
            label.setBackground(new Background(new BackgroundFill(Color.rgb(193, 255, 193, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        }

        fingerView.setFinger(finger);
    }

    private void buildSubject() throws IOException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1);

        // rest of 11 digits
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }

        // add BioGraphy to subject from client
        BioGraphy bioGraphy = BioGraphy.builder()
                .firstName(tfFirstName.getText())
                .lastName(tfLastName.getText())
                .nid(sb.toString())
                .gender(Gender.valueOf(male.isSelected() ? "MALE" : "FEMALE"))
                .build();

        // add Address to subject from client
        Address address = Address.builder()
                .street(tfStreet.getText())
                .country(cbbCountry.getSelectionModel().getSelectedItem().toString())
                .city(cbbCity.getSelectionModel().getSelectedItem().toString())
                .province(cbbProvince.getSelectionModel().getSelectedItem().toString())
                .district(cbbDistrict.getSelectionModel().getSelectedItem().toString())
                .zip(tfZip.getText())
                .build();

        // add Contact to subject from client
        Contact contact = Contact.builder()
                .address(address)
                .phoneNumber(tfPhone.getText())
                .email(tfEmail.getText())
                .build();

        // create template for subject
        NBiometricTask task = client.createTask(
                EnumSet.of(NBiometricOperation.CREATE_TEMPLATE, NBiometricOperation.ASSESS_QUALITY), subject);
        client.performTask(task);

        // add Image Face to list images of subject
        Image imageFace = Image.builder()
                .quality(Byte.toUnsignedInt(subject.getFaces().get(1).getObjects().get(0).getQuality()))
                .bioType(BioType.FACE)
                .format(ImageFormat.JPG)
                .base64Image(NImageUtils.imageFileToBase64String(fileFace.getAbsolutePath()))
                .enabled(true)
                .pose(Pose.FACE_FRONT)
                .build();
        images.add(imageFace);

        // add Image Fingerprint to list images of subject
        for (int j = 0; j < subject.getFingers().size(); j++) {
            int fingerImageQuality = Byte.toUnsignedInt(subject.getFingers().get(j).getObjects().get(0).getQuality());

            NFPosition fingerPosition = subject.getFingers().get(j).getPosition();

            Image imageFinger = Image.builder()
                    .quality(fingerImageQuality)
                    .bioType(BioType.FINGER)
                    .format(ImageFormat.JPG)
                    .base64Image(NImageUtils.NImageToBase64String(subject.getFingers().get(j).getImage()))
                    .enabled(true)
                    .pose(nfPositionToPose(fingerPosition))
                    .build();
            images.add(imageFinger);
        }


        // create dto save data
        SubjectDto.CreateReq dto = SubjectDto.CreateReq.builder()
                .bioGraphy(bioGraphy)
                .contact(contact)
                .images(images)
                .build();
        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }


        List<ImageInfo> imageInfos = new ArrayList<>();

        // save images Face & Fingerprint from list Images of Subject
        for (int i = 0; i < dto.getImages().size(); i++) {
            // format image of face
            if (dto.getImages().get(i).getBioType().equals(BioType.FACE)) {
                String fileName = dto.getBioGraphy().getNid() + "_face." + dto.getImages().get(i).getFormat().name();

                switch (dto.getImages().get(i).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        subject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        subject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        subject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        subject.getFaces().get(i).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }

                ImageInfo faceImageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(dto.getImages().get(i).getQuality())
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();
                imageInfos.add(faceImageInfo);

            }
            // format images of fingerprints
            else if (dto.getImages().get(i).getBioType().equals(BioType.FINGER)) {
                String fileName = dto.getBioGraphy().getNid() + dto.getImages().get(i).getPose().name() + "." + dto.getImages().get(i).getFormat().name();
                switch (dto.getImages().get(i).getFormat().name()) {
                    case "JPG":
                    case "JPEG":
                        subject.getFingers().get(i - 1).getImage().save(filePath + fileName, NImageFormat.getJPEG());
                        break;
                    case "PNG":
                        subject.getFingers().get(i - 1).getImage().save(filePath + fileName, NImageFormat.getPNG());
                        break;
                    case "WSQ":
                        subject.getFingers().get(i - 1).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                        break;
                    case "TIFF":
                        subject.getFingers().get(i - 1).getImage().save(filePath + fileName, NImageFormat.getTIFF());
                        break;
                }

                ImageInfo faceImageInfo = ImageInfo.builder()
                        .imageFormat(dto.getImages().get(i).getFormat())
                        .imageUrl("/" + imagePath + fileName)
                        .imageQuality(dto.getImages().get(i).getQuality())
                        .bioType(dto.getImages().get(i).getBioType())
                        .pose(dto.getImages().get(i).getPose())
                        .enabled(true)
                        .build();
                imageInfos.add(faceImageInfo);
            }
        }
        byte[] template = subject.getTemplateBuffer().toByteArray();
        Subject subject = subjectService.create(dto, imageInfos, template);
//        if (check = true) {
        saveAlert(subject);
//            fxWeaver.loadController(AllSubject.class).refreshTableView();
//        }
    }

    private void saveAlert(Subject subject) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User saved successfully.");
        alert.setHeaderText(null);
        alert.setContentText("successfully");
        alert.showAndWait();
//        if (check = true) {
//            close();
//        }
//        return check;
    }

    // list StackPane Face & Fingerprint
    private void initStackPanes() {
        stpFace.getChildren().add(faceView);
        stpFace.setAlignment(Pos.CENTER);
        mirrorFingerprint.getChildren().add(mirrorView);
        mirrorFingerprint.setAlignment(Pos.CENTER);

        fingerprint1.getChildren().add(fingerprintView1);
        fingerprint1.setAlignment(Pos.CENTER);

        fingerprint2.getChildren().add(fingerprintView2);
        fingerprint2.setAlignment(Pos.CENTER);

        fingerprint3.getChildren().add(fingerprintView3);
        fingerprint3.setAlignment(Pos.CENTER);

        fingerprint4.getChildren().add(fingerprintView4);
        fingerprint4.setAlignment(Pos.CENTER);

        fingerprint5.getChildren().add(fingerprintView5);
        fingerprint5.setAlignment(Pos.CENTER);

        fingerprint6.getChildren().add(fingerprintView6);
        fingerprint6.setAlignment(Pos.CENTER);

        fingerprint7.getChildren().add(fingerprintView7);
        fingerprint7.setAlignment(Pos.CENTER);

        fingerprint8.getChildren().add(fingerprintView8);
        fingerprint8.setAlignment(Pos.CENTER);

        fingerprint9.getChildren().add(fingerprintView9);
        fingerprint9.setAlignment(Pos.CENTER);

        fingerprint10.getChildren().add(fingerprintView10);
        fingerprint10.setAlignment(Pos.CENTER);
    }

    // list FingerView
    private void initFingerView() {
        listViewFinger.add(fingerprintView1);
        listViewFinger.add(fingerprintView2);
        listViewFinger.add(fingerprintView3);
        listViewFinger.add(fingerprintView4);
        listViewFinger.add(fingerprintView5);
        listViewFinger.add(fingerprintView6);
        listViewFinger.add(fingerprintView7);
        listViewFinger.add(fingerprintView8);
        listViewFinger.add(fingerprintView9);
        listViewFinger.add(fingerprintView10);
    }

    //list StackPanes Fingerprint
    private void initStackPanesFinger() {
        listStackPane.add(fingerprint1);
        listStackPane.add(fingerprint2);
        listStackPane.add(fingerprint3);
        listStackPane.add(fingerprint4);
        listStackPane.add(fingerprint5);
        listStackPane.add(fingerprint6);
        listStackPane.add(fingerprint7);
        listStackPane.add(fingerprint8);
        listStackPane.add(fingerprint9);
        listStackPane.add(fingerprint10);
    }

    // list country in database
    private void initCBB() {
        List<Country> countries = countryRepository.findAll();
        cbbCity.getItems().clear();
        cbbProvince.getItems().clear();
        cbbDistrict.getItems().clear();

        for (int i = 0; i < countries.size(); i++) {
            cbbCountry.getItems().add(countries.get(i).getCountry_name());
        }

        cbbCountry.getSelectionModel().selectedItemProperty().addListener((obs, old, newObs) -> {
            cbbCity.getItems().clear();
            List<City> cities = new ArrayList<>();
            cities.addAll(cityRepository.findAll());
            for (int j = 0; j < cities.size(); j++) {
                if (newObs.equals(cities.get(j).getCountry().getCountry_name())) {
                    cbbCity.getItems().add(cities.get(j).getCity_name());
                }
            }

        });
        cbbCity.getSelectionModel().selectedItemProperty().addListener((obs1, old1, newObs1) -> {
            cbbProvince.getItems().clear();
            List<Province> provinces = provinceRepository.findAll();
            for (int k = 0; k < provinces.size(); k++) {
                if (newObs1.equals(provinces.get(k).getCity().getCity_name())) {
                    cbbProvince.getItems().add(provinces.get(k).getProvince_name());
                }
            }
        });
        cbbProvince.getSelectionModel().selectedItemProperty().addListener((obs2, old2, newObs2) -> {
            cbbDistrict.getItems().clear();
            List<District> districts = districtRepository.findAll();
            for (int l = 0; l < districts.size(); l++) {
                if (newObs2.equals(districts.get(l).getProvince().getProvince_name())) {
                    cbbDistrict.getItems().add(districts.get(l).getDistrict_name());
                }
            }
        });
    }

    // list missing fingerprint
    private void initListMFP() {
        listMFP.setDisable(false);
        ObservableList<NFPosition> positions = FXCollections.observableArrayList(
                NFPosition.RIGHT_THUMB,
                NFPosition.RIGHT_INDEX_FINGER,
                NFPosition.RIGHT_MIDDLE_FINGER,
                NFPosition.RIGHT_RING_FINGER,
                NFPosition.RIGHT_LITTLE_FINGER,
                NFPosition.LEFT_THUMB,
                NFPosition.LEFT_INDEX_FINGER,
                NFPosition.LEFT_MIDDLE_FINGER,
                NFPosition.LEFT_RING_FINGER,
                NFPosition.LEFT_LITTLE_FINGER
        );
        listMF.addAll(positions);
        listMFP.getItems().addAll(positions);
        listMFP.setCellFactory(CheckBoxListCell.forListView(new Callback<NFPosition, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(NFPosition position) {
                BooleanProperty property = new SimpleBooleanProperty();
                property.addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        listMF.remove(position);

                    } else {
                        listMF.add(position);
                    }
                });
                return property;
            }
        }));
    }

    //validate
    private void isNulls() {
        validation.isNullFx(tfEmail);
        validation.isNullFx(tfPhone);
        validation.isNullFx(tfFirstName);
        validation.isNullFx(tfLastName);
        validation.isNullFx(tfStreet);
        validation.isNullFx(tfZip);
    }

    private void isNumbers() {
        validation.isNumber(tfZip);
    }

    private void isEmails() {
        validation.isEmail(tfEmail);
    }

    private void isPhones() {
        validation.isPhone(tfPhone);
    }

    private void cbbNulls() {
        validation.cbbNull(cbbCountry);
        validation.cbbNull(cbbCity);
        validation.cbbNull(cbbProvince);
        validation.cbbNull(cbbDistrict);
    }

    private void dpkNulls() {
        validation.dpkNull(dpkDOB);
        dpkDOB.setConverter(new StringConverter<LocalDate>() {
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
    }


    //convert NFPosition to Pose
    private Pose nfPositionToPose(NFPosition nfPosition) {
        Pose pose = null;
        switch (nfPosition) {
            case LEFT_LITTLE_FINGER:
                pose = Pose.FINGER_LEFT_LITTLE;
                break;
            case LEFT_RING_FINGER:
                pose = Pose.FINEGR_LEFT_RING;
                break;
            case LEFT_MIDDLE_FINGER:
                pose = Pose.FINEGR_LEFT_MIDDLE;
                break;
            case LEFT_INDEX_FINGER:
                pose = Pose.FINGER_LEFT_INDEX;
                break;
            case LEFT_THUMB:
                pose = Pose.FINGER_LEFT_THUMB;
                break;
            case RIGHT_THUMB:
                pose = Pose.FINGER_RIGHT_THUMB;
                break;
            case RIGHT_INDEX_FINGER:
                pose = Pose.FINGER_RIGHT_INDEX;
                break;
            case RIGHT_MIDDLE_FINGER:
                pose = Pose.FINGER_RIGHT_MIDDLE;
                break;
            case RIGHT_RING_FINGER:
                pose = Pose.FINGER_RIGHT_RING;
                break;
            case RIGHT_LITTLE_FINGER:
                pose = Pose.FINGER_RIGHT_LITTLE;
                break;
            default:
                break;
        }
        return pose;
    }

    // format folder save subject
    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }

    private void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            cbbDeviceFinger.getItems().add((NFingerScanner) device);
        }
        NFingerScanner scanner = (NFingerScanner) client.getFingerScanner();
        if (scanner == null) {
            cbbDeviceFinger.getSelectionModel().selectFirst();
        } else if (scanner != null) {
            cbbDeviceFinger.getSelectionModel().select(scanner);
        }
    }

    private class FingerSelectionListener implements ChangeListener<NFingerScanner> {
        @Override
        public void changed(ObservableValue<? extends NFingerScanner> observableValue, NFingerScanner nFingerScanner, NFingerScanner newFinger) {
            client.setFingerScanner(newFinger);
        }
    }

    private void updateShownImage() {
        mirrorView.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
    }

    private void updateSegmentationStatus(NBiometricStatus status) {
        if (status == NBiometricStatus.OK) {
            showSegments();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Segmentation failed: " + status);
                alert.showAndWait();
            });
            return;
        }
    }

    private class SegmentHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask task, final Object attachment) {
            Platform.runLater(() -> {
                scanning = false;
                updateShownImage();
                updateSegmentationStatus(task.getStatus());
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            Platform.runLater(() -> {
                scanning = false;
                updateShownImage();
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setContentText("Error");
                a.setHeaderText("Is Failed");
                a.show();
            });
        }
    }

    private void updateControls() {
        listMFP.setDisable(true);
    }

}