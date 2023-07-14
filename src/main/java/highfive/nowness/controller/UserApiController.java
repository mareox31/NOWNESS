package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.service.UserDetailsService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/v1/user")
public class UserApiController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    UserApiController(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 중복된 email 또는 nickname 이 있는지 확인합니다.
     * @param signUpForm 클라이언트에서 보내온 입력 값인 email 또는 nickname 을 저장하고 있습니다.
     * @return 중복된 정보가 있는 경우 true, 없는 경우 false 반환
     * @author 정성국
     */
    @PostMapping("/duplicate")
    public ResponseEntity<Boolean> duplicateCheck(@RequestBody SignUpForm signUpForm) {
        boolean isExist;
        if (signUpForm.getEmail() == null) {
            isExist = userDetailsService.isExistNickname(signUpForm.getNickname());
        } else {
            isExist = userDetailsService.isExistEmail(signUpForm.getEmail());
        }
        return ResponseEntity.ok().body(isExist);
    }

    @PostMapping("/password")
    public ResponseEntity<Boolean> findPassword(HttpServletRequest request, @RequestBody FindPasswordForm findPasswordForm) {
        AtomicReference<ResponseEntity<Boolean>> responseEntity = new AtomicReference<>(ResponseEntity.ok(true));
        getUserInfo(findPasswordForm.email()).ifPresentOrElse(user -> {
                    String resetCode = UserUtil.getRandomCode();
                    userDetailsService.sendPasswordResetEmail(user, "http://%s".formatted(request.getHeader("host")), resetCode);
                    userDetailsService.savePasswordResetEmail(resetCode, findPasswordForm.email());
                },
                () -> responseEntity.set(ResponseEntity.noContent().build()));
        return responseEntity.get();
    }

    private Optional<User> getUserInfo(String email) {
        try {
            return Optional.of((User) userDetailsService.loadUserByEmail(email));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private record FindPasswordForm(String email) {}

    @PutMapping("/password")
    public ResponseEntity<Boolean> resetPassword(@RequestBody ResetPasswordForm resetPasswordForm) {
        boolean isPasswordUpdated = userDetailsService.resetPasswordByResetCode(resetPasswordForm.code(),
                passwordEncoder.encode(resetPasswordForm.password()));
        if (isPasswordUpdated) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private record ResetPasswordForm(String code, String password) {}
}
