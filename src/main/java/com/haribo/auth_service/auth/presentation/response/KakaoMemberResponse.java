package com.haribo.auth_service.auth.presentation.response;

import com.haribo.auth_service.global.enums.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoMemberResponse {
    private String memberUid;
    private Long kakaoId; // 인가코드가 아닌 회원번호

    // 필수
    private String email;
    private String nickname;

    // 선택
    private String profileImage;

    // 추가 정보
    private LocalDateTime lastLoginDate;
    private String profileId;
    private MemberStatus memberStatus;
}

