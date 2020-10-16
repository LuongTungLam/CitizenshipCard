package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.account.dto.AccountDto;
import com.biometrics.cmnd.account.entity.Account;
import com.biometrics.cmnd.account.entity.Authority;
import com.biometrics.cmnd.account.service.AccountService;
import com.biometrics.cmnd.common.dto.*;
import com.biometrics.cmnd.common.model.*;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
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
import java.util.ResourceBundle;

@Component
@FxmlView("RegisterCard.fxml")
public class RegisterCard implements Initializable {

    // không cần chỗ này
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

//    @FXML
//    private TextField username, password, repassword, firstname, lastname, city, country, province, street, district, zip, birthday, email, phone, nid;
//    @FXML
//    private RadioButton male, female;
//    @FXML
//    private Button chooseImage, save;
//    @FXML
//    private StackPane imageview;
//    @FXML
//    private Label errorGender, errorPassword, errorUsername, errorRepassword, errorFirstname, errorLastname, errorCity, errorCountry, errorProvince, errorStreet, errorDistrict, errorZip, errorBirthday, errorEmail, errorPhone, errorNid;
//    @FXML
//    private ComboBox<Authority> role;
//
//    private File file;
//    private FileChooser fc;
//    private ObservableList<Subject> subject;
//    private ObservableList<Account> account;
//
//    @Autowired
//    private final SubjectService subjectService;
//
//    @Autowired
//    private final RecognitionService recognitionService;
//
//    @Autowired
//    private final AccountService accountService;
//
//    private final NBiometricClient client;
//    private final FaceViewNode faceViewNode;
//
//    public RegisterCard(SubjectService subjectService, NBiometricClient client, RecognitionService recognitionService, AccountService accountService) {
//        super();
//        this.client = client;
//        this.subjectService = subjectService;
//        this.accountService = accountService;
//        this.recognitionService = recognitionService;
//        this.faceViewNode = new FaceViewNode();
//    }
//
//    public void role(){
////        Authority authority =
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        this.imageview.getChildren().add(faceViewNode);
//        this.imageview.setAlignment(Pos.CENTER);
//    }
//
//    @FXML
//    public void chooseImage(ActionEvent event) throws IOException {
//        fc = new FileChooser();
//        file = fc.showOpenDialog(null);
//        if (file != null) {
//            NImage image = NImage.fromFile(file.getAbsolutePath());
//            NSubject subject = new NSubject();
//            NFace face = new NFace();
//            face.setImage(image);
//            subject.getFaces().add(face);
//            this.faceViewNode.setFace(face);
//        }
//    }
//
//    @FXML
//    public void save(ActionEvent event) throws IOException {
//        if (!username.getText().isEmpty() == false) {
//            errorUsername.setText("Password not null");
//        } else if (!password.getText().equals(repassword.getText())) {
//            errorPassword.setText("Password dose not match");
//        } else if (!password.getText().isEmpty() == false) {
//            errorPassword.setText("Password not null");
//        } else if (!firstname.getText().isEmpty() == false) {
//            errorFirstname.setText("Password not null");
//        } else if (!lastname.getText().isEmpty() == false) {
//            errorLastname.setText("Password not null");
//        } else if (!city.getText().isEmpty() == false) {
//            errorCity.setText("Password not null");
//        } else if (!country.getText().isEmpty() == false) {
//            errorCountry.setText("Password not null");
//        } else if (!nid.getText().isEmpty() == false) {
//            errorNid.setText("Password not null");
//        } else if (!province.getText().isEmpty() == false) {
//            errorProvince.setText("Password not null");
//        } else if (!street.getText().isEmpty() == false) {
//            errorStreet.setText("Password not null");
//        } else if (!district.getText().isEmpty() == false) {
//            errorDistrict.setText("Password not null");
//        } else if (!zip.getText().isEmpty() == false) {
//            errorZip.setText("Password not null");
//        } else if (!birthday.getText().isEmpty() == false) {
//            errorBirthday.setText("Password not null");
//        } else if (!email.getText().isEmpty() == false) {
//            errorEmail.setText("Password not null");
//        } else if (!phone.getText().isEmpty() == false) {
//            errorPhone.setText("Password not null");
//        } else if (!(male.isSelected() || female.isSelected())) {
//            errorGender.setText("Gender not select");
//        } else {
//            Register();
//            errorUsername.setText("");
//            errorPassword.setText("");
//            errorRepassword.setText("");
//            errorFirstname.setText("");
//            errorLastname.setText("");
//            errorCity.setText("");
//            errorCountry.setText("");
//            errorNid.setText("");
//            errorProvince.setText("");
//            errorStreet.setText("");
//            errorDistrict.setText("");
//            errorZip.setText("");
//            errorBirthday.setText("");
//            errorEmail.setText("");
//            errorPhone.setText("");
//            errorGender.setText("");
//        }
//    }
//
//    public void Register() throws IOException {
//        if (file != null) {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.registerModule(new JavaTimeModule());
//            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//            BioGraphy bioGraphy = BioGraphy.builder()
//                    .firstName(getFirstName())
//                    .lastName(getLastName())
//                    .nid(getNid())
//                    .gender(Gender.valueOf(getGender()))
//                    .birthDate(LocalDate.parse(getBirthday()))
//                    .build();
//            Address address = Address.builder()
//                    .street(getStreet())
//                    .city(getCity())
//                    .district(getDistrict())
//                    .province(getProvince())
//                    .country(getCountry())
//                    .zip(getZip())
//                    .build();
//            Contact contact = Contact.builder()
//                    .address(address)
//                    .phoneNumber(getPhoneNumber())
//                    .email(getEmail())
//                    .build();
//
//            String base64Image = NImageUtils.imageFileToBase64String(file.getAbsolutePath());
//            Image image = Image.builder()
//                    .quality(100)
//                    .bioType(BioType.FACE)
//                    .format(ImageFormat.JPG)
//                    .base64Image(base64Image)
//                    .enabled(true)
//                    .pose(Pose.FACE_FRONT)
//                    .build();
//
//            SubjectDto.CreateReq dto = SubjectDto.CreateReq.builder()
//                    .bioGraphy(bioGraphy)
//                    .contact(contact)
//                    .image(image)
//                    .build();
//
//            NSubject faceSubject = recognitionService.extractTemplate(base64Image, ImageFormat.JPG);
//            byte[] template = faceSubject.getTemplateBuffer().toByteArray();
//            String imagePath = generateFaceImagePath();
//            String filePath = "./uploads/" + imagePath;
//            Path path = Paths.get(filePath);
//            if (Files.notExists(path)) {
//                Files.createDirectories(Paths.get(filePath));
//            }
//
//            String fileName = dto.getBioGraphy().getNid() + "_face." + dto.getImage().getFormat().name();
//            switch (dto.getImage().getFormat().name()) {
//                case "JPG":
//                case "JPEG":
//                    faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getJPEG());
//                    break;
//                case "PNG":
//                    faceSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getPNG());
//                    break;
//                case "WSQ":
//                    faceSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getWSQ());
//                    break;
//                case "TIFF":
//                    faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getTIFF());
//                    break;
//            }
//
//            int quality =  faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
//
//            ImageInfo imageInfo = ImageInfo.builder()
//                    .imageFormat(ImageFormat.PNG)
//                    .imageUrl("/" + imagePath + fileName)
//                    .imageQuality(quality)
//                    .bioType(BioType.FACE)
//                    .pose(Pose.FACE_FRONT)
//                    .enabled(true)
//                    .build();
//
//            Subject subject = subjectService.create(dto, imageInfo, template);
//
//            Account account = Account.builder()
//                    .username(getUsername())
//                    .password(Password.builder().value(getPassword()).build())
//                    .subject(subject)
////                    .authorities()
//                    .build();
//
////            AccountDto.SignUpRes resDto = new AccountDto.SignUpRes(subject);
//            saveAlert(account);
//        }
//    }
//
//    private String generateFaceImagePath() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
//        String imagePath = LocalDateTime.now().format(formatter);
//        return imagePath;
//    }
//
//    private void saveAlert(Account account) {
//    }
//
//    private String getGenderTitle(String gender) {
//        return (gender.equals("MALE")) ? "his" : "her";
//    }
//
//    public String getUsername() {
//        return username.getText();
//    }
//
//    public String getPassword() {
//        return password.getText();
//    }
//
//    public String getGender() {
//        return male.isSelected() ? "MALE" : "FEMALE";
//    }
//
//    public String getFirstName() {
//        return firstname.getText();
//    }
//
//    public String getEmail() {
//        return email.getText();
//    }
//
//    public String getLastName() {
//        return lastname.getText();
//    }
//
//    public String getDistrict() {
//        return district.getText();
//    }
//
//    public String getCity() {
//        return city.getText();
//    }
//
//    public String getCountry() {
//        return country.getText();
//    }
//
//    public String getProvince() {
//        return province.getText();
//    }
//
//    public String getStreet() {
//        return street.getText();
//    }
//
//    public String getZip() {
//        return zip.getText();
//    }
//
//    public String getBirthday() {
//        return birthday.getText();
//    }
//
//    public String getNid() {
//        return nid.getText();
//    }
//
//    public String getPhoneNumber() {
//        return phone.getText();
//    }
}
