package com.biometrics.cmnd.subject.coutryDB.entityDB;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "city")
@Getter
@NoArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;


    @Column(name = "city_name")
    private String city_name;

    @Column(name = "status")
    private int status;

    @Builder
    public City(int id,Country country,String city_name,int status){
        this.id = id;
        this.country = country;
        this.city_name = city_name;
        this.status = status;
    }
}
