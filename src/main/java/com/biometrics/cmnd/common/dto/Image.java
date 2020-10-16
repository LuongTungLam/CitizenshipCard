package com.biometrics.cmnd.common.dto;

import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.model.Pose;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @NotNull
    private String base64Image;

    @NotNull
    private ImageFormat format;

    @NotNull
    private BioType bioType;

    private Pose pose;

    private int quality;

    private boolean enabled;


    @Builder
    private Image(String base64Image, ImageFormat format, BioType bioType, Pose pose, int quality, boolean enabled) {
        this.base64Image = base64Image;
        this.format = format;
        this.bioType = bioType;
        this.pose = pose;
        this.quality = quality;
        this.enabled = enabled;
    }

}
