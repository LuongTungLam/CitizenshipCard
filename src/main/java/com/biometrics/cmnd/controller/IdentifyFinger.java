package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioGraphy;
import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.dto.Image;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.model.Pose;
import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.biometrics.cmnd.identify.dto.CandidateListItem;
import com.biometrics.cmnd.identify.dto.IdentifyDto;
import com.biometrics.cmnd.identify.entity.Identify;
import com.biometrics.cmnd.identify.service.IdentifyService;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.BioTemplate;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
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
import com.neurotec.io.NBuffer;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Component
@FxmlView("IdentifyFinger.fxml")
public class IdentifyFinger implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    @Autowired
    private final RecognitionService recognitionService;

    @Autowired
    private final IdentifyService identifyService;

    @FXML
    private StackPane fingerIdentify, faceIdentify;
    @FXML
    private Button scan, identify;
    @FXML
    private ComboBox cbbScan;
    @FXML
    private AnchorPane identifyDialog;
    @FXML
    private TableView<CandidateListItem> viewIdentify;

    private boolean scanning;
    private ObservableList data;
    private final FingerViewLittle viewFinger;
    private Stage stage;
    private final FxWeaver fxWeaver;
    private final NDeviceManager deviceManager;
    private final NBiometricClient client;
    private NSubject subject;
    private List<Subject> subjects;
    private final SegmentHandler segmentHandler = new SegmentHandler();
    private List<SubjectDto.SubjectRes> results = new ArrayList<>();
    private ObservableList identifyList;
    private List<CandidateListItem> candidateList = new ArrayList<>();

    public IdentifyFinger(NBiometricClient client, FxWeaver fxWeaver, SubjectService subjectService, RecognitionService recognitionService, IdentifyService identifyService) {
        super();
        this.client = client;
        this.subjectService = subjectService;
        this.recognitionService = recognitionService;
        this.identifyService = identifyService;
        this.fxWeaver = fxWeaver;
        client.setUseDeviceManager(true);
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
        this.subject = new NSubject();
        this.viewFinger = new FingerViewLittle();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        stage.setScene(new Scene(identifyDialog));
        cbbScan.valueProperty().addListener(new IdentifyFinger.FingerSelectionListener());
        this.fingerIdentify.getChildren().add(viewFinger);
        this.fingerIdentify.setAlignment(Pos.CENTER);
        updateScannerList();
        findAll();
        listIdentify();
    }

    private void listIdentify() {
        TableColumn<CandidateListItem, String> id = new TableColumn<>("Id");
        id.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CandidateListItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CandidateListItem, String> id) {
                return new SimpleObjectProperty(id.getValue().getSubjectId());
            }
        });

        TableColumn<CandidateListItem, String> score = new TableColumn<>("Score");
        score.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CandidateListItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CandidateListItem, String> score) {
                return new SimpleObjectProperty(score.getValue().getScore());
            }
        });

        TableColumn<CandidateListItem, javafx.scene.image.Image> image = new TableColumn<>("Image");
        image.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CandidateListItem, javafx.scene.image.Image>, ObservableValue<javafx.scene.image.Image>>() {
            @Override
            public ObservableValue<javafx.scene.image.Image> call(TableColumn.CellDataFeatures<CandidateListItem, javafx.scene.image.Image> images) {
                ImageView viewFaceIdentify = new ImageView();
                viewFaceIdentify.setFitWidth(50);
                viewFaceIdentify.setFitHeight(50);
                Subject subject = subjectService.findById(images.getValue().getSubjectId());
                for (int i = 0; i < subject.getSubjectImages().size(); i++) {
                    if (subject.getSubjectImages().get(i).getImageInfo().getPose().equals(Pose.FACE_FRONT)) {
                        try {
                            File file = new File("uploads" + subject.getSubjectImages().get(i).getImageInfo().getImageUrl());
                            viewFaceIdentify.setImage(new javafx.scene.image.Image(file.toURI().toURL().toExternalForm()));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return new SimpleObjectProperty(viewFaceIdentify);
            }
        });
        TableColumn<CandidateListItem, String> pose = new TableColumn<>("Pose");
        pose.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CandidateListItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CandidateListItem, String> poses) {
                Subject subject = subjectService.findById(poses.getValue().getSubjectId());
                for (int i = 0; i < subject.getSubjectImages().size(); i++) {

                }
                return null;
            }
        });
        viewIdentify.getColumns().addAll(id, score, image);
    }


    @FXML
    public void scan(ActionEvent event) throws IOException {
        viewIdentify.getItems().clear();
        updateFingersTools();
        NFinger finger = new NFinger();
        subject = new NSubject();
        subject.getFingers().add(finger);
        viewFinger.setFinger(finger);
        viewFinger.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);
        client.performTask(task, null, segmentHandler);
        scanning = true;
    }

    private class FingerSelectionListener implements ChangeListener<NFingerScanner> {
        @Override
        public void changed(ObservableValue<? extends NFingerScanner> observableValue, NFingerScanner nFingerScanner, NFingerScanner newFinger) {
            client.setFingerScanner(newFinger);
        }
    }

    @FXML
    private void identify() throws IOException {
        IdentifyDto.Req req = IdentifyDto.Req.builder()
                .imageFormat(ImageFormat.PNG)
                .prob(NImageUtils.NImageToBase64String(subject.getFingers().get(0).getImage()))
                .build();
        List<NSubject> referenceSubjects = new ArrayList<>();

        // step 1: get bioTemplate all of subject
        for (int i = 0; i < results.size(); i++) {
            for (int j = 0; j < results.get(i).getSubjectImages().size(); j++) {
                if (results.get(i).getSubjectImages().get(j).getImageInfo().getBioType().equals(BioType.FINGER)) {
                    BioTemplate bioTemplate = results.get(i).getSubjectImages().get(j).getSubject().getBioTemplate();
                    NSubject referenceSubject = createSubject(bioTemplate.getTemplate(), String.valueOf(bioTemplate.getSubject().getId()));
                    referenceSubjects.add(referenceSubject);
                }
            }
        }

        // step 2: create prob subject
        NSubject prodSubject = createSubject(req.getProb(), req.getImageFormat());
        // step 2: identification
        NSubject resultSubject = recognitionService.identify(prodSubject, referenceSubjects);

        // step 3: convert face token NImage to byte array
        NImage prodTokenImage = resultSubject.getFingers().get(1).getImage();
        int imageQuantity = resultSubject.getFingers().get(1).getObjects().get(0).getQuality();
        byte[] prod = prodTokenImage.save(NImageFormat.getPNG()).toByteArray();

        // step 4: extract matching score
        List<NMatchingResult> matchingResults = prodSubject.getMatchingResults();

        // step 5: register identification data with DB
        Identify identify = identifyService.createIdentification(ImageFormat.PNG, imageQuantity, prod, matchingResults);

        // step 6: return results
        for (int i = 0; i < matchingResults.size(); i++) {
            Subject subject = subjectService.findById(Long.parseLong(matchingResults.get(i).getId()));
            BioGraphy bioGraphy = BioGraphy.builder()
                    .nid(subject.getNid().getValue())
                    .gender(subject.getGender())
                    .firstName(subject.getName().getFirstName())
                    .lastName(subject.getName().getLastName())
                    .birthDate(subject.getBirthDate())
                    .build();
            List<Image> imageList = new ArrayList<>();
            for (int j = 0; j < subject.getSubjectImages().size(); j++) {
                Image image = Image.builder()
                        .quality(subject.getSubjectImages().get(j).getImageInfo().getImageQuality())
                        .bioType(subject.getSubjectImages().get(j).getImageInfo().getBioType())
                        .pose(subject.getSubjectImages().get(j).getImageInfo().getPose())
                        .format(subject.getSubjectImages().get(j).getImageInfo().getFormat())
                        .base64Image(subject.getSubjectImages().get(j).getImageInfo().getImageByBase64())
                        .enabled(true)
                        .build();
                imageList.add(image);
            }
            CandidateListItem candidateListItem = CandidateListItem.builder()
                    .subjectId(Long.parseLong(matchingResults.get(i).getId()))
                    .score(matchingResults.get(i).getScore())
                    .bioGraphy(bioGraphy)
                    .image(imageList)
                    .build();
            candidateList.add(candidateListItem);
        }

        identifyList = FXCollections.observableList(candidateList);
        viewIdentify.setItems(identifyList);
    }

    private NSubject createSubject(String prob, ImageFormat imageFormat) {
        NSubject fingerSubject = new NSubject();
        NBiometricTask task = null;

        NImage image = NImageUtils.base64StringToNImage(prob, String.valueOf(imageFormat));
        NFinger finger = new NFinger();
        finger.setImage(image);
        fingerSubject.getFingers().add(finger);
        task = client.createTask(EnumSet.of(NBiometricOperation.DETECT_SEGMENTS, NBiometricOperation.ASSESS_QUALITY, NBiometricOperation.CREATE_TEMPLATE), fingerSubject);
        client.performTask(task);

        return fingerSubject;
    }

    private void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            cbbScan.getItems().add((NFingerScanner) device);
        }
        NFingerScanner scanner = (NFingerScanner) client.getFingerScanner();
        if (scanner == null) {
            cbbScan.getSelectionModel().selectFirst();
        } else if (scanner != null) {
            cbbScan.getSelectionModel().select(scanner);
        }
    }

    public void show() {
        stage.show();
    }

    private void findAll() {
        subjects = subjectService.findAll();
        for (Subject subject : subjects) {
            results.add(new SubjectDto.SubjectRes(subject));
        }
        data = FXCollections.observableArrayList(results);
    }

    private void updateFingersTools() {
        client.reset();
        client.setUseDeviceManager(true);
    }

    private class SegmentHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask task, final Object attachment) {
            Platform.runLater(() -> {
                scanning = false;
                updateShownImage();
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            Platform.runLater(() -> {
                scanning = false;
                updateShownImage();
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setContentText("Please selecte camera from the list");
                a.setHeaderText("No camera selected");
                a.show();
            });
        }
    }

    private void updateShownImage() {
        viewFinger.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
    }

    private NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for (NLRecord record : template.getFaces().getRecords()) {
                System.out.println(record.getQuality()
                );
            }
        }
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }
}
