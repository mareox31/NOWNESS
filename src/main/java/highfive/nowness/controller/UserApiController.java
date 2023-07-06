package highfive.nowness.controller;

import highfive.nowness.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserApiController {

    private final UserDetailsService userDetailsService;

    @Autowired
    UserApiController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
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
}
