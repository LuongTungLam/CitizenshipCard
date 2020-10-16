package com.biometrics.cmnd.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum  Role {
    @JsonProperty("ADMIN")
    ADMIN,
    @JsonProperty("SUBADMIN")
    SUBADMIN,
    @JsonProperty("USER")
    USER;

}
