package highfive.nowness.service;

import highfive.nowness.dto.UserDTO;
import highfive.nowness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.UUID;

@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private static final String FORMATTED_ID_KEY_NAME = "id";
    private static final String FORMATTED_EMAIL_KEY_NAME = "email";
    private static final String GOOGLE_ID_KEY_NAME = "sub";
    private static final String GOOGLE_EMAIL_KEY_NAME = "email";
    private static final String KAKAO_ID_KEY_NAME = "id";
    private static final String KAKAO_EMAIL_KEY_NAME = "email";
    private static final String KAKAO_USERINFO_KEY_NAME = "kakao_account";
    private static final String NAVER_ID_KEY_NAME = "id";
    private static final String NAVER_EMAIL_KEY_NAME = "email";
    private static final String NAVER_USERINFO_KEY_NAME = "response";

    private static final String GOOGLE_CLIENT_NAME = "Google";
    private static final String KAKAO_CLIENT_NAME = "kakao";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuth2UserCustomService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return getUser(oAuth2User, userRequest);
    }

    private DefaultOAuth2User getUser(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        String clientName = userRequest.getClientRegistration().getClientName();
        Map<String, Object> userInfo = convertAttributesToUnifiedFormat(clientName, oAuth2User.getAttributes());
        String email = userInfo.get(FORMATTED_EMAIL_KEY_NAME).toString();
        UserDTO userDto = loadUserByEmail(email);
        if (!userDto.isVerifiedEmail()) userDto.setVerifiedEmail(true);
        saveOrUpdateUser(userDto);
        userInfo.put("id", userDto.getId());
        userInfo.put("nickname", userDto.getNickname());
        return new DefaultOAuth2User(getUserAuthorities(oAuth2User), userInfo, FORMATTED_ID_KEY_NAME);
    }

    private Map<String, Object> convertAttributesToUnifiedFormat(String clientName, Map<String, Object> attributes) {
        switch (clientName) {
            case GOOGLE_CLIENT_NAME -> {
                return convertGoogleAttributes(attributes);
            }
            case KAKAO_CLIENT_NAME -> {
                return convertKakaoAttributes(attributes);
            }
            default -> {
                return convertNaverAttributes(attributes);
            }
        }
    }

    private Map<String, Object> convertGoogleAttributes(Map<String, Object> attributes) {
        Map<String, Object> googleAttributes = new HashMap<>();
        googleAttributes.put(FORMATTED_ID_KEY_NAME, attributes.get(GOOGLE_ID_KEY_NAME));
        googleAttributes.put(FORMATTED_EMAIL_KEY_NAME, attributes.get(GOOGLE_EMAIL_KEY_NAME));
        return googleAttributes;
    }

    private Map<String, Object> convertKakaoAttributes(Map<String, Object> attributes) {
        Map<String, Object> kakaoAttributes = new HashMap<>();
        kakaoAttributes.put(FORMATTED_ID_KEY_NAME, attributes.get(KAKAO_ID_KEY_NAME));
        kakaoAttributes.put(FORMATTED_EMAIL_KEY_NAME,
                ((Map<String, Object>)attributes.get(KAKAO_USERINFO_KEY_NAME)).get(KAKAO_EMAIL_KEY_NAME));
        return kakaoAttributes;
    }

    private Map<String, Object> convertNaverAttributes(Map<String, Object> attributes) {
        Map<String, Object> naverAttributes = new HashMap<>();
        naverAttributes.put(FORMATTED_ID_KEY_NAME,
                ((Map<String, Object>)attributes.get(NAVER_USERINFO_KEY_NAME)).get(NAVER_ID_KEY_NAME));
        naverAttributes.put(FORMATTED_EMAIL_KEY_NAME,
                ((Map<String, Object>)attributes.get(NAVER_USERINFO_KEY_NAME)).get(NAVER_EMAIL_KEY_NAME));
        return naverAttributes;
    }

    private UserDTO loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> UserDTO.builder()
                .email(email)
                .nickname(UUID.nameUUIDFromBytes(email.getBytes()).toString().substring(0, 8))
                .password(passwordEncoder.encode(email))
                .lastLoginIp(getUserIpAddress())
                .build());
    }

    private String getUserIpAddress() {
        var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getRemoteAddr();
    }

    private void saveOrUpdateUser(UserDTO userDTO) {
        userRepository.save(userDTO);
    }

    private Set<GrantedAuthority> getUserAuthorities(OAuth2User oAuth2User) {
        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }
}
