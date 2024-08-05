package com.haribo.auth_service.auth.application.service;

import com.haribo.auth_service.auth.presentation.response.KakaoMemberResponse;

public interface AuthService {
    String getKakaoLoginUrl();
    KakaoMemberResponse getKakaoInfoWithCode(String code) throws Exception;
    String getKakaoLogoutUrl();
}
