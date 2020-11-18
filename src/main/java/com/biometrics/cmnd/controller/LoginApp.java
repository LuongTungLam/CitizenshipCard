package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.biometrics.cmnd.identify.dto.IdentifyDto;
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
import com.neurotec.io.NBuffer;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Component
@FxmlView("LoginApp.fxml")
public class LoginApp implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    @Autowired
    private final RecognitionService recognitionService;

    @Autowired
    private final IdentifyService identifyService;

    private final FxWeaver fxWeaver;

    @FXML
    private AnchorPane loginPane;
    @FXML
    private Button scan, login,face;
    @FXML
    private StackPane viewFinger;
    @FXML
    private ComboBox listLiveScan,listCamera;

    private final NDeviceManager deviceManager;
    private final NBiometricClient client;
    private NSubject subject;
    private final FingerViewLittle viewFingers;
    private boolean scanning;
    private final SegmentHandler segmentHandler = new SegmentHandler();
    private List<SubjectDto.SubjectRes> results = new ArrayList<>();
    private List<Subject> subjects;
    private Stage stage;

    public LoginApp(NBiometricClient client, SubjectService subjectService, RecognitionService recognitionService, IdentifyService identifyService, FxWeaver fxWeaver) {
        super();
        this.viewFingers = new FingerViewLittle();
        this.subjectService = subjectService;
        this.recognitionService = recognitionService;
        this.identifyService = identifyService;
        this.client = client;
        client.setUseDeviceManager(true);
        deviceManager = client.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
        this.subject = new NSubject();
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        listLiveScan.valueProperty().addListener(new LoginApp.FingerSelectionListener());
        this.viewFinger.getChildren().add(viewFingers);
        this.viewFinger.setAlignment(Pos.CENTER);
        updateScannerList();
        findAll();
    }

    public void scan(ActionEvent event) {
        updateFingersTools();
        NFinger finger = new NFinger();
        subject = new NSubject();
        subject.getFingers().add(finger);
        viewFingers.setFinger(finger);
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);
        client.performTask(task, null, segmentHandler);
        scanning = true;
    }

    @FXML
    public void face(ActionEvent event) {
    }

    @FXML
    public void login(ActionEvent event) throws MalformedURLException {
        if (!subject.getFingers().isEmpty()) {
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

            // step 4: extract matching score
            List<NMatchingResult> matchingResults = prodSubject.getMatchingResults();
            List<Integer> score = new ArrayList<>();
            for (int i = 0; i < matchingResults.size(); i++) {
                score.add(matchingResults.get(i).getScore());
                if (matchingResults.get(i).getScore() >= Collections.max(score) && matchingResults.get(i).getScore() > 100) {
                    Subject subject = subjectService.findById(Long.parseLong(matchingResults.get(i).getId()));
                    SubjectDto.SubjectRes res = new SubjectDto.SubjectRes(subject);
                    fxWeaver.loadController(AllSubject.class).show(res);
                    close();
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Login Failed");
//                        alert.setHeaderText("Not Found Finger !");
                        alert.showAndWait();
                    });
                }
            }
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Please Scan !!");
                alert.showAndWait();
            });
        }
    }

    private void close() {
        Stage stage = (Stage) login.getScene().getWindow();
        stage.close();
    }

    private void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            listLiveScan.getItems().add((NFingerScanner) device);
        }
        NFingerScanner scanner = (NFingerScanner) client.getFingerScanner();
        if (scanner == null) {
            listLiveScan.getSelectionModel().selectFirst();
        } else if (scanner != null) {
            listLiveScan.getSelectionModel().select(scanner);
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

    private NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
//        if (template.getFaces() != null) {
//            for (NLRecord record : template.getFaces().getRecords()) {
//                System.out.println(record.getQuality()
//                );
//            }
//        }
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }

    private void updateShownImage() {
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
    }

    private void updateFingersTools() {
        client.reset();
        client.setUseDeviceManager(true);
    }

    private void findAll() {
        subjects = subjectService.findAll();
        for (Subject subject : subjects) {
            results.add(new SubjectDto.SubjectRes(subject));
        }
    }

    public void show() {
        subject = new NSubject();
        viewFingers.setFinger(null);
        stage.setScene(new Scene(loginPane));
        stage.show();
    }
}
