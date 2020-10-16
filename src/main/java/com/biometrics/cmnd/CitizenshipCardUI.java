package com.biometrics.cmnd;

import com.biometrics.cmnd.application.CitizenshipCardApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitizenshipCardUI {
    public static void main(String[] args) {
        Application.launch(CitizenshipCardApplication.class, args);
    }
}
