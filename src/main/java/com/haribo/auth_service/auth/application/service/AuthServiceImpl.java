package com.haribo.auth_service.auth.application.service;

import com.haribo.auth_service.auth.application.dto.kakaoapi.KakaoMemberDto;
import com.haribo.auth_service.auth.domain.AuthMember;
import com.haribo.auth_service.auth.domain.repository.AuthMemberRepository;
import com.haribo.auth_service.auth.presentation.response.KakaoMemberResponse;
import com.haribo.auth_service.member.domain.ProfileMember;
import com.haribo.auth_service.member.domain.repository.ProfileMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMemberRepository authMemberRepository;
    private final ProfileMemberRepository profileMemberRepository;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect.login}")
    private String KAKAO_REDIRECT_LOGIN;

    @Value("${kakao.redirect.logout}")
    private String KAKAO_REDIRECT_LOGOUT;

    @Value("${base.profile-image}")
    private String BASE_PROFILE_IMAGE;


    private static final String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private static final String KAKAO_API_URI = "https://kapi.kakao.com";

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public String getKakaoLoginUrl() {
        return KAKAO_AUTH_URI + "/oauth/authorize"
                + "?client_id=" + KAKAO_CLIENT_ID
                + "&redirect_uri=" + KAKAO_REDIRECT_LOGIN
                + "&response_type=code";
    }

    @Override
    public KakaoMemberResponse getKakaoInfoWithCode(String code) throws Exception {
        if (code == null)
            throw new Exception("Failed get authorization code");

        logger.info("1. 인가코드로 Access Token 발급하기");
        String accessToken = getKakaoAccessTokem(code);

        logger.info("2. Access Token으로 카카오에 저장된 멤버 정보 가져오기");
        JSONObject jsonObj = getMemberInfoWithToken(accessToken);

        logger.info("3. 가져온 정보로 가입 후 로그인 or 로그인을 진행하기");
        KakaoMemberDto kakaoMemberDto = registerAndLoginIfNeeded(jsonObj);

        logger.info("4. 응답형식(KakaoMemberResponse)에 맞게 리턴하기");


        return KakaoMemberResponse.builder().build();
    }

    private String getKakaoAccessTokem(String code) throws ParseException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("client_secret", KAKAO_CLIENT_SECRET);
        params.add("code", code);
        params.add("redirect_uri", KAKAO_REDIRECT_LOGIN);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_AUTH_URI + "/oauth/token",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonParser.parse(response.getBody());
        return (String) jsonObj.get("access_token");
    }

    private JSONObject getMemberInfoWithToken(String accessToken) throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded; charset=utf-8");

        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(
                KAKAO_API_URI + "/v2/user/me",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(response.getBody());
    }

    private KakaoMemberDto registerAndLoginIfNeeded(JSONObject jsonObj) throws Exception {
        JSONObject account = (JSONObject) jsonObj.get("kakao_account");
        JSONObject profile = (JSONObject) account.get("profile");

        long kakaoId = (long) jsonObj.get("id");
        String email = String.valueOf(account.get("email"));
        String nickName = String.valueOf(profile.get("nickname"));
        String profileImage = String.valueOf(profile.get("profile_image_url"));


        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        logger.info("가입여부 판단");
        AuthMember authMember;
        ProfileMember profileMember;

        Optional<AuthMember> optionalAuthMember = authMemberRepository.findByKakaoUid(kakaoId);
        if (optionalAuthMember.isPresent()) {
            logger.info("이미 가입한 유저이므로 DB 에서 가져오기");
            authMember = optionalAuthMember.get();
            authMember.setLastLoginDate(LocalDateTime.now());
            authMemberRepository.save(authMember);

            profileMember = authMember.getProfileMember();
        }else{
            logger.info("신규 멤버이므로 가입 처리하기");
            authMember = saveNewAuthMember(kakaoId);
            profileMember = saveNewProfileMember(authMember, email, nickName, profileImage);
        }

        logger.info("세션에 로그인한 멤버 정보 저장하기");
        session.setAttribute("authMember", authMember);
        session.setAttribute("profileMember", profileMember);

        return KakaoMemberDto.builder()
                .authMember(authMember)
                .profileMember(profileMember)
                .build();
    }

    private AuthMember saveNewAuthMember(long kakaoUid) {
        String memberUid = String.valueOf(UUID.randomUUID());

        AuthMember authMember = AuthMember.builder()
                .memberUid(memberUid)
                .kakaoUid(kakaoUid)
                .lastLoginDate(LocalDateTime.now())
                .build();
        authMemberRepository.save(authMember);

        return authMemberRepository.findById(memberUid).orElseThrow(() -> new RuntimeException("AuthMember not found"));
    }

    private ProfileMember saveNewProfileMember(AuthMember authMember, String email, String nickname, String profileImage){
        ProfileMember profileMember = ProfileMember.builder()
                .profileId(authMember.getMemberUid())
                .authMember(authMember)
                .name(nickname)
                .nickName(nickname)
                .email(email)
                .profileImage(profileImage.equals("null") ? BASE_PROFILE_IMAGE : profileImage)
                .memberStatus(com.haribo.auth_service.global.enums.MemberStatus.ACTIVE)
                .build();

        profileMemberRepository.save(profileMember);

        return profileMemberRepository.findById(authMember.getMemberUid()).orElseThrow(() -> new RuntimeException("ProfileMember not found"));
    }
}




























