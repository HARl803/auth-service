package com.haribo.auth_service.auth.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.haribo.auth_service.global.entity.BaseTimeEntity;
import com.haribo.auth_service.member.domain.ProfileMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_member")
public class AuthMember extends BaseTimeEntity {
    @Id
    @Column(name = "member_uid", length = 48)
    private String memberUid;

    @Column(name = "kakao_uid", nullable = false, unique = true)
    private Long kakaoUid;

    @Column(name = "last_login_date", nullable = false)
    private LocalDateTime lastLoginDate;

    @OneToOne(mappedBy = "authMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JsonManagedReference
    private ProfileMember profileMember;
}