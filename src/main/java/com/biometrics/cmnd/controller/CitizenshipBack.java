package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.*;
import com.biometrics.cmnd.common.model.Gender;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.model.ImageInfo;
import com.biometrics.cmnd.common.model.Pose;
import com.biometrics.cmnd.common.nxView.FaceViewLittle;
import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("CitizenshipBack.fxml")
public class CitizenshipBack implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    @FXML
    private AnchorPane addDialog;

    @FXML
    private Button scan, save;
    @FXML
    private StackPane viewFace, leftLittle, leftRing, leftMiddle, leftIndex, leftThumb, rightLittle, rightRing, rightMiddle, rightIndex, rightThumb, viewFinger;
    @FXML
    private ComboBox fingerBox;
    @FXML
    private Label missingFinger, lblLeftLittle, lblLeftRing, lblLeftMiddle, lblLeftIndex, lblLeftThumb, lblRightThumb, lblRightIndex, lblRightMiddle, lblRightRing, lblRightLittle;
    @FXML
    private TextField tfFirstName, tfLastName, tfCity, tfProvince, tfDistrict, tfCountry, tfStreet, tfZip, tfEmail, tfPhone, tfNid;
    @FXML
    private DatePicker dpBirthDay;
    @FXML
    private RadioButton rdbMale, rdbFemale;

    private Stage stage;
    private NSubject subject;
    private final FingerViewLittle viewFingers, viewLeftLittle, viewLeftRing, viewLeftMiddle, viewLeftIndex, viewLeftThumb, viewRightLittle, viewRightRing, viewRightMiddle, viewRightIndex, viewRightThumb;
    private final FaceViewLittle viewFaces;
    private final NDeviceManager deviceManager;
    private final NBiometricClient client;
    private final FxWeaver fxWeaver;
    private final SegmentHandler segmentHandler = new SegmentHandler();
    private boolean scanning;
    private boolean check;
    private NFace face;

    private FileChooser fileChooser;

    private List<NFinger> fingerImage = new ArrayList<>();

    private List<String> base64Image = new ArrayList<>();

    private List<Image> images = new ArrayList<>();

    private List<NFPosition> listMissingFinger = new ArrayList<>();

    private File fileFace;

    private List<Label> listLblFinger = new ArrayList<>();

    private List<FingerViewLittle> listViewFinger = new ArrayList<>();

    private List<StackPane> listStackPane = new ArrayList<>();

    @FXML
    private ListView<NFPosition> listPositions;

    public CitizenshipBack(SubjectService subjectService, NBiometricClient client, FxWeaver fxWeaver) {
        this.client = client;
        this.fxWeaver = fxWeaver;
        this.subjectService = subjectService;
        client.setUseDeviceManager(true);
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
        this.subject = new NSubject();
        this.viewFingers = new FingerViewLittle();
        this.viewLeftLittle = new FingerViewLittle();
        this.viewLeftRing = new FingerViewLittle();
        this.viewLeftMiddle = new FingerViewLittle();
        this.viewLeftIndex = new FingerViewLittle();
        this.viewLeftThumb = new FingerViewLittle();
        this.viewRightLittle = new FingerViewLittle();
        this.viewRightRing = new FingerViewLittle();
        this.viewRightMiddle = new FingerViewLittle();
        this.viewRightIndex = new FingerViewLittle();
        this.viewRightThumb = new FingerViewLittle();
        this.listPositions = new ListView<NFPosition>();
        this.viewFaces = new FaceViewLittle();
        this.fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        stage.setScene(new Scene(addDialog));
        fingerBox.valueProperty().addListener(new FingerSelectionListener());
        this.viewFinger.getChildren().add(viewFingers);
        this.viewFinger.setAlignment(Pos.CENTER);
        this.viewFace.getChildren().add(viewFaces);
        this.viewFace.setAlignment(Pos.CENTER);
        this.leftLittle.getChildren().add(viewLeftLittle);
        this.leftLittle.setAlignment(Pos.CENTER);
        this.leftRing.getChildren().add(viewLeftRing);
        this.leftRing.setAlignment(Pos.CENTER);
        this.leftMiddle.getChildren().add(viewLeftMiddle);
        this.leftMiddle.setAlignment(Pos.CENTER);
        this.leftIndex.getChildren().add(viewLeftIndex);
        this.leftIndex.setAlignment(Pos.CENTER);
        this.leftThumb.getChildren().add(viewLeftThumb);
        this.leftThumb.setAlignment(Pos.CENTER);
        this.rightLittle.getChildren().add(viewRightLittle);
        this.rightLittle.setAlignment(Pos.CENTER);
        this.rightRing.getChildren().add(viewRightRing);
        this.rightRing.setAlignment(Pos.CENTER);
        this.rightMiddle.getChildren().add(viewRightMiddle);
        this.rightMiddle.setAlignment(Pos.CENTER);
        this.rightIndex.getChildren().add(viewRightIndex);
        this.rightIndex.setAlignment(Pos.CENTER);
        this.rightThumb.getChildren().add(viewRightThumb);
        this.rightThumb.setAlignment(Pos.CENTER);
        updateScannerList();
        initFingerPositions();
        controlColor();
        initFingerView();
        intLblFinger();
        initStackPane();
        dpBirthDay.setConverter(new StringConverter<LocalDate>() {
                                    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                                    @Override
                                    public String toString(LocalDate date) {
                                        return (date != null) ? dateTimeFormatter.format(date) : "";
                                    }

                                    @Override
                                    public LocalDate fromString(String string) {
                                        return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateTimeFormatter) : null;
                                    }
                                }
        );
    }

    public void initFingerPositions() {
        ObservableList<NFPosition> positions = FXCollections.observableArrayList(
                NFPosition.LEFT_LITTLE_FINGER,
                NFPosition.LEFT_RING_FINGER,
                NFPosition.LEFT_MIDDLE_FINGER,
                NFPosition.LEFT_INDEX_FINGER,
                NFPosition.LEFT_THUMB,
                NFPosition.RIGHT_THUMB,
                NFPosition.RIGHT_INDEX_FINGER,
                NFPosition.RIGHT_MIDDLE_FINGER,
                NFPosition.RIGHT_RING_FINGER,
                NFPosition.RIGHT_LITTLE_FINGER
        );
        listMissingFinger.addAll(positions);
        listPositions.getItems().addAll(positions);
        listPositions.setCellFactory(CheckBoxListCell.forListView(new Callback<NFPosition, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(NFPosition position) {
                BooleanProperty property = new SimpleBooleanProperty();
                property.addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        listMissingFinger.remove(position);

                    } else {
                        listMissingFinger.add(position);
                    }
                });
                return property;
            }
        }));
    }

    private void initFingerView() {
        listViewFinger.add(viewLeftLittle);
        listViewFinger.add(viewLeftRing);
        listViewFinger.add(viewLeftMiddle);
        listViewFinger.add(viewLeftIndex);
        listViewFinger.add(viewLeftThumb);
        listViewFinger.add(viewRightThumb);
        listViewFinger.add(viewRightIndex);
        listViewFinger.add(viewRightMiddle);
        listViewFinger.add(viewRightRing);
        listViewFinger.add(viewRightLittle);
    }

    private void initStackPane() {
        listStackPane.add(leftLittle);
        listStackPane.add(leftRing);
        listStackPane.add(leftMiddle);
        listStackPane.add(leftIndex);
        listStackPane.add(leftThumb);
        listStackPane.add(rightThumb);
        listStackPane.add(rightIndex);
        listStackPane.add(rightMiddle);
        listStackPane.add(rightRing);
        listStackPane.add(rightLittle);
    }

    private void intLblFinger() {
        listLblFinger.add(lblLeftLittle);
        listLblFinger.add(lblLeftRing);
        listLblFinger.add(lblLeftMiddle);
        listLblFinger.add(lblLeftIndex);
        listLblFinger.add(lblLeftThumb);
        listLblFinger.add(lblRightThumb);
        listLblFinger.add(lblRightIndex);
        listLblFinger.add(lblRightMiddle);
        listLblFinger.add(lblRightRing);
        listLblFinger.add(lblRightLittle);
    }

    // scan by roller finger
    @FXML
    public void roll(ActionEvent event) {
        NFinger finger = new NFinger();
        NFImpressionType roll = NFImpressionType.LIVE_SCAN_OPTICAL_CONTACT_ROLLED;
        finger.setImpressionType(roll);
        subject = new NSubject();
        subject.getFingers().add(finger);
        viewFingers.setFinger(finger);
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        segment();
    }

    // open file face
    @FXML
    public void file(ActionEvent event) throws IOException {
        fileFace = fileChooser.showOpenDialog(null);
        if (fileFace != null) {
            NImage image = NImage.fromFile(fileFace.getAbsolutePath());
            face = new NFace();
            face.setImage(image);
            subject.getFaces().add(face);
            this.viewFaces.setFace(face);
        }
    }

    // scan finger default
    @FXML
    public void scan(ActionEvent event) {
        NFinger finger = new NFinger();
        subject.getFingers().add(finger);
        viewFingers.setFinger(finger);
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        segment();
        scanning = true;
    }

    private void segment() {
        client.setFingersDeterminePatternClass(true);
        client.setFingersCalculateNFIQ(true);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE), subject);
        client.performTask(task, null, segmentHandler);
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
        }
    }

    private void showSegments() {
        int indexFinger = subject.getFingers().size();

        if (listMissingFinger.size() < indexFinger) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successfully.");
            alert.setHeaderText(null);
            alert.setContentText("Enough number of fingers");
            alert.showAndWait();
            return;
        }

        switch (indexFinger) {
            case 1:
                setSegmentInfo(subject.getFingers().get(0), listLblFinger.get(0), listMissingFinger.get(0), listViewFinger.get(0), listStackPane.get(0));
                break;
            case 2:
                setSegmentInfo(subject.getFingers().get(1), listLblFinger.get(1), listMissingFinger.get(1), listViewFinger.get(1), listStackPane.get(1));
                break;
            case 3:
                setSegmentInfo(subject.getFingers().get(2), listLblFinger.get(2), listMissingFinger.get(2), listViewFinger.get(2), listStackPane.get(2));
                break;
            case 4:
                setSegmentInfo(subject.getFingers().get(3), listLblFinger.get(3), listMissingFinger.get(3), listViewFinger.get(3), listStackPane.get(3));
                break;
            case 5:
                setSegmentInfo(subject.getFingers().get(4),listLblFinger.get(4), listMissingFinger.get(4), listViewFinger.get(4), listStackPane.get(4));
                break;
            case 6:
                setSegmentInfo(subject.getFingers().get(5),listLblFinger.get(5), listMissingFinger.get(5), listViewFinger.get(5), listStackPane.get(5));
                break;
            case 7:
                setSegmentInfo(subject.getFingers().get(6),listLblFinger.get(6), listMissingFinger.get(6), listViewFinger.get(6), listStackPane.get(6));
                break;
            case 8:
                setSegmentInfo(subject.getFingers().get(7),listLblFinger.get(7), listMissingFinger.get(7), listViewFinger.get(7), listStackPane.get(7));
                break;
            case 9:
                setSegmentInfo(subject.getFingers().get(8),listLblFinger.get(8), listMissingFinger.get(8), listViewFinger.get(8), listStackPane.get(8));
                break;
            case 10:
                setSegmentInfo(subject.getFingers().get(9),listLblFinger.get(9), listMissingFinger.get(9), listViewFinger.get(9), listStackPane.get(9));
                break;
        }
    }

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

    private void setSegmentInfo(NFinger finger, Label lblPs, NFPosition position, FingerViewLittle fingerView, StackPane stackPane) {
        finger.setPosition(position);
        String lblPosition = finger.getPosition().toString();
        lblPs.setText(lblPosition);
        if (lblPosition.equals(NFPosition.RIGHT_THUMB.name()) || lblPosition.equals(NFPosition.LEFT_THUMB.name())){
            lblPs.setTextFill(Color.BLACK);
            lblPs.setBackground(new Background(new BackgroundFill(Color.rgb(255,106,106, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        }else  if (lblPosition.equals(NFPosition.LEFT_INDEX_FINGER.name()) || lblPosition.equals(NFPosition.RIGHT_INDEX_FINGER.name())){
            lblPs.setTextFill(Color.BLACK);
            lblPs.setBackground(new Background(new BackgroundFill(Color.rgb(84, 255, 159, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        }else {
            lblPs.setTextFill(Color.BLACK);
            lblPs.setBackground(new Background(new BackgroundFill(Color.rgb(193, 255, 193, 0.7), new CornerRadii(5.0), new Insets(-5.0))));
        }

        fingerView.setFinger(finger);
    }

    private void updateShownImage() {
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
    }

    @FXML
    public void save(ActionEvent event) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        BioGraphy bioGraphy = BioGraphy.builder()
                .firstName(tfFirstName.getText())
                .lastName(tfLastName.getText())
                .nid(tfNid.getText())
                .gender(Gender.valueOf(rdbMale.isSelected() ? "MALE" : "FEMALE"))
                .birthDate(dpBirthDay.getValue())
                .build();

        Address address = Address.builder()
                .street(tfStreet.getText())
                .city(tfCity.getText())
                .district(tfDistrict.getText())
                .province(tfProvince.getText())
                .country(tfCountry.getText())
                .zip(tfZip.getText())
                .build();

        Contact contact = Contact.builder()
                .address(address)
                .phoneNumber(tfPhone.getText())
                .email(tfEmail.getText())
                .build();

        NBiometricTask task = client.createTask(
                EnumSet.of(NBiometricOperation.CREATE_TEMPLATE, NBiometricOperation.ASSESS_QUALITY), subject);
        client.performTask(task);

        Image imageFace = Image.builder()
                .quality(Byte.toUnsignedInt(subject.getFaces().get(1).getObjects().get(0).getQuality()))
                .bioType(BioType.FACE)
                .format(ImageFormat.JPG)
                .base64Image(NImageUtils.imageFileToBase64String(fileFace.getAbsolutePath()))
                .enabled(true)
                .pose(Pose.FACE_FRONT)
                .build();
        images.add(imageFace);

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

        for (int i = 0; i < dto.getImages().size(); i++) {
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
            } else if (dto.getImages().get(i).getBioType().equals(BioType.FINGER)) {
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
        if (check = true) {
            saveAlert(subject);
//            fxWeaver.loadController(AllSubject.class).refreshTableView();
        }
    }

    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }

    public void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            fingerBox.getItems().add((NFingerScanner) device);
        }
        NFingerScanner scanner = (NFingerScanner) client.getFingerScanner();
        if (scanner == null) {
            fingerBox.getSelectionModel().selectFirst();
        } else if (scanner != null) {
            fingerBox.getSelectionModel().select(scanner);
        }
    }

    private class FingerSelectionListener implements ChangeListener<NFingerScanner> {
        @Override
        public void changed(ObservableValue<? extends NFingerScanner> observableValue, NFingerScanner nFingerScanner, NFingerScanner newFinger) {
            client.setFingerScanner(newFinger);
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

    private void controlColor() {
        if (leftLittle.getChildren().isEmpty()) {
            lblLeftLittle.setTextFill(Color.GREEN);
        }
    }

    public boolean saveAlert(Subject subject) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User saved successfully.");
        alert.setHeaderText(null);
        alert.setContentText("successfully");
        alert.showAndWait();
        if (check = true) {
            close();
        }
        return check;
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage = (Stage) save.getScene().getWindow();
        stage.close();
    }
}
