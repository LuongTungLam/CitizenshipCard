package com.biometrics.cmnd.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("UNKNOWN") UNKNOWN,
    @JsonProperty("MALE") MALE,
    @JsonProperty("FEMALE") FEMALE;
}
