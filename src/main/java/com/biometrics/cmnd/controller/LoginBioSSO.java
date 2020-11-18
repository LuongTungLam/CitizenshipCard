package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.nxView.FaceViewLittle;
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
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.*;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

@Component
@FxmlView
public class LoginBioSSO implements Initializable {

    @FXML
    private Button btnClose;

    @Autowired
    private final SubjectService subjectService;

    @Autowired
    private final RecognitionService recognitionService;

    @FXML
    private Button btnLogin, btnLoginScan, btnLoginFace;

    @FXML
    private ComboBox cbbDeviceCamera;

    @FXML
    private StackPane faceLogin;

    @FXML
    private AnchorPane apLogin;

    @FXML
    private BorderPane bpLogin;

    @FXML
    private StackPane paneChangeLogin;

    private final FaceViewLittle faceViewLittle;
    private final FxWeaver fxWeaver;
    private NDeviceManager deviceManager;
    private NSubject subject;
    private boolean capturing;
    private List<SubjectDto.SubjectRes> results = new ArrayList<>();
    private List<Subject> subjects;
    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();
    private Stage stage;
    private double x, y;

    public LoginBioSSO(FxWeaver fxWeaver, SubjectService subjectService, RecognitionService recognitionService) {
        super();
        this.fxWeaver = fxWeaver;
        this.faceViewLittle = new FaceViewLittle();
        this.subjectService = subjectService;
        this.recognitionService = recognitionService;
        FaceTools.getInstance().getClient().setUseDeviceManager(true);
//        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        deviceManager = FaceTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        deviceManager.initialize();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        cbbDeviceCamera.valueProperty().addListener(new CameraSelectionListener());
        updateScannerList();
        faceLogin.getChildren().add(faceViewLittle);
        faceLogin.setAlignment(Pos.CENTER);
        findAll();
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource() == btnLoginFace) {
            bpLogin.setCenter(paneChangeLogin);
        }

        if (event.getSource() == btnClose) {
            System.exit(0);
        }
        if (event.getSource() == btnLoginScan) {
            loadPane("LoginScan");
        }
        if (event.getSource() == btnLogin) {
            loginFace();
        }


    }

    private void loadPane(String s) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(s + ".fxml"));
        if (s.equals("LoginScan")){
            Map<Class, Callable<?>> creators = new HashMap<>();
            creators.put(LoginScan.class, new Callable<LoginScan>() {
                @Override
                public LoginScan call() throws Exception {
                    return new LoginScan(fxWeaver,subjectService,recognitionService);
                }
            });

            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> param) {
                    Callable<?> callable = creators.get(param);
                    if (callable == null) {
                        try {
                            return param.newInstance();
                        } catch (InstantiationException | IllegalAccessException ex) {
                            throw new IllegalStateException(ex);
                        }
                    } else {
                        try {
                            return callable.call();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            });
        }
        Parent root = loader.load();
        bpLogin.setCenter(root);
    }


    private void loginFace() throws MalformedURLException {
        updateFaceTools();
        NFace face = new NFace();
//        FaceTools.getInstance().getClient().setFacesLivenessMode(NLivenessMode.NONE);
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.STREAM));
        subject = new NSubject();
        subject.getFaces().add(face);
        faceViewLittle.setFace(face);
        capturing = true;

        NBiometricTask task = FaceTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.SEGMENT, NBiometricOperation.CREATE_TEMPLATE), subject);
        FaceTools.getInstance().getClient().performTask(task, null, captureCompletionHandler);
    }

    private NSubject createSubject(String prob, ImageFormat imageFormat) {
        NSubject faceSB = new NSubject();
        NBiometricTask task = null;

        NImage image = NImageUtils.base64StringToNImage(prob, String.valueOf(imageFormat));
        NFace face = new NFace();
        face.setImage(image);
        faceSB.getFaces().add(face);
        task = FaceTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.DETECT_SEGMENTS, NBiometricOperation.ASSESS_QUALITY, NBiometricOperation.CREATE_TEMPLATE), faceSB);
        FaceTools.getInstance().getClient().performTask(task);

        return faceSB;
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

    private void updateFaceTools() {
        NBiometricClient client = FaceTools.getInstance().getClient();
        client.setUseDeviceManager(true);
    }

    private void close() {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.close();
    }

    public void updateScannerList() {
        for (NDevice device : deviceManager.getDevices()) {
            cbbDeviceCamera.getItems().add(device);
        }
        NCamera camera = FaceTools.getInstance().getClient().getFaceCaptureDevice();
        if (camera == null) {
            cbbDeviceCamera.getSelectionModel().selectFirst();
        } else if (camera != null) {
            cbbDeviceCamera.getSelectionModel().select(camera);
        }

    }


    private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask task, final Object attachment) {
            if (task.getError() != null) {
                failed(task.getError(), attachment);
                return;
            }
            final NBiometricStatus status = task.getStatus();
            if (status == NBiometricStatus.OK) {
                faceViewLittle.setFace(subject.getFaces().get(1));
                updateFaceTools();
                FaceTools.getInstance().getClient().forceStart();

                if (!subject.getFaces().isEmpty()) {
                    IdentifyDto.Req req = IdentifyDto.Req.builder()
                            .imageFormat(ImageFormat.PNG)
                            .prob(NImageUtils.NImageToBase64String(faceViewLittle.getFace().getImage()))
                            .build();

                    List<NSubject> referenceSubjects = new ArrayList<>();
                    // step 1: get bioTemplate all of subject
                    for (int i = 0; i < results.size(); i++) {
                        for (int j = 0; j < results.get(i).getSubjectImages().size(); j++) {
                            if (results.get(i).getSubjectImages().get(j).getImageInfo().getBioType().equals(BioType.FACE)) {
                                BioTemplate bioTemplate = results.get(i).getSubjectImages().get(j).getSubject().getBioTemplate();
                                NSubject referenceSubject = createSubject(bioTemplate.getTemplate(), String.valueOf(bioTemplate.getSubject().getId()));
                                referenceSubjects.add(referenceSubject);
                            }
                        }
                    }

                    // step 2: create prob subject
                    NSubject prodSubject = createSubject(req.getProb(), req.getImageFormat());
                    // step 2: identification
                    NSubject resultSubject = recognitionService.identifyFace(prodSubject, referenceSubjects);
                    // step 4: extract matching score
                    List<NMatchingResult> matchingResults = prodSubject.getMatchingResults();
                    List<Integer> score = new ArrayList<>();
                    for (int i = 0; i < matchingResults.size(); i++) {
                        score.add(matchingResults.get(i).getScore());
                        if (matchingResults.get(i).getScore() >= Collections.max(score) && matchingResults.get(i).getScore() > 60) {
                            Subject subject = subjectService.findById(Long.parseLong(matchingResults.get(i).getId()));
                            SubjectDto.SubjectRes res = new SubjectDto.SubjectRes(subject);
                            Platform.runLater(() -> {
                                try {
                                    fxWeaver.loadController(HomeBioSSO.class).show(res);
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                close();
                            });
                        } else {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Login Failed");
                                alert.setHeaderText("Not Found Face !");
                                alert.showAndWait();
                            });
                        }
                    }
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    capturing = false;
                }

            });
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    capturing = false;
                }

            });
        }

    }

    private class CameraSelectionListener implements ChangeListener<NCamera> {
        @Override
        public void changed(ObservableValue<? extends NCamera> observableValue, NCamera oldCamera, NCamera newCamera) {
            if (newCamera != null) {
                FaceTools.getInstance().getClient().setFaceCaptureDevice(newCamera);
            }
        }
    }

    private void findAll() {
        subjects = subjectService.findAll();
        for (Subject subject : subjects) {
            results.add(new SubjectDto.SubjectRes(subject));
        }
    }

    public void show() {
        subject = new NSubject();
        faceViewLittle.setFace(null);
        Scene scene = new Scene(apLogin);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        apLogin.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });
        apLogin.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - x);
            stage.setY(e.getScreenY() - y);
        });
        stage.show();
    }
}
