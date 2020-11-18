package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.CityRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.CountryRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.DistrictRepository;
import com.biometrics.cmnd.subject.coutryDB.repositoryDB.ProvinceRepository;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.jfoenix.controls.*;
import com.neurotec.biometrics.client.NBiometricClient;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

@Component
@FxmlView
public class HomeBioSSO implements Initializable {
    @Autowired
    private final SubjectService subjectService;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final NBiometricClient client;
    @FXML
    private AnchorPane home, anchorView;
    @FXML
    private StackPane spDialog;
    @FXML
    private TableView<SubjectDto.SubjectRes> tableProfile;

    @FXML
    private TableColumn<SubjectDto.SubjectRes, String> tcName, tcNumberCard, tcGender, tcAddress, tcPhone, tcEmail, tcCreatedBy;

    @FXML
    private TableColumn<SubjectDto.SubjectRes, Void> tcBtn;

    @FXML
    private Button btnCreate, btnRefresh, btnHome, btnIdentify, btnHelp;

    @FXML
    private BorderPane borderView;

    @FXML
    private Pagination pagination;

    @FXML
    private JFXComboBox filterData;

    @FXML
    private Button btnUser;

    @FXML
    private StackPane spUser;

    @FXML
    private ImageView ivUser;

    @FXML
    private JFXTextField tfFilter;

    private ObservableList<SubjectDto.SubjectRes> data = FXCollections.observableArrayList();
    private FilteredList<SubjectDto.SubjectRes> filteredData;
    private double x, y;
    private final FxWeaver fxWeaver;
    private Stage stage;
    private JFXPopup ppSetting;

    public HomeBioSSO(FxWeaver fxWeaver, NBiometricClient client, SubjectService subjectService, CountryRepository countryRepository, CityRepository cityRepository, ProvinceRepository provinceRepository, DistrictRepository districtRepository) {
        super();
        this.client = client;
        this.fxWeaver = fxWeaver;
        this.subjectService = subjectService;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        listProfile();
        database();
        paginationTable();
        showingProfile();
        popupControl();
        Rectangle clip = new Rectangle(
                ivUser.getFitWidth(), ivUser.getFitHeight()
        );
        clip.setArcWidth(50);
        clip.setArcHeight(50);
        ivUser.setClip(clip);
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        if (event.getSource() == btnHome) {
            borderView.setCenter(anchorView);
        }
        if (event.getSource() == btnCreate) {
            loadPane("NewProfileBioSSO");
            showDialog();
        }
        if (event.getSource() == btnRefresh) {
            database();
            filterData.getSelectionModel().select(1);
        }

        if (event.getSource().equals(btnUser)) {
            ppSetting.show(btnUser, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, -90, 45);
        }

        if (event.getSource().equals(btnHelp)) {
            showDialog();
        }
    }

    private void showDialog() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("Read before create profile"));
        content.setBody(new Text("Please scanning from Right hand to Left hand,\n"
        + "from Thumb Fingerprint to Little Fingerprint"));
        JFXDialog dialog = new JFXDialog(spDialog, content, JFXDialog.DialogTransition.CENTER);
        dialog.show();
    }

    private void showingProfile() {

    }

    private void database() {
        tableProfile.getItems().removeAll();
        List<Subject> subjectList = subjectService.findAll();
        List<SubjectDto.SubjectRes> results = new ArrayList<>();
        for (Subject subject : subjectList) {
            results.add(new SubjectDto.SubjectRes(subject));
        }
        data = FXCollections.observableArrayList(results);
        filteredData = new FilteredList<>(data, tab -> true);
    }

    private void paginationTable() {
        tfFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(
                    profile -> newValue == null || newValue.toString().isEmpty() || profile.getBioGraphy().getFirstName().toLowerCase().contains(newValue.toString().toLowerCase()) || profile.getBioGraphy().getLastName().toLowerCase().contains(newValue.toString().toLowerCase())
                            || profile.getContact().getEmail().toLowerCase().contains(newValue.toString().toLowerCase()) || profile.getBioGraphy().getGender().toString().toLowerCase().contains(newValue.toString().toLowerCase()) || profile.getContact().getAddress().getCountry().toLowerCase().contains(newValue.toString().toLowerCase())
                            || profile.getContact().getAddress().getCity().toLowerCase().contains(newValue.toString().toLowerCase()) || profile.getContact().getAddress().getProvince().toLowerCase().contains(newValue.toString().toLowerCase()) || profile.getContact().getAddress().getDistrict().toLowerCase().contains(newValue.toString().toLowerCase())
                            || profile.getContact().getAddress().getStreet().toLowerCase().contains(newValue.toString().toLowerCase())
            );
            changeTableView(pagination.getCurrentPageIndex(), 9);
        });
//        filterData.setItems(listShowing);
        filterData.getItems().addAll("1", "9");

        filterData.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            if (t1.intValue() == 0) {
                int rowsPage = 1;
                int totalProfile = (int) (Math.ceil(data.size() * 1.0 / rowsPage));
                pagination.setPageCount(totalProfile);
                pagination.setCurrentPageIndex(0);
                changeTableView(0, rowsPage);
                pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> changeTableView(newValue.intValue(), rowsPage));
            }
            if (t1.intValue() == 1) {
                int rowsPage = 9;
                int totalProfile = (int) (Math.ceil(data.size() * 1.0 / rowsPage));
                pagination.setPageCount(totalProfile);
                pagination.setCurrentPageIndex(0);
                changeTableView(0, rowsPage);
                pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> changeTableView(newValue.intValue(), rowsPage));
//                changeTableView(pagination.getCurrentPageIndex(), rowsPage);
            }
        });
        filterData.getSelectionModel().selectLast();
    }

    private void changeTableView(int index, int limit) {
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, data.size());
        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<SubjectDto.SubjectRes> sortedData = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableProfile.comparatorProperty());
        tableProfile.setItems(sortedData);
    }

    private void listProfile() {
        tcName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> fullName) {
                return new SimpleObjectProperty<>(fullName.getValue().getBioGraphy().getLastName() + " " + fullName.getValue().getBioGraphy().getFirstName());
            }
        });

        tcNumberCard.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> numberCard) {
                return new SimpleObjectProperty<>(numberCard.getValue().getBioGraphy().getNid());
            }
        });

        tcGender.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> gender) {
                return new SimpleObjectProperty(gender.getValue().getBioGraphy().getGender());
            }
        });

        tcAddress.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> address) {
                return new SimpleObjectProperty(
                        address.getValue().getContact().getAddress().getStreet()
                                + ", " + address.getValue().getContact().getAddress().getProvince()
                                + ", " + address.getValue().getContact().getAddress().getDistrict()
                                + ", " + address.getValue().getContact().getAddress().getCity()
                                + ", " + address.getValue().getContact().getAddress().getCountry()
                );
            }
        });

        tcPhone.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> phone) {
                return new SimpleObjectProperty<>(phone.getValue().getContact().getPhoneNumber());
            }
        });
        tcEmail.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> email) {
                return new SimpleObjectProperty<>(email.getValue().getContact().getEmail());
            }
        });
        tcCreatedBy.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SubjectDto.SubjectRes, String> createdBy) {
                return new SimpleObjectProperty<>(createdBy.getValue().getCreatedBy());
            }
        });

        tcBtn.setCellFactory(new Callback<TableColumn<SubjectDto.SubjectRes, Void>, TableCell<SubjectDto.SubjectRes, Void>>() {
            @Override
            public TableCell<SubjectDto.SubjectRes, Void> call(TableColumn<SubjectDto.SubjectRes, Void> param) {
                final TableCell<SubjectDto.SubjectRes, Void> cell = new TableCell<SubjectDto.SubjectRes, Void>() {
                    private final Button button = new Button();

                    {
                        button.setGraphic(new FontIcon("fas-edit"));
                        button.setStyle("-fx-background-color: white");
                        button.setOnAction((ActionEvent event) -> {

                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
                return cell;
            }
        });
    }

    private void loadPane(String pane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(pane + ".fxml"));
        if (pane.equals("NewProfileBioSSO")) {
            Map<Class, Callable<?>> creators = new HashMap<>();
            creators.put(NewProfileBioSSO.class, new Callable<NewProfileBioSSO>() {
                @Override
                public NewProfileBioSSO call() throws Exception {
                    return new NewProfileBioSSO(subjectService, client, countryRepository, cityRepository, provinceRepository, districtRepository);
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

    public void show(SubjectDto.SubjectRes subject) throws MalformedURLException {
        btnUser.setText(String.valueOf(subject.getBioGraphy().getFirstName()));
        for (int i = 0; i < subject.getSubjectImages().size(); i++) {
            if (subject.getSubjectImages().get(i).getImageInfo().getBioType().equals(BioType.FACE)) {
                File file = new File("uploads" + subject.getSubjectImages().get(i).getImageInfo().getImageUrl());
                Image image = new Image(file.toURI().toURL().toExternalForm(), 200, 200, false, true);
                ivUser.setImage(image);
//                spUser.getChildren().add(ivUser);
            }
        }
        Scene scene = new Scene(spDialog);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        home.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });
        home.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - x);
            stage.setY(e.getScreenY() - y);
        });
        stage.show();
    }

    public final class UserSetting implements Initializable {

        @FXML
        private Button btnLogOut;

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            btnLogOut.setOnAction(out -> {
                btnUser.setText("");
                Stage stage = (Stage) btnUser.getScene().getWindow();
                fxWeaver.loadController(LoginBioSSO.class).show();
                stage.close();
            });
        }
    }

    private void popupControl() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserSetting.fxml"));
            loader.setController(new UserSetting());
            ppSetting = new JFXPopup(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
