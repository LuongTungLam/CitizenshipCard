package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("AllSubject.fxml")
public class AllSubject implements Initializable {

    @Autowired
    private final SubjectService subjectService;

    @FXML
    private TableView<Subject> tableSubject;

    private ObservableList data;

    public AllSubject(SubjectService subjectService){
        super();
        this.subjectService = subjectService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listSubject();
    }

    public void listSubject(){
        List<Subject> subjectList = subjectService.findAll();
        data = FXCollections.observableArrayList(subjectList);

        TableColumn<Subject,Integer> id = new TableColumn<>("Id");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Subject,String> created_by = new TableColumn<>("Created By");
        created_by.setCellValueFactory(new PropertyValueFactory<>("created_by"));

        TableColumn<Subject,String> city = new TableColumn<>("City");
        city.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Subject,String> zip = new TableColumn<>("Zip");
        zip.setCellValueFactory(new PropertyValueFactory<>("zip"));

        TableColumn<Subject, Date> birth_date = new TableColumn<>("Birth Date");
        birth_date.setCellValueFactory(new PropertyValueFactory<>("birth_date"));

        TableColumn<Subject,String> email = new TableColumn<>("Email");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Subject,String> gender = new TableColumn<>("Gender");
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Subject,String> first_name = new TableColumn<>("First Name");
        first_name.setCellValueFactory(new PropertyValueFactory<>("first_name"));

        TableColumn<Subject,String> last_name = new TableColumn<>("Last Name");
        last_name.setCellValueFactory(new PropertyValueFactory<>("last_name"));

        TableColumn<Subject,String> nid = new TableColumn<>("Nid");
        nid.setCellValueFactory(new PropertyValueFactory<>("nid"));

        TableColumn<Subject,String> phone_number = new TableColumn<>("Phone Number");
        phone_number.setCellValueFactory(new PropertyValueFactory<>("phone_number"));

        TableColumn detail = new TableColumn("Action");

        tableSubject.getColumns().setAll(id,created_by,city,zip,birth_date,email,gender,first_name,last_name,nid,phone_number,detail);
        tableSubject.setItems(data);
    }

    public void detail(ActionEvent event) throws IOException {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("DetailSubject.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        DetailSubject detailSubject = loader.getController();
        Subject selected = tableSubject.getSelectionModel().getSelectedItem();
        detailSubject.setSubject(selected);
        stage.setScene(scene);
    }
}
