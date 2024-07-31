package com.haribo.auth_service.auth.presentation;

import com.haribo.auth_service.auth.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/authorize")
    public RedirectView authorize() {
        logger.info("kakao-login 화면 진입");
        String kakaoLoginUrl = authService.getKakaoLoginUrl();
        return new RedirectView(kakaoLoginUrl);
    }
}