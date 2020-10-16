package com.biometrics.cmnd.common.service.impl;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.dto.Image;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceRecognitionService implements RecognitionService {

    private final NBiometricClient biometricClient;
    private NSubject faceSubject;

    @Override
    public NSubject extractTemplate(String encodedString, ImageFormat format) {
        if (encodedString == null) throw new NullPointerException("image is null");

        NBiometricTask task = null;

        NImage image = NImageUtils.base64StringToNImage(encodedString, format.name());

//        Image image1 = Image.builder()
//                .base64Image(encodedString)
//                .bioType(BioType.FACE)
//                .build();

//        if (image1.getBioType() == BioType.FACE) {
            NFace face = new NFace();
            face.setImage(image);
            faceSubject = new NSubject();
            faceSubject.getFaces().add(face);
            task = biometricClient.createTask(
                    EnumSet.of(
                            NBiometricOperation.DETECT_SEGMENTS,
                            NBiometricOperation.ASSESS_QUALITY,
                            NBiometricOperation.CREATE_TEMPLATE),
                    faceSubject);
            biometricClient.performTask(task);
//        NBiometricStatus status = biometricClient.createTemplate(faceSubject);
            if (task.getStatus() == NBiometricStatus.OK) {
                if (faceSubject.getFaces().size() > 1) {
                    System.out.printf("Found %d faces\n", faceSubject.getFaces().size() - 1);
                }
                for (NFace nface : faceSubject.getFaces()) {
                    for (NLAttributes attribute : nface.getObjects()) {
                        System.out.println("Face:");
                        System.out.format("\tLocation = (%d, %d), width = %d, height = %d\n", attribute.getBoundingRect().getBounds().x, attribute.getBoundingRect().getBounds().y,
                                attribute.getBoundingRect().width, attribute.getBoundingRect().height);

                        if ((attribute.getRightEyeCenter().confidence > 0) || (attribute.getLeftEyeCenter().confidence > 0)) {
                            System.out.println("\tFound eyes:");
                            if (attribute.getRightEyeCenter().confidence > 0) {
                                System.out.format("\t\tRight: location = (%d, %d), confidence = %d%n", attribute.getRightEyeCenter().x, attribute.getRightEyeCenter().y,
                                        attribute.getRightEyeCenter().confidence);
                            }
                            if (attribute.getLeftEyeCenter().confidence > 0) {
                                System.out.format("\t\tLeft: location = (%d, %d), confidence = %d%n", attribute.getLeftEyeCenter().x, attribute.getLeftEyeCenter().y,
                                        attribute.getLeftEyeCenter().confidence);
                            }
                            if (attribute.getNoseTip().confidence > 0) {
                                System.out.println("\tFound nose:");
                                System.out.format("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getNoseTip().x, attribute.getNoseTip().y, attribute.getNoseTip().confidence);
                            }
                            if (attribute.getMouthCenter().confidence > 0) {
                                System.out.println("\tFound mouth:");
                                System.out.printf("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getMouthCenter().x, attribute.getMouthCenter().y, attribute.getMouthCenter().confidence);
                            }
                        }
                    }
                }
            }
//        }
        return faceSubject;
    }

    @Override
    public int verify(byte[] refTemplate, NImage candidateFaceImage) {
        return 0;
    }

    @Override
    public NSubject verify(String probImage, ImageFormat format, byte[] candidateTemplate) {
        return null;
    }

    @Override
    public List<NMatchingResult> identify(String base64image, ImageFormat imageFormat, Map<Long, byte[]> templates) {
        return null;
    }

    @Override
    public NSubject identify(NSubject probSubject, List<NSubject> referenceSubjects) {
        return null;
    }

    @Override
    public NBiometricStatus enrollOnServer(NSubject subject) {
        return null;
    }

    @Override
    public List<NMatchingResult> identifyOnServer(NSubject subject) {
        return null;
    }
}
