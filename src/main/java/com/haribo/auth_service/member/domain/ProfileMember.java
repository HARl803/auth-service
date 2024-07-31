package com.haribo.auth_service.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.haribo.auth_service.auth.domain.AuthMember;
import com.haribo.auth_service.global.entity.BaseTimeEntity;
import com.haribo.auth_service.global.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "profile")
public class ProfileMember extends BaseTimeEntity {
    @Id
    @Column(name = "profile_id")
    private String profileId;

    @OneToOne
    @JoinColumn(name = "member_uid", referencedColumnName = "member_uid")
    @JsonIgnore
    private AuthMember authMember;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nick_name", nullable = false, unique = true)
    private String nickName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false)
    private MemberStatus memberStatus;
}