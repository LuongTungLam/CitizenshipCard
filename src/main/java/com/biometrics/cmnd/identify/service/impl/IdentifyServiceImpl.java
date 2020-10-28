package com.biometrics.cmnd.identify.service.impl;

import com.biometrics.cmnd.common.model.ImageFormat;
import com.biometrics.cmnd.identify.entity.Candidate;
import com.biometrics.cmnd.identify.entity.CaseType;
import com.biometrics.cmnd.identify.entity.Identify;
import com.biometrics.cmnd.identify.exception.IdentifyNotFoundException;
import com.biometrics.cmnd.identify.repository.IdentifyRepository;
import com.biometrics.cmnd.identify.service.IdentifyService;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.service.SubjectService;
import com.neurotec.biometrics.NMatchingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdentifyServiceImpl implements IdentifyService {

    private final IdentifyRepository identifyRepository;

    private final SubjectService subjectService;

    @Override
    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject) {
        Identify identify = Identify.builder()
                .caseType(CaseType.VERIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();
        identify.addSubject(subject);
        identify = identifyRepository.save(identify);
        return identify;
    }

    @Override
    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject, int score) {
        Identify identify = Identify.builder()
                .caseType(CaseType.VERIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();
        identify.addSubject(subject);

        Candidate candidate = Candidate.builder()
                .score(score)
                .identify(identify)
                .bioTemplate(subject.getBioTemplate())
                .build();
        identify.addCandidate(candidate);
        identify = identifyRepository.save(identify);
        return identify;
    }


    @Override
    public Identify createIdentification(ImageFormat imageFormat, int imageQuality, byte[] prob, List<NMatchingResult> nMatchingResults) {
        Identify identify = Identify.builder()
                .caseType(CaseType.IDENTIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();

        for (int i = 0; i < nMatchingResults.size(); i++) {
            String id = nMatchingResults.get(i).getId();
            int index = id.indexOf('_');
            Long subjectId = Long.valueOf(id.substring(index + 1));
            Subject subject = subjectService.findById(subjectId);
            Candidate candidate = Candidate.builder()
                    .bioTemplate(subject.getBioTemplate())
                    .score(nMatchingResults.get(i).getScore())
                    .identify(identify)
                    .build();
            identify.addCandidate(candidate);
        }

        identify = identifyRepository.save(identify);
        return identify;
    }

    @Override
    public Identify findOne(long id) {
        Optional<Identify> identify = identifyRepository.findById(id);
        identify.orElseThrow(() -> new IdentifyNotFoundException(id));
        return identify.get();
    }

    @Override
    public List<Identify> findAll() {
        return identifyRepository.findAll();
    }

    @Override
    public List<Identify> findAllByCaseType(CaseType caseType) {
        return identifyRepository.findAllByCaseType(caseType);
    }

    @Override
    public Identify update(long id, Identify identify) {
        Identify forUpdate = findOne(id);
        forUpdate = identify;
        Identify updated = identifyRepository.save(forUpdate);
        return updated;
    }

    @Override
    public void delete(long id) {
        Identify identify = findOne(id);
        identifyRepository.delete(identify);
    }
}
