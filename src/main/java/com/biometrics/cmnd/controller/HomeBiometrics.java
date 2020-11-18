package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.subject.service.SubjectService;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.JFXTooltip;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

@Component
@FxmlView("HomeBiometrics.fxml")
public class HomeBiometrics implements Initializable {

    @FXML
    private AnchorPane homepage, anchorView;

    @FXML
    private BorderPane borderView, borderPane;

    @FXML
    private Button btnMenu, btnHome, btnCreate, btnIdentify, btnRefresh, btnSetting, btnHelp, btnNotify;

    private final SubjectService subjectService;
    private final FxWeaver fxWeaver;
    private Stage stage;
    private double x, y;
    private JFXPopup ppMenu, ppSetting;

    public HomeBiometrics(FxWeaver fxWeaver, SubjectService subjectService) {
        super();
        this.fxWeaver = fxWeaver;
        this.subjectService = subjectService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        borderPane = new BorderPane();
        tooltipsControl();
        popupControl();
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        if (event.getSource() == btnMenu) {
            ppMenu.show(btnMenu, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, -12, 15);
        } else if (event.getSource() == btnHome) {
            borderView.setCenter(anchorView);
        } else if (event.getSource() == btnCreate) {
            loadPage("CreateSubject");
        } else if (event.getSource() == btnIdentify) {

        } else if (event.getSource() == btnRefresh) {

        }
        if (event.getSource() == btnSetting) {
            ppSetting.show(btnSetting, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, -12, 15);
        }
    }

    private void loadPage(String page) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(page + ".fxml"));
        if (page.equals("CreateSubject")) {
            Map<Class, Callable<?>> creators = new HashMap<>();
            creators.put(CreateSubject.class, new Callable<CreateSubject>() {
                @Override
                public CreateSubject call() throws Exception {
                    return new CreateSubject(subjectService);
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
        borderView.setCenter(root);
    }

    @FXML
    private void handleMouseEvent(MouseEvent event) {

    }

    public void show() {
        stage.setScene(new Scene(homepage));
        stage.initStyle(StageStyle.UNDECORATED);
        homepage.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });
        homepage.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - x);
            stage.setY(e.getScreenY() - y);
        });
        stage.show();
    }

    public final class ContactPopup implements Initializable {
        @FXML
        private JFXListView<?> menuPopup;

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            menuPopup.setOnMouseClicked(click -> {
                if (menuPopup.getSelectionModel().getSelectedIndex() == 1) {
                    System.exit(0);
                }
            });
        }
    }

    public final class SettingPopup implements Initializable {
        @FXML
        private Pane settingPopup;

        @FXML
        private JFXToggleButton btnBackground;

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            btnBackground.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                    if (btnBackground.isSelected() == true) {
                        btnBackground.setText("ON");
                        homepage.setStyle("-fx-background-color: #000");
                    } else {
                        btnBackground.setText("OFF");
                        homepage.setStyle("-fx-background-color: #fff");
                    }
                }
            });
        }
    }

    private void tooltipsControl() {
        Tooltip tlRefresh = new Tooltip("Refresh");
        tlRefresh.setShowDelay(Duration.seconds(0.1));
        btnRefresh.setTooltip(tlRefresh);

        Tooltip tlHome = new Tooltip("Home");
        tlHome.setShowDelay(Duration.seconds(0.1));
        btnHome.setTooltip(tlHome);

        Tooltip tlCreate = new Tooltip("Create");
        tlCreate.setShowDelay(Duration.seconds(0.1));
        btnCreate.setTooltip(tlCreate);

        Tooltip tlIdentify = new Tooltip("Identify");
        tlIdentify.setShowDelay(Duration.seconds(0.1));
        btnIdentify.setTooltip(tlIdentify);

        Tooltip tlMenu = new Tooltip("Menu");
        tlMenu.setShowDelay(Duration.seconds(0.1));
        btnMenu.setTooltip(tlMenu);
    }

    private void popupControl() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ContactPopup.fxml"));
            loader.setController(new ContactPopup());
            ppMenu = new JFXPopup(loader.load());

            FXMLLoader loaderSt = new FXMLLoader(getClass().getResource("SettingPopup.fxml"));
            loaderSt.setController(new SettingPopup());
            ppSetting = new JFXPopup(loaderSt.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
