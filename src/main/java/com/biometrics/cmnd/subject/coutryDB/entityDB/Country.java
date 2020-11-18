package com.biometrics.cmnd.subject.coutryDB.entityDB;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "country")
@Getter
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "country_name", length = 200)
    private String country_name;

    @Column(name = "status", length = 10)
    private int status;

    @Builder
    public Country(int id,String country_name,int status){
        this.id = id;
        this.country_name = country_name;
        this.status = status;
    }

}
