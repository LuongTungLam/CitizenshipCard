package com.biometrics.cmnd.subject.coutryDB.entityDB;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "district")
@Getter
@NoArgsConstructor
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "district_name")
    private String district_name;

    @Column(name = "status")
    private int status;

    @Builder
    public District(int id,Province province,String district_name,int status){
        this.id = id;
        this.province = province;
        this.district_name = district_name;
        this.status = status;
    }
}
