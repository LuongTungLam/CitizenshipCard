package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.nxView.FaceViewLittle;
import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.biometrics.cmnd.common.tools.FaceTools;
import com.biometrics.cmnd.common.tools.FingersTools;
import com.biometrics.cmnd.identify.dto.IdentifyDto;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.BioTemplate;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.swing.NFingerViewBase;
import com.neurotec.devices.*;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Component
@FxmlView
public class LoginScan implements Initializable {

    @FXML
    private Button btnScan;
    @FXML
    private StackPane fingerLogin;
    @FXML
    private ComboBox cbbDeviceFingerScan;

    @Autowired
    private final SubjectService subjectService;

    @Autowired
    private final RecognitionService recognitionService;
    private final FingerViewLittle fingerViewLittle;
    private final FxWeaver fxWeaver;
    private NDeviceManager deviceManager;
    private NSubject subject;
    private List<SubjectDto.SubjectRes> results = new ArrayList<>();
    private Stage stage;
    private double x, y;
    private List<Subject> subjects;
    private boolean scanning;
    private final SegmentHandler segmentHandler = new SegmentHandler();

    public LoginScan(FxWeaver fxWeaver, SubjectService subjectService, RecognitionService recognitionService) {
        super();
        this.fxWeaver = fxWeaver;
        this.fingerViewLittle = new FingerViewLittle();
        this.subjectService = subjectService;
        this.recognitionService = recognitionService;
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        cbbDeviceFingerScan.valueProperty().addListener(new FingerSelectionListener());
        updateListDeviceFingerScan();
        fingerLogin.getChildren().add(fingerViewLittle);
        fingerLogin.setAlignment(Pos.CENTER);
        findAll();
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource() == btnScan) {
            updateFingersTools();
            NFinger finger = new NFinger();
            subject = new NSubject();
            subject.getFingers().add(finger);
            fingerViewLittle.setFinger(finger);
            fingerViewLittle.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
            NBiometricTask task = FingersTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);
            FingersTools.getInstance().getClient().performTask(task, null, segmentHandler);
            scanning = true;
        }
    }

    private void findAll() {
        subjects = subjectService.findAll();
        for (Subject subject : subjects) {
            results.add(new SubjectDto.SubjectRes(subject));
        }
    }

    private void updateListDeviceFingerScan() {
        for (NDevice device : deviceManager.getDevices()) {
            cbbDeviceFingerScan.getItems().add(device);
        }
        NFingerScanner fingerScanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if (fingerScanner == null) {
            cbbDeviceFingerScan.getSelectionModel().selectFirst();
        } else if (fingerScanner != null) {
            cbbDeviceFingerScan.getSelectionModel().select(fingerScanner);
        }
    }

    private class FingerSelectionListener implements ChangeListener<NFingerScanner> {
        @Override
        public void changed(ObservableValue<? extends NFingerScanner> observableValue, NFingerScanner nFingerScanner, NFingerScanner t1) {
            FingersTools.getInstance().getClient().setFingerScanner(t1);
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

    protected void updateFingersTools() {
        FingersTools.getInstance().getClient().reset();
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        FingersTools.getInstance().getClient().setFingersReturnBinarizedImage(true);
    }

    private void updateShownImage() {
        fingerViewLittle.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
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
                    try {
                        fxWeaver.loadController(HomeBioSSO.class).show(res);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    close();
                } else {
                    return;
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

    protected NSubject createSubject(String prob, ImageFormat imageFormat) {
        NSubject fingerSubject = new NSubject();
        NBiometricTask task = null;

        NImage image = NImageUtils.base64StringToNImage(prob, String.valueOf(imageFormat));
        NFinger finger = new NFinger();
        finger.setImage(image);
        fingerSubject.getFingers().add(finger);
        task = FingersTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.DETECT_SEGMENTS, NBiometricOperation.ASSESS_QUALITY, NBiometricOperation.CREATE_TEMPLATE), fingerSubject);
        FingersTools.getInstance().getClient().performTask(task);

        return fingerSubject;
    }

    protected NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }

    private void close() {
        Stage stage = (Stage) btnScan.getScene().getWindow();
        stage.close();
    }
}
