package com.biometrics.cmnd.subject.coutryDB.repositoryDB;

import com.biometrics.cmnd.subject.coutryDB.entityDB.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Integer> {
}
