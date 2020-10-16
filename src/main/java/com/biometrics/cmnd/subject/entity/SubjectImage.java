package com.biometrics.cmnd.subject.entity;

import com.biometrics.cmnd.common.dto.Image;
import com.biometrics.cmnd.common.model.Auditable;
import com.biometrics.cmnd.common.model.ImageInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name = "subject_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectImage extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ImageInfo imageInfo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Builder
    public SubjectImage(ImageInfo imageInfo, Subject subject) {
        this.imageInfo = imageInfo;
        this.subject = subject;
    }

    public void updateSubjectImage(Image image) throws IOException {
        this.imageInfo.updateImageInfo(image);
    }

}
