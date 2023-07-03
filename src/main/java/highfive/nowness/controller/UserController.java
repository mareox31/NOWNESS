package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.service.UserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public UserController(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginForm(@AuthenticationPrincipal User user) {
        if (user == null) return "login_signup";
        else return "redirect:/main";
    }

    @GetMapping("/signup")
    public String showSignupForm(@AuthenticationPrincipal User user) {
        if (user == null) return "login_signup";
        else return "redirect:/main";
    }

    @PostMapping("/signup")
    public String processSignup(SignUpForm signUpForm, HttpServletRequest request) {
        userDetailsService.saveUser(signUpForm.toUser(passwordEncoder, request));
        return "redirect:/user/login";
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
    public String showMypage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "mypage";
    }
}
