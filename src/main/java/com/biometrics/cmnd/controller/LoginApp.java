package com.biometrics.cmnd.controller;

import com.biometrics.cmnd.account.dto.AccountDto;
import com.biometrics.cmnd.account.entity.Account;
import com.biometrics.cmnd.account.service.impl.AccountServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("LoginApp.fxml")
@RequiredArgsConstructor
public class LoginApp {

    @FXML private TextField username,password;
    @FXML private Button login;

    final AccountServiceImpl accountService;

    public void login(ActionEvent event) {
        AccountDto.Login login = null;
        Account account = accountService.getAccountByUsername(login.getUsername());
        boolean matched = account.getPassword().isMatched(login.getPassword());

    }
}
