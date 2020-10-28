package com.biometrics.cmnd.identify.repository;


import com.biometrics.cmnd.identify.entity.CaseType;
import com.biometrics.cmnd.identify.entity.Identify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentifyRepository extends JpaRepository<Identify, Long> {

    List<Identify> findAllByCaseType(CaseType caseType);

}
