package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.util.UserUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping({"/main", "/"})
    String main(@AuthenticationPrincipal User user,
                @AuthenticationPrincipal OAuth2User oAuth2User,
                Model model) {
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }
        return "main";
    }

    @GetMapping("/rankboard")
    public String rankboard() {
        return "rankboard";
    }

    @GetMapping("/reportboard")
    public String reportboard() {
        return "reportboard";
    }

    @GetMapping("/requestboard")
    public String requestboard() {
        return "requestboard";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }



}
