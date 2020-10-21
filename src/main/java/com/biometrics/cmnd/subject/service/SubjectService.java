package com.biometrics.cmnd.subject.service;

import com.biometrics.cmnd.account.dto.AccountDto;
import com.biometrics.cmnd.account.entity.Authority;
import com.biometrics.cmnd.account.exception.UserIdDuplicationException;
import com.biometrics.cmnd.account.repository.AuthorityRepository;
import com.biometrics.cmnd.account.service.AccountService;
import com.biometrics.cmnd.common.model.ImageInfo;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import com.biometrics.cmnd.subject.entity.Subject;
import com.biometrics.cmnd.subject.entity.SubjectImage;
import com.biometrics.cmnd.subject.exception.SubjectNotFoundException;
import com.biometrics.cmnd.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service(value = "subjectService")
@Transactional
public class SubjectService {

    private final AuthorityRepository authorityRepository;
    private final SubjectRepository subjectRepository;
    private final AccountService accountService;

    public Subject create(AccountDto.SignUpReq dto, ImageInfo imageInfo, byte[] bioTemplate) {
        if (accountService.isExistedUserId(dto.getAuth().getUsername())) {
            throw new UserIdDuplicationException(dto.getAuth().getUsername());
        }
        Subject subject = dto.toSubjectEntity();
        subject.addSubjectImage(imageInfo);
        subject.addBioTemplate(bioTemplate);
        Set<Authority> authorities = new HashSet<>();
        dto.getAuth().getRoles().forEach(role -> {
            authorities.add(authorityRepository.findByRole(role));
        });
        subject.addAccount(dto.getAuth(), authorities);
        return subjectRepository.save(subject);
    }

    public Subject create(SubjectDto.CreateReq dto, List<ImageInfo> imageInfos, byte[] bioTemplate) {

        Subject subject = dto.toEntity();
        imageInfos.forEach(imageInfo -> subject.addSubjectImage(imageInfo));
        subject.addBioTemplate(bioTemplate);

        return subjectRepository.save(subject);
    }

    public Subject update(long id, SubjectDto.CreateReq dto, ImageInfo imageInfo, byte[] bioTemplate) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new SubjectNotFoundException(id));
        subject.updateSubject(dto.getBioGraphy(), dto.getContact());

        int birthYear = dto.getBioGraphy().getBirthDate().getYear();
        subject.getBioTemplate().updateBioTemplate(bioTemplate,
                dto.getBioGraphy().getGender(),
                birthYear,
                dto.getContact().getAddress().getProvince(),
                dto.getContact().getAddress().getCountry(),
                dto.getImages().get(0).getBioType());

        SubjectImage subjectImage = SubjectImage.builder().imageInfo(imageInfo).subject(subject).build();

        subject.getSubjectImages().stream().forEach(subjectImage1 -> {
            subjectImage1.getImageInfo().setDisable();
        });

        subject.getSubjectImages().add(subjectImage);

        return subjectRepository.save(subject);
    }

    public Subject findById(long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new RuntimeException("cannot found subject by given id:" + id));
        return subject;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Subject update(Subject subject) {
        return subjectRepository.save(subject);
    }

    public void delete(Subject subject) {
        subjectRepository.delete(subject);
    }

    public long count() {
        return subjectRepository.count();
    }
}
