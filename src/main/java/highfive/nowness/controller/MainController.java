package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.util.UserUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping({"/main", "/"})
    String main(@AuthenticationPrincipal User user,
                @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (UserUtil.isNotLogin(user, oAuth2User)) System.out.println("Not Login");
        return "main";
    }
}
