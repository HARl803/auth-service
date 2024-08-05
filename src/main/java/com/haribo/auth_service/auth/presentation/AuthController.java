package com.haribo.auth_service.auth.presentation;

import com.haribo.auth_service.auth.application.service.AuthService;
import com.haribo.auth_service.auth.presentation.response.KakaoMemberResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/redirect-kakaologin")
    public ResponseEntity<KakaoMemberResponse> kakaoLogin(HttpServletRequest request) throws Exception {
        logger.info("kakao-login 후에 진행할 로직");
        KakaoMemberResponse kakaoMemberResponse = authService.getKakaoInfoWithCode(request.getParameter("code"));
        return ResponseEntity.ok().body(kakaoMemberResponse);
    }

    @GetMapping("/logout")
    public RedirectView logout(HttpServletRequest request) {
        logger.info("kakao-logout 화면 진입");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.info("세션 무효화 성공");
        } else {
            logger.info("세션이 존재하지 않아요");
        }

        String kakaoLogoutUrl = authService.getKakaoLogoutUrl();
        return new RedirectView(kakaoLogoutUrl);
    }

    @GetMapping("/redirect-kakalogout")
    public RedirectView kakalogout() {
        logger.info("kakao-logout 후에 진행할 로직");
        // TODO : 배포 후 사이트 주소 넣을 예정
        return new RedirectView();
    }

}