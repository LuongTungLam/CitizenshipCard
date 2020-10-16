package com.biometrics.cmnd.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageFormat {
    @JsonProperty("JPG")
    JPG,
    @JsonProperty("JPEG")
    JPEG,
    @JsonProperty("PNG")
    PNG,
    @JsonProperty("WSQ")
    WSQ,
    @JsonProperty("TIFF")
    TIFF;
}
