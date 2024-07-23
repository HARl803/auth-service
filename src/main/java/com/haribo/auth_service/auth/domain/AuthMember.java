package com.haribo.auth_service.auth.domain;

import com.haribo.auth_service.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_member")
public class AuthMember extends BaseTimeEntity {
    @Id
    private String memberId;

    @Column(nullable = false, unique = true)
    private String kakaoUid;
}