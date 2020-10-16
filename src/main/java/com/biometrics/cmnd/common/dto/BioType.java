package com.biometrics.cmnd.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BioType {
    @JsonProperty("FACE")
    FACE,
    @JsonProperty("FINGER")
    FINGER,
    @JsonProperty("IRIS")
    IRIS,
    @JsonProperty("VOICE")
    VOICE;

}
