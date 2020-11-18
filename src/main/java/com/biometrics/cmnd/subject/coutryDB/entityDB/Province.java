package com.biometrics.cmnd.subject.coutryDB.entityDB;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "province")
@Getter
@NoArgsConstructor
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "province_name")
    private String province_name;

    @Column(name = "status")
    private int status;

    @Builder
    public Province(int id,City city,String province_name,int status){
        this.id = id;
        this.city = city;
        this.province_name = province_name;
        this.status = status;
    }
}
