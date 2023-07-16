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
import org.springframework.web.bind.annotation.RequestParam;

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
    public String processIdPwSignup(SignUpForm signUpForm, HttpServletRequest request) {
        if (isDuplicateUserInfo(signUpForm)) return "redirect:/user/signup";
        User user = signUpForm.toUser(passwordEncoder, request);
        userDetailsService.saveUser(user);
        String code = UserUtil.getRandomCode();
        userDetailsService.sendVerificationEmail(user, "http://%s".formatted(request.getHeader("host")), code);
        userDetailsService.saveUnverifiedEmail(code, user.getEmail());
        return "welcome_signup";
    }

    private boolean isDuplicateUserInfo(SignUpForm signUpForm) {
        try {
            userDetailsService.loadUserByEmail(signUpForm.getEmail());
            userDetailsService.loadUserByNickname(signUpForm.getNickname());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * ID/PW 를 사용하여 회원가입한 사용자가 이메일 검증을 위해 발송된 이메일을 통해 접근하는 경로입니다.
     * @param code 사용자가 회원가입 시 받은 인증 코드
     * @return 저장된 code 가 있다면 email 확인이 완료되었다는 문구가 작성된 화면을 반환하고, 그렇지 않은 경우 다른 화면을 반환합니다.
     */
    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam String code) {
        if (userDetailsService.verifyEmail(code)) return "email_verified";
        else return "redirect:/";
    }

    // 테스트용
    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "welcome_signup";
    }

    // 테스트용
    @GetMapping("/testmail")
    public String testMail() {
        String code = UserUtil.getRandomCode();
        String email = "--@gmail.com";
        userDetailsService.sendVerificationEmail(User.builder()
                        .email(email)
                        .nickname("tester")
                        .build(),
                "http://localhost:8080",
                        code);
        userDetailsService.saveUnverifiedEmail(code, email);
        return "welcome_signup";
    }

    @GetMapping("/find-password")
    public String showFindPasswordForm() {
        return "find_password";
    }

    @GetMapping("/reset-password")
    public String showChangePasswordForm(@RequestParam String code) {
        if (userDetailsService.isExistPasswordResetCode(code)) return "reset_password";
        else return "reset_password_code_expired";
    }

    @GetMapping("/mypage")
    public String showMypage(@AuthenticationPrincipal User user,
                             @AuthenticationPrincipal OAuth2User oAuth2User,
                             Model model) {
        if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
        UserUtil.addPublicUserInfoToModel(model, user);
        return "mypage";
    }

}
