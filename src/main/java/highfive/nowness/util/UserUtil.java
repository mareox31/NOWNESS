package highfive.nowness.util;

import highfive.nowness.domain.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import java.util.UUID;

public class UserUtil {
    public static User convertOAuth2UserToUser(OAuth2User oAuth2User) {
        return User.builder()
                .id(oAuth2User.getAttribute("id"))
                .email(oAuth2User.getAttribute("email"))
                .nickname(oAuth2User.getAttribute("nickname"))
                .verifiedEmail(true)
                .build();
    }

    public static void addPublicUserInfoToModel(Model model, User user) {
        model.addAttribute("userId", user.getId());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("verifiedUser", user.isVerifiedEmail());
    }

    public static boolean isNotLogin(User user, OAuth2User oAuth2User) {
        return user == null && oAuth2User == null;
    }

    public static String getRandomCode() {
        return UUID.randomUUID().toString();
    }

    public static String getHost() {
        return "http://101.101.218.244:8080";
    }

}
