package com.haribo.auth_service.auth.application.dto.kakaoapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.haribo.auth_service.auth.domain.AuthMember;
import com.haribo.auth_service.member.domain.ProfileMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KakaoMemberDto {
    private AuthMember authMember;
    private ProfileMember profileMember;
}
