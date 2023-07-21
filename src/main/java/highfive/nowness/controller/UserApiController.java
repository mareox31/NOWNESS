package highfive.nowness.controller;

import highfive.nowness.domain.User;
import highfive.nowness.service.UserDetailsService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    /**
     * 비밀번호 찾기 메뉴를 통해 입력받은 정보로 비밀번호 초기화 메일을 발송합니다.
     * @param request 사용자 요청에 대한 정보를 갖고 있습니다.
     * @param findPasswordForm 사용자가 비밀번호를 재설정하기 위해 입력한 메일 정보를 갖고 있습니다.
     * @return 존재하는 이메일일 경우 200 OK, 그렇지 않을 경우 204 No Content 반환
     */
    @PostMapping("/password")
    public ResponseEntity<Boolean> findPassword(HttpServletRequest request, @RequestBody FindPasswordForm findPasswordForm) {
        AtomicReference<ResponseEntity<Boolean>> responseEntity = new AtomicReference<>(ResponseEntity.ok(true));
        getUserInfoByEmail(findPasswordForm.email()).ifPresentOrElse(user -> {
                    String resetCode = UserUtil.getRandomCode();
                    userDetailsService.sendPasswordResetEmail(user, UserUtil.getHost(), resetCode);
                    userDetailsService.savePasswordResetEmail(resetCode, findPasswordForm.email());
                },
                () -> responseEntity.set(ResponseEntity.noContent().build()));
        return responseEntity.get();
    }

    private Optional<User> getUserInfoByEmail(String email) {
        try {
            return Optional.of((User) userDetailsService.loadUserByEmail(email));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private record FindPasswordForm(String email) { }

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

    @PatchMapping("/password")
    public ResponseEntity<Boolean> changePassword(@AuthenticationPrincipal User user,
                                                  @AuthenticationPrincipal OAuth2User oAuth2User,
                                                  @RequestBody ChangePasswordForm changePasswordForm) {
        if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
        String email = changePasswordForm.email();
        if (isNotQualifiedUser(user, email)) return ResponseEntity.badRequest().build();

        String password = passwordEncoder.encode(changePasswordForm.newPassword());
        boolean isPasswordUpdated = userDetailsService.changePassword(email, password);
        if (isPasswordUpdated) {
            changePasswordToAuthenticatedUser(user, password);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private record ChangePasswordForm(String email, String newPassword) { }

    private boolean isNotQualifiedUser(User user, String email) {
        return !user.getEmail().equals(email);
    }

    private void changePasswordToAuthenticatedUser(User user, String password) {
        SecurityContext context = SecurityContextHolder.getContext();
        User changedUser = (userDetailsService.loadUserByNickname(user.getNickname()));
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                changedUser, changedUser.getPassword(), user.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @PostMapping("/unverified-email")
    public ResponseEntity<Boolean> sendVerificationEmail(
            @RequestBody ResendingVerificationEmailForm resendingVerificationEmailForm) {

        AtomicReference<ResponseEntity<Boolean>> responseEntity = new AtomicReference<>(ResponseEntity.ok(true));

        getUserInfoByEmail(resendingVerificationEmailForm.email()).ifPresentOrElse(user -> {
            if (user.isVerifiedEmail()) {
                responseEntity.set(ResponseEntity.badRequest().build());
            } else {
                String verificationCode = UserUtil.getRandomCode();
                userDetailsService.sendVerificationEmail(user, UserUtil.getHost(), verificationCode);
                userDetailsService.saveUnverifiedEmail(verificationCode, user.getEmail());
            }
        }, () -> responseEntity.set(ResponseEntity.noContent().build()));

        return responseEntity.get();

    }

    private record ResendingVerificationEmailForm(String email) { }

    @GetMapping("/nickname")
    public ResponseEntity<Boolean> checkDuplicateNickname(@RequestParam(name = "newNickname") String nickname) {
        boolean isDuplicate = userDetailsService.isExistNickname(nickname);
        return ResponseEntity.ok(isDuplicate);
    }

    @PatchMapping("/nickname")
    public ResponseEntity<String> changeNickname(@AuthenticationPrincipal User user,
                                                 @AuthenticationPrincipal OAuth2User oAuth2User,
                                                 @RequestBody ChangeNicknameForm form) {

        if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
        String email = form.email();
        if (isNotQualifiedUser(user, email)) return ResponseEntity.badRequest().build();

        String nickname = form.newNickname();
        boolean isNicknameUpdated = userDetailsService.changeNickname(email, nickname);
        if (isNicknameUpdated) {
            changeNicknameToAuthenticatedUser(user, nickname);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    public record ChangeNicknameForm (String email, String newNickname) {}

    private void changeNicknameToAuthenticatedUser(User user, String nickname) {
        SecurityContext context = SecurityContextHolder.getContext();
        User changedUser = (userDetailsService.loadUserByNickname(nickname));
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                changedUser, user.getPassword(), user.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> withdrawal(@AuthenticationPrincipal User user,
                                              @AuthenticationPrincipal OAuth2User oAuth2User,
                                              HttpServletRequest request,
                                              @PathVariable long id) {
        if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
        if (user.getId() == id && userDetailsService.withdrawal(id)) {
            SecurityContextHolder.clearContext();
            request.getSession(false).invalidate();
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.badRequest().build();
    }

}