package com.biometrics.cmnd.subject.repository;

import com.biometrics.cmnd.subject.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject,Long> {

    Optional<Subject> findByNid(String nid);

}
