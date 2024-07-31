package com.haribo.auth_service.member.domain.repository;

import com.haribo.auth_service.auth.domain.AuthMember;
import com.haribo.auth_service.member.domain.ProfileMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileMemberRepository extends JpaRepository<ProfileMember, String> {
    ProfileMember findByAuthMember(AuthMember authMember);
}

