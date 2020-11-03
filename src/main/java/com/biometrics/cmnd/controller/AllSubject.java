package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.neurotec.biometrics.client.NBiometricClient;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("AllSubject.fxml")
public class AllSubject implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    private final FxWeaver fxWeaver;

    @FXML
    private ImageView iconView, iconRefresh, iconSearch, imageUser, iconLogout;
    @FXML
    private TableView<SubjectDto.SubjectRes> tableSubject;
    @FXML
    private Button add, refresh, search, logout;
    @FXML
    private AnchorPane rootPane, allSubjectPane,userPane;
    @FXML
    private Label nameUser;

    @FXML private VBox statusContainer;

    private TranslateTransition showStatus;
    private TranslateTransition hideStatus;
    private boolean showsStatus = false;
    private static final int AUTO_HIDE_DEALY = 5;

    private final NBiometricClient client;
    private Stage stage;

    private ObservableList data;

    public AllSubject(SubjectService subjectService, NBiometricClient client, FxWeaver fxWeaver) {
        super();
        this.fxWeaver = fxWeaver;
        this.subjectService = subjectService;
        this.client = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        stage.setScene(new Scene(allSubjectPane));
        File add = new File("uploads/icons/add.png");
        iconView.setImage(new Image(add.toURI().toString()));
        File refresh = new File("uploads/icons/reload.png");
        iconRefresh.setImage(new Image(refresh.toURI().toString()));
        File search = new File("uploads/icons/magnifiying-glass.png");
        iconSearch.setImage(new Image(search.toURI().toString()));
        File logout = new File("uploads/icons/logout.png");
        iconLogout.setImage(new Image(logout.toURI().toString()));
        listSubject();
    }

    public void listSubject() {
        tableSubject.getItems().clear();
        List<Subject> subjectList = subjectService.findAll();
        List<SubjectDto.SubjectRes> results = new ArrayList<>();
        for (Subject subject : subjectList) {
            results.add(new SubjectDto.SubjectRes(subject));
        }

//        data = FXCollections.observableArrayList(subjectList);
        data = FXCollections.observableArrayList(results);

        TableColumn<SubjectDto.SubjectRes, Integer> id = new TableColumn<>("Id");
        id.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, Integer> id) {
                return new SimpleObjectProperty(id.getValue().getSubjectId());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> created_by = new TableColumn<>("Created By");
        created_by.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> created) {
                return new SimpleObjectProperty(created.getValue().getCreatedBy());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> city = new TableColumn<>("City");
        city.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> city) {
                return new SimpleObjectProperty<>(city.getValue().getContact().getAddress().getCity());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> zip = new TableColumn<>("Zip");
        zip.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> zip) {
                return new SimpleObjectProperty<>(zip.getValue().getContact().getAddress().getZip());
            }
        });

        TableColumn<SubjectDto.SubjectRes, Date> birth_date = new TableColumn<>("Birth Date");
        birth_date.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, Date> birthday) {
                return new SimpleObjectProperty(birthday.getValue().getBioGraphy().getBirthDate());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> email) {
                return new SimpleStringProperty(email.getValue().getContact().getEmail());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> gender = new TableColumn<>("Gender");
        gender.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> gender) {
                return new SimpleObjectProperty(gender.getValue().getBioGraphy().getGender());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> first_name = new TableColumn<>("First Name");
        first_name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> first_name) {
                return new SimpleObjectProperty<>(first_name.getValue().getBioGraphy().getFirstName());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> last_name = new TableColumn<>("Last Name");
        last_name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> last_name) {
                return new SimpleObjectProperty<>(last_name.getValue().getBioGraphy().getLastName());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> nid = new TableColumn<>("Nid");
        nid.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> nid) {
                return new SimpleObjectProperty<>(nid.getValue().getBioGraphy().getNid());
            }
        });

        TableColumn<SubjectDto.SubjectRes, String> phone_number = new TableColumn<>("Phone Number");
        phone_number.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> phone) {
                return new SimpleObjectProperty<>(phone.getValue().getContact().getPhoneNumber());
            }
        });


        tableSubject.getColumns().setAll(id, created_by, first_name, last_name, city, zip, birth_date, email, gender, nid, phone_number);
        tableSubject.setItems(data);

        ContextMenu menu = new ContextMenu();
        MenuItem detail = new MenuItem("Detail");
        MenuItem delete = new MenuItem("Delete");
        menu.getItems().addAll(detail, delete);

        detail.setOnAction(next -> {
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("DetailSubject.fxml"));
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                DetailSubject detailSubject = loader.getController();
                Subject subject = subjectService.findById(tableSubject.getSelectionModel().getSelectedItem().getSubjectId());
                SubjectDto.SubjectRes res = new SubjectDto.SubjectRes(subject);
                detailSubject.setSubject(res);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        delete.setOnAction(deleteRow -> {
            Subject subject = subjectService.findById(tableSubject.getSelectionModel().getSelectedItem().getSubjectId());
            subjectService.delete(subject);
            refreshTableView();
        });

        tableSubject.setRowFactory(click -> {
            TableRow<SubjectDto.SubjectRes> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty() && mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.getClickCount() == 1) {
                    menu.show(tableSubject, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            });
            return row;
        });
    }

    @FXML
    private void add(ActionEvent event) {
        fxWeaver.loadController(CitizenshipBack.class).show();
    }

    @FXML
    public void refreshTableView() {
        listSubject();
    }

    @FXML
    private void search(ActionEvent event) {
        fxWeaver.loadController(IdentifyFinger.class).show();
    }

    @FXML
    public void logout(ActionEvent event){
        nameUser = new Label();
        imageUser = new ImageView();
        Stage stage = (Stage) logout.getScene().getWindow();
        fxWeaver.loadController(LoginApp.class).show();
        stage.close();
    }

    public void show(SubjectDto.SubjectRes subject) throws MalformedURLException {
        nameUser.setText(String.valueOf(subject.getBioGraphy().getFirstName()) + " " + subject.getBioGraphy().getLastName());
        for (int i = 0; i < subject.getSubjectImages().size(); i++) {
            if (subject.getSubjectImages().get(i).getImageInfo().getBioType().equals(BioType.FACE)) {
                File file = new File("uploads" + subject.getSubjectImages().get(i).getImageInfo().getImageUrl());
                Image image = new Image(file.toURI().toURL().toExternalForm(), 180, 250, false, true);
                imageUser.setImage(image);
            }
        }
        userPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                StackPane root = new StackPane();
                Label label = new Label("Your are now in the second form");
                root.getChildren().add(label);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            }
        });
        stage.show();
    }

}
