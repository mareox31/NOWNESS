package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.service.UserDetailsService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@Controller
public class UserController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"/login", "/signup"})
    public String showLoginForm(@AuthenticationPrincipal User user,
                                @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (UserUtil.isNotLogin(user, oAuth2User)) return "login_signup";
        else return "redirect:/main";
    }

    @PostMapping("/signup")
    public String processSignup(SignUpForm signUpForm, HttpServletRequest request) {
        if (isDuplicateUserInfo(signUpForm)) return "redirect:/user/signup";
        User user = signUpForm.toUser(passwordEncoder, request);
        userDetailsService.saveUser(user);
        return "redirect:/user/login";
    }

    private boolean isDuplicateUserInfo(SignUpForm signUpForm) {
        try {
            userDetailsService.loadUserByEmail(signUpForm.getEmail());
            userDetailsService.loadUserByNickname(signUpForm.getNickname());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @GetMapping("/mypage")
    public String showMypage(@AuthenticationPrincipal User user,
                             @AuthenticationPrincipal OAuth2User oAuth2User,
                             Model model) {
        if (UserUtil.isNotLogin(user, oAuth2User)) return "redirect:/user/login";
        if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
        model.addAttribute("id", user.getId());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("nickname", user.getNickname());
        return "mypage";
    }

}
