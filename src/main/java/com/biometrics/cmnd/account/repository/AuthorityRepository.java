package com.biometrics.cmnd.account.repository;

import com.biometrics.cmnd.account.entity.Authority;
import com.biometrics.cmnd.common.dto.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority,Long> {
    Authority findByRole(Role role);
}
