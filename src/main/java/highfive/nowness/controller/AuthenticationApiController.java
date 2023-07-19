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

    @DeleteMapping("/token")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        AtomicReference<ResponseEntity<Boolean>> responseEntity = new AtomicReference<>(ResponseEntity.ok(true));
        Optional.of(SecurityContextHolder.getContext()).ifPresentOrElse(
                context -> {SecurityContextHolder.clearContext(); request.getSession(false).invalidate();},
                () -> responseEntity.set(ResponseEntity.noContent().build()));

        return responseEntity.get();
    }
}
