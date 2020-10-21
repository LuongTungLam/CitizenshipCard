package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.nxView.FingerViewLittle;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFingerViewBase;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.images.NImageRotateFlipType;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@Component
@FxmlView("CitizenshipBack.fxml")

public class CitizenshipBack implements Initializable {
    @FXML
    private Button scan, save;
    @FXML
    private StackPane leftLittle, leftRing, leftMiddle, leftIndex, leftThumb, rightLittle, rightRing, rightMiddle, rightIndex, rightThumb, viewFinger;
    @FXML
    private ComboBox fingerBox;
    @FXML
    private Label missingFinger;

    private NImageRotateFlipType rotateFlipType;
    private NSubject subject;
    private final FingerViewLittle viewFingers, viewLeftLittle, viewLeftRing, viewLeftMiddle, viewLeftIndex, viewLeftThumb, viewRightLittle, viewRightRing, viewRightMiddle, viewRightIndex, viewRightThumb;
    private final NDeviceManager deviceManager;
    private final NBiometricClient client;
    private final SegmentHandler segmentHandler = new SegmentHandler();
    private boolean scanning;

    private List<NFinger> fingerImage = new ArrayList<>();

    public CitizenshipBack(NBiometricClient client) {
        super();
        this.client = client;
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
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fingerBox.valueProperty().addListener(new FingerSelectionListener());
        this.viewFinger.getChildren().add(viewFingers);
        this.viewFinger.setAlignment(Pos.CENTER);
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
    }

    @FXML
    private ListView<NFPosition> listPositions;

    private void initFingerPositions() {
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
        listPositions.getItems().addAll(positions);
        listPositions.getSelectionModel().selectFirst();
    }

    @FXML
    public void roll(ActionEvent event) {
        NFinger finger = new NFinger();
        NFImpressionType roll = NFImpressionType.LIVE_SCAN_OPTICAL_CONTACT_ROLLED;
//        roll.add(NImageRotateFlipType.ROTATE_180_FLIP_NONE);
        finger.setImpressionType(roll);
        subject = new NSubject();
        subject.getFingers().add(finger);
        viewFingers.setFinger(finger);
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        segment();
    }

    @FXML
    public void save(ActionEvent event) {

    }

    @FXML
    public void scan(ActionEvent event) {
        NFinger finger = new NFinger();
        subject = new NSubject();
        subject.getFingers().add(finger);
        viewFingers.setFinger(finger);
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
        segment();
    }

    private void segment() {
        client.setFingersDeterminePatternClass(true);
        client.setFingersCalculateNFIQ(true);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CAPTURE,NBiometricOperation.CREATE_TEMPLATE, NBiometricOperation.SEGMENT, NBiometricOperation.ASSESS_QUALITY), subject);
        client.performTask(task, null, segmentHandler);
        scanning = true;
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
        if (fingerImage.size() >= 10){
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Count Finger Print");
                alert.setHeaderText("Count Finger less than 10");
                alert.showAndWait();
            });
            return;
        }
        if (subject.getFingers().get(1).getStatus() == NBiometricStatus.OK) {
            int indexFinger = fingerImage.size();
            switch (indexFinger) {
                case 0:
//                    subject.getFingers().get(0).setPosition(NFPosition.LEFT_LITTLE_FINGER);
                    setSegmentInfo(subject.getFingers().get(1),NFPosition.LEFT_LITTLE_FINGER, viewLeftLittle);
                    break;
                case 1:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.LEFT_RING_FINGER,viewLeftRing);
                    break;
                case 2:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.LEFT_MIDDLE_FINGER,viewLeftMiddle);
                    break;
                case 3:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.LEFT_INDEX_FINGER,viewLeftIndex);
                    break;
                case 4:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.LEFT_THUMB,viewLeftThumb);
                    break;
                case 5:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.RIGHT_THUMB,viewRightThumb);
                    break;
                case 6:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.RIGHT_INDEX_FINGER,viewRightIndex);
                    break;
                case 7:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.RIGHT_MIDDLE_FINGER,viewRightMiddle);
                    break;
                case 8:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.RIGHT_RING_FINGER,viewRightRing);
                    break;
                case 9:
                    setSegmentInfo(subject.getFingers().get(1), NFPosition.RIGHT_LITTLE_FINGER,viewRightLittle);
                    break;
            }
        }
        fingerImage.add(subject.getFingers().get(1));

        if (fingerImage.size() >= 10){
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Count Finger Print");
                alert.setHeaderText("Count Finger less than 10");
                alert.showAndWait();
            });
        }
    }

    private void setSegmentInfo(NFinger finger,NFPosition position, FingerViewLittle fingerView) {
        finger.setPosition(position);
        fingerView.setFinger(finger);
    }

    private void updateShownImage() {
        viewFingers.setShownImage(NFingerViewBase.ShownImage.ORIGINAL);
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

}
