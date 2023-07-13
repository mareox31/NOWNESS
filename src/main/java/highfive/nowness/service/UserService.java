package highfive.nowness.service;

import highfive.nowness.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {
    UserDetails loadUserByEmail(String email) throws UsernameNotFoundException;
    User loadUserByNickname(String nickname) throws UsernameNotFoundException;
}
