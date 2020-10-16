package com.biometrics.cmnd.account.exception;

import com.biometrics.cmnd.error.ErrorCode;

public class PasswordFailedExceededException extends RuntimeException {

    private ErrorCode errorCode;

    public PasswordFailedExceededException() {
        this.errorCode = ErrorCode.PASSWORD_FAILED_EXCEEDED;
    }
}
