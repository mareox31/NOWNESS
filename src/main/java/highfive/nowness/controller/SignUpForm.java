package highfive.nowness.controller;

import highfive.nowness.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class SignUpForm {
    private String email;
    private String password;
    private String nickname;
    private String captchaKey;
    private String captchaInput;

    public User toUser(PasswordEncoder passwordEncoder, HttpServletRequest request) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .lastLoginIp(request.getRemoteAddr())
                .build();
    }
}
