package com.haribo.auth_service.auth.application.service;

import com.haribo.auth_service.auth.presentation.response.KakaoMemberResponse;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect.login}")
    private String KAKAO_REDIRECT_LOGIN;

    @Value("${kakao.redirect.logout}")
    private String KAKAO_REDIRECT_LOGOUT;

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
}




























