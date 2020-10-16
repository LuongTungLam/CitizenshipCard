package com.biometrics.cmnd.account.repository;

import com.biometrics.cmnd.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Account findByUsername(String userId);
}
