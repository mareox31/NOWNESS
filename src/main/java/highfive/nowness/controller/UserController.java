package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.service.UserDetailsService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
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
        if (isDuplicateUserInfo(signUpForm)) return "login_signup";
        userDetailsService.saveUser(signUpForm.toUser(passwordEncoder, request));
        return "redirect:/user/login";
    }

    private boolean isDuplicateUserInfo(SignUpForm signUpForm) {
        return userDetailsService.loadUserByEmail(signUpForm.getEmail()) != null ||
                userDetailsService.loadUserByNickname(signUpForm.getNickname()) != null;
    }

    @Data
    class SignUpForm {
        private String email;
        private String password;
        private String nickname;

        public User toUser(PasswordEncoder passwordEncoder, HttpServletRequest request) {
            return User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .lastLoginIp(request.getRemoteAddr())
                    .build();
        }
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
