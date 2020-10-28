package com.biometrics.cmnd.identify.service;

import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.identify.entity.CaseType;
import com.biometrics.cmnd.identify.entity.Identify;
import com.biometrics.cmnd.subject.entity.Subject;
import com.neurotec.biometrics.NMatchingResult;

import java.util.List;

public interface IdentifyService {

    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject);

    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject, int score);

    public Identify createIdentification(ImageFormat imageFormat, int imageQuality, byte[] prob, List<NMatchingResult> nMatchingResults);

    public Identify findOne(long id);

    public List<Identify> findAll();

    public List<Identify> findAllByCaseType(CaseType caseType);

    public Identify update(long id, Identify identify);

    public void delete(long id);
}
