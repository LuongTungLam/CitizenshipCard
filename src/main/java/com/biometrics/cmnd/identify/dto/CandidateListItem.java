package com.biometrics.cmnd.identify.dto;

import com.biometrics.cmnd.common.dto.BioGraphy;
import com.biometrics.cmnd.common.dto.Image;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateListItem {

    private long subjectId;

    private int score;

    private BioGraphy bioGraphy;

    private List<Image> image;

    @Builder
    public CandidateListItem(long subjectId, int score, BioGraphy bioGraphy, List<Image> image) {
        this.subjectId = subjectId;
        this.score = score;
        this.bioGraphy = bioGraphy;
        this.image = image;
    }
}
