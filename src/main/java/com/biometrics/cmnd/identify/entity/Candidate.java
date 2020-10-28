package com.biometrics.cmnd.identify.entity;

import com.biometrics.cmnd.common.model.Auditable;
import com.biometrics.cmnd.subject.entity.BioTemplate;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "candidate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"identify", "bioTemplate"})
public class Candidate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score")
    private int score;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "identity_id", nullable = false)
    private Identify identify;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bio_template_id", nullable = false)
    private BioTemplate bioTemplate;

    @Builder
    public Candidate(int score, Identify identify, BioTemplate bioTemplate) {
        this.score = score;
        this.identify = identify;
        this.bioTemplate = bioTemplate;
    }

}
