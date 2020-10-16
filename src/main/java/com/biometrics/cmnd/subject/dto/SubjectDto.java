package com.biometrics.cmnd.subject.dto;

import com.biometrics.cmnd.common.dto.Address;
import com.biometrics.cmnd.common.dto.BioGraphy;
import com.biometrics.cmnd.common.dto.Contact;
import com.biometrics.cmnd.common.dto.Image;
import com.biometrics.cmnd.common.model.Email;
import com.biometrics.cmnd.common.model.Name;
import com.biometrics.cmnd.common.model.Nid;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.entity.SubjectImage;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SubjectDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateReq {
        @Valid
        private BioGraphy bioGraphy;
        @Valid
        private Contact contact;
        @Valid
        private List<Image> images = new ArrayList<>();

        @Builder
        public CreateReq(BioGraphy bioGraphy, Contact contact, List<Image> images) {
            this.bioGraphy = bioGraphy;
            this.contact = contact;
            this.images = images;
        }

        public Subject toEntity() {
            Subject subject = Subject.builder()
                    .nid(Nid.builder().value(this.bioGraphy.getNid()).build())
                    .name(buildName())
                    .gender(this.bioGraphy.getGender())
                    .birthDate(this.bioGraphy.getBirthDate())
                    .email(Email.builder().address(this.contact.getEmail()).build())
                    .phoneNumber(this.contact.getPhoneNumber())
                    .address(buildAddress())
                    .build();

            return subject;
        }

        private Name buildName() {
            return Name.builder()
                    .firstName(this.bioGraphy.getFirstName())
                    .lastName(this.bioGraphy.getLastName())
                    .build();
        }

        private Address buildAddress() {
            return Address.builder()
                    .street(this.contact.getAddress().getStreet())
                    .city(this.contact.getAddress().getCity())
                    .province(this.contact.getAddress().getProvince())
                    .country(this.contact.getAddress().getCountry())
                    .zip(this.contact.getAddress().getZip())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SubjectRes {
        private long subjectId;
        private LocalDateTime createdAt;
        private BioGraphy bioGraphy;
        private Contact contact;
        private Image image;

        @Builder
        public SubjectRes(Subject subject) {
            this.subjectId = subject.getId();
            this.createdAt = subject.getCreatedDate();

            this.bioGraphy = BioGraphy.builder()
                    .nid(subject.getNid().getValue())
                    .firstName(subject.getName().getFirstName())
                    .lastName(subject.getName().getLastName())
                    .gender(subject.getGender())
                    .birthDate(subject.getBirthDate())
                    .build();

            this.contact = Contact.builder()
                    .email(subject.getEmail().getAddress())
                    .phoneNumber(subject.getPhoneNumber())
                    .address(subject.getAddress())
                    .build();
            // return only one image which is enabled.
            List<SubjectImage> subjectImages = subject.getSubjectImages();
            for (SubjectImage subjectImage : subject.getSubjectImages()) {
                if (subjectImage.getImageInfo().isEnabled()) {
                    String imageUrl = "./uploads" + subjectImage.getImageInfo().getImageUrl();
                    this.image = Image.builder()
                            .base64Image(NImageUtils.imageFileToBase64String(imageUrl))
                            .bioType(subjectImage.getImageInfo().getBioType())
                            .format(subjectImage.getImageInfo().getFormat())
                            .quality(subjectImage.getImageInfo().getImageQuality())
                            .build();
                }
            }
        }
    }
}
