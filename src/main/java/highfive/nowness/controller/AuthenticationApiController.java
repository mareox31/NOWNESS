package highfive.nowness.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/v1/user")
public class AuthenticationApiController {

    /**
     * 현재 사용자의 로그아웃을 수행합니다. 사용자의 Credentials가 저장된 SecurityContext 를 비우고, 세션을  파기합니다.
     * @param request the HTTP request object
     * @return 로그아웃 성공 시 200(success), 실패 시  204(No Content)를 반환
     */
    @DeleteMapping("/token")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        AtomicReference<ResponseEntity<Boolean>> responseEntity = new AtomicReference<>(ResponseEntity.ok(true));
        Optional.of(SecurityContextHolder.getContext()).ifPresentOrElse(
                context -> {SecurityContextHolder.clearContext(); request.getSession(false).invalidate();},
                () -> responseEntity.set(ResponseEntity.noContent().build()));
        return responseEntity.get();
    }

}
