package com.biometrics.cmnd.subject.coutryDB.repositoryDB;

import com.biometrics.cmnd.subject.coutryDB.entityDB.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Integer> {
}
