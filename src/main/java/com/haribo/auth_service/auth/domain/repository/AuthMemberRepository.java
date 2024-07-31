package com.haribo.auth_service.auth.domain.repository;

import com.haribo.auth_service.auth.domain.AuthMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthMemberRepository extends JpaRepository<AuthMember, String> {
    Optional<AuthMember> findByKakaoUid(Long kakaoUid);
}