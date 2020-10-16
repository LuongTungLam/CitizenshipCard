package com.biometrics.cmnd.account.entity;

import com.biometrics.cmnd.account.dto.AccountDto;
import com.biometrics.cmnd.common.dto.Auth;
import com.biometrics.cmnd.common.model.Auditable;
import com.biometrics.cmnd.common.model.Password;
import com.biometrics.cmnd.subject.entity.Subject;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "account")
@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Embedded
    private Password password;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled;

    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "account_authorities", joinColumns = {
            @JoinColumn(name = "account_id") }, inverseJoinColumns = {
            @JoinColumn(name = "authority_id") })
    private Set<Authority> authorities = new HashSet<>();

    @Builder
    public Account(
            String username,
            Password password, Subject subject, Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.subject = subject;
        this.authorities = authorities;
    }
    public void updatePassword(AccountDto.PasswordChangeReq dto) {
        this.password.changePassword(dto.getNewPassword(), dto.getOldPassword());
    }

    public void updateAccount(Auth auth) {
        this.username = auth.getUsername();
        this.password.changePassword(auth.getPassword(), this.password.getValue());
    }

}
