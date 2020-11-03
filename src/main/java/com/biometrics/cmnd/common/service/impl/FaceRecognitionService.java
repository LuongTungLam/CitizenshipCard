package com.biometrics.cmnd.common.service.impl;

import com.biometrics.cmnd.common.dto.BioType;
import com.biometrics.cmnd.common.dto.Image;
import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.common.service.RecognitionService;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        NSubject probeSubject = null;
        NBiometricTask enrollTask = null;
        NBiometricStatus status = null;
        List<NMatchingResult> results = new ArrayList<>();

        try {
            probeSubject = createSubject(base64image, imageFormat);
            status = biometricClient.createTemplate(probeSubject);
            if (status != NBiometricStatus.OK) {
                System.out.format("Failed to create probe template. Status: %s.\n", status);
                System.exit(-1);
            }

            enrollTask = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
            for (Map.Entry<Long, byte[]> template : templates.entrySet()) {
                enrollTask.getSubjects().add(createSubject(template.getValue(), String.format("GallerySubject_%d", template.getKey())));
            }

            biometricClient.performTask(enrollTask);
            if (enrollTask.getStatus() != NBiometricStatus.OK) {
                System.out.format("Enrollment was unsuccessful. Status: %s.\n", enrollTask.getStatus());
                if (enrollTask.getError() != null) throw enrollTask.getError();
                System.exit(-1);
            }

            biometricClient.setMatchingThreshold(48);

            biometricClient.setFacesMatchingSpeed(NMatchingSpeed.LOW);

            status = biometricClient.identify(probeSubject);

            if (status == NBiometricStatus.OK) {
                for (NMatchingResult result : probeSubject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", result.getId(), result.getScore());
                    results.add(result);
                }
            } else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                System.out.format("Match not found");
            } else {
                System.out.format("Identification failed. Status: %s\n", status);
                System.exit(-1);
            }

        } catch (Throwable th) {

        } finally {
            if (enrollTask != null) enrollTask.dispose();
            if (probeSubject != null) probeSubject.dispose();
            if (biometricClient != null) biometricClient.dispose();
        }

        return results;

    }

    @Override
    public NSubject identify(NSubject probSubject, List<NSubject> referenceSubjects) {
        NBiometricTask enrollTask = null;
        NBiometricStatus status = null;
        List<NMatchingResult> results = new ArrayList<>();

        try {
            status = biometricClient.createTemplate(probSubject);
            if (status != NBiometricStatus.OK) {
                System.out.format("Failed to create probe template. Status: %s.\n", status);
                System.exit(-1);
            }

            enrollTask = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
            enrollTask.getSubjects().addAll(referenceSubjects);

            biometricClient.performTask(enrollTask);
            if (enrollTask.getStatus() != NBiometricStatus.OK) {
                System.out.format("Enrollment was unsuccessful. Status: %s.\n", enrollTask.getStatus());
                if (enrollTask.getError() != null) throw enrollTask.getError();
            }

            biometricClient.setMatchingThreshold(48);

            biometricClient.setFingersMatchingSpeed(NMatchingSpeed.LOW);

            status = biometricClient.identify(probSubject);

            if (status == NBiometricStatus.OK) {
                for (NMatchingResult result : probSubject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", result.getId(), result.getScore());
                    results.add(result);
                }
            } else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText("Not Found Finger !");
                    alert.showAndWait();
                });
            } else {
                System.out.format("Identification failed. Status: %s\n", status);
                System.exit(-1);
            }

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            if (enrollTask != null) enrollTask.dispose();
        }

        return probSubject;

    }

    @Override
    public NBiometricStatus enrollOnServer(NSubject subject) {
        return null;
    }

    @Override
    public List<NMatchingResult> identifyOnServer(NSubject subject) {
        return null;
    }

    private NSubject createSubject(String probImage, ImageFormat format) {
        NSubject subject = extractTemplate(probImage, format);
        subject.setId("prob");
        return subject;
    }

    private NSubject createSubject(byte[] refTemplate) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for (NLRecord record : template.getFaces().getRecords()) {
                log.info("Quality : " + record.getQuality());
            }
        }
        subject.setTemplate(template);
        subject.setId("candidate");
        return subject;
    }

    private NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for (NLRecord record : template.getFaces().getRecords()) {
                log.info("Quality : " + record.getQuality());
            }
        }
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }

    private NSubject createSubject(NImage faceImage) {
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setImage(faceImage);
        subject.getFaces().add(face);
        return subject;
    }

    private NSubject createSubject(String base64Image, String imageFormat) {
        NSubject subject = new NSubject();
        NImage faceImage = NImageUtils.base64StringToNImage(base64Image, imageFormat);
        NFace face = new NFace();
        face.setImage(faceImage);
        subject.getFaces().add(face);
        return subject;
    }

}
