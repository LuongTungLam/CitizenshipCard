package com.biometrics.cmnd.account.exception;

import com.biometrics.cmnd.common.model.Email;
import lombok.Getter;

@Getter
public class EmailDuplicationException extends RuntimeException {

    private Email email;
    private String field;

    public EmailDuplicationException(Email email) {
        this.field = "email";
        this.email = email;
    }
}
