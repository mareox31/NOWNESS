package highfive.nowness.util;

import highfive.nowness.domain.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserUtil {
    public static User convertOAuth2UserToUser(OAuth2User oAuth2User) {
        return User.builder()
                .id(oAuth2User.getAttribute("id"))
                .email(oAuth2User.getAttribute("email"))
                .nickname(oAuth2User.getAttribute("nickname"))
                .build();
    }

    public static boolean isNotLogin(User user, OAuth2User oAuth2User) {
        return user == null && oAuth2User == null;
    }

}
