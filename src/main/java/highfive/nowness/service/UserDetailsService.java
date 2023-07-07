package highfive.nowness.service;

import highfive.nowness.domain.User;
import highfive.nowness.dto.UserDTO;
import highfive.nowness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements UserService {

    UserRepository userRepository;

    @Autowired
    UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security 의 UserDetailsService 인터페이스로부터 상속받은 loadUserByUsername 을 구현합니다.
     * 사용자를 식별하기 위한 유일한 값을 입력 받아 {@link org.springframework.security.core.userdetails.UserDetails} 인터페이스를 상속받은 클래스를 반환합니다.
     * 어플리케이션에서는 username 대신 email 을 사용합니다.
     * @param username 사용자가 입력한 email
     * @return email 로 사용자 정보를 불러온 후 {@link org.springframework.security.core.userdetails.UserDetails} 를 상속받은 클래스를 반환
     * @throws UsernameNotFoundException 사용자를 찾지 못한 경우 예외를 던집니다.
     * @author 정성국
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByEmail(username);
    }

    /**
     * 어플리케이션에서 email 을 username 대신 사용하여 loadUserByUsername 으로 인한 혼란을 방지하기 위해 추가합니다.
     * @param email 사용자가 입력한 email
     * @return email 로 사용자 정보를 불러온 후 {@link org.springframework.security.core.userdetails.UserDetails} 를 상속받은 클래스를 반환
     * @throws UsernameNotFoundException 사용자를 찾지 못한 경우 예외를 던집니다.
     * @author 정성국
     */
    @Override
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        UserDTO userDto = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new User(userDto);
    }

    @Override
    public User loadUserByNickname(String nickname) throws UsernameNotFoundException {
        UserDTO userDto = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException(nickname));
        return new User(userDto);
    }

    /**
     * 저장소에 신규 사용자 정보를 저장합니다.
     * @param user 사용자 도메인 클래스
     * @author 정성국
     */
    public void saveUser(User user) {
        userRepository.save(UserDTO.builder()
                .email(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .lastLoginIp(user.getLastLoginIp())
                .build());
    }

    public boolean isExistEmail(String email) {
        return userRepository.countByEmail(email) == 1;
    }

    public boolean isExistNickname(String nickname) {
        return userRepository.countByNickname(nickname) == 1;
    }
}
