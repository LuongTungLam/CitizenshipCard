package com.biometrics.cmnd.common.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    private int status;
    private String message;
    private Object results;

    @Builder
    public ApiResponse(int status,String message,Object results){
        this.status = status;
        this.message = message;
        this.results = results;
    }
}
