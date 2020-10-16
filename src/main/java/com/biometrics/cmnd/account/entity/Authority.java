package com.biometrics.cmnd.account.entity;

import com.biometrics.cmnd.common.dto.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "authority")
@Getter
@ToString
@NoArgsConstructor
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Authority(Role role) {
        this.role = role;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

}
