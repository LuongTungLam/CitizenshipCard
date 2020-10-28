package com.biometrics.cmnd.identify.dto;

import com.biometrics.cmnd.identify.entity.Candidate;
import com.biometrics.cmnd.subject.dto.SubjectDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateDto {

    private long candidateId;

    private int score;

    private SubjectDto.SubjectRes subject;

    @Builder
    public CandidateDto(Candidate candidate) {
        this.candidateId = candidate.getId();
        this.score = candidate.getScore();
        this.subject = SubjectDto.SubjectRes.builder()
                .subject(candidate.getBioTemplate().getSubject())
                .build();
    }
}
