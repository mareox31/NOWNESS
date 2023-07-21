package highfive.nowness.service;

import highfive.nowness.domain.User;
import highfive.nowness.dto.UserDTO;
import highfive.nowness.repository.BoardRepository;
import highfive.nowness.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDetailsService implements UserService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final JavaMailSender javaMailSender;

    @Autowired
    UserDetailsService(UserRepository userRepository, JavaMailSender javaMailSender, BoardRepository boardRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.javaMailSender = javaMailSender;
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
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        UserDTO userDto = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new User(userDto);
    }

    @Transactional(readOnly = true)
    @Override
    public User loadUserByNickname(String nickname) throws UsernameNotFoundException {
        UserDTO userDto = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException(nickname));
        return new User(userDto);
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.saveOrUpdateUser(UserDTO.builder()
                .email(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .lastLoginIp(user.getLastLoginIp())
                .build());
    }

    @Transactional(readOnly = true)
    public boolean isExistEmail(String email) {
        return userRepository.findIdByEmail(email) > 0;
    }

    @Transactional(readOnly = true)
    public boolean isExistNickname(String nickname) {
        return userRepository.countByNickname(nickname) == 1;
    }

    public void sendPasswordResetEmail(User user, String siteURL, String resetCode) {
        String toAddress = user.getEmail();
        String fromAddress = "nowness@nowness.com";
        String senderName = "Nowness";
        String subject = "요청하신 비밀번호 재설정 링크입니다.";
        String content
                = "<p>안녕하세요 [[name]]님, 요청하신 비밀번호 재설정 링크를 보내드립니다.</p><br>"
                + "<p>아래 링크 클릭하셔서 비밀번호를 변경해 주세요.</p><br>"
                + "<h3><a href=\"[[URL]]\">비밀번호 재설정 하기</a></h3>"
                + "<p>보안을 위해 3일 후에는 링크를 통해 비밀번호 재설정이 불가능하며, 비밀번호 찾기 메뉴에서 다시 요청하셔야 합니다.</p><br>"
                + "감사합니다.<br>"
                + "Nowness Team";

        content = content.replace("[[name]]", user.getNickname());

        String resetURL = siteURL + "/user/reset-password?code=" + resetCode;
        content = content.replace("[[URL]]", resetURL);

        var email = new Email(toAddress, fromAddress, senderName, subject, content);

        var helper = new MimeMessageHelper(javaMailSender.createMimeMessage());

        javaMailSender.send(convertEmailToMimeMessage(email, helper));
    }

    public void sendVerificationEmail(User user, String siteURL, String verifyCode) {
        String toAddress = user.getEmail();
        String fromAddress = "nowness@nowness.com";
        String senderName = "Nowness";
        String subject = "회원 가입을 환영합니다.";
        String content
                = "<p>[[name]]님, 회원 가입을 환영합니다.</p><br>"
                + "<p>아래 링크를 눌러 회원 가입을 완료해주세요.</p><br>"
                + "<h3><a href=\"[[URL]]\">이메일 확인</a></h3>"
                + "감사합니다.<br>"
                + "Nowness Team";

        content = content.replace("[[name]]", user.getNickname());

        String verifyURL = siteURL + "/user/verify?code=" + verifyCode;
        content = content.replace("[[URL]]", verifyURL);

        var email = new Email(toAddress, fromAddress, senderName, subject, content);

        var helper = new MimeMessageHelper(javaMailSender.createMimeMessage());

        javaMailSender.send(convertEmailToMimeMessage(email, helper));
    }

    private record Email(String toAddress, String fromAddress, String senderName, String subject, String content) {}

    private MimeMessage convertEmailToMimeMessage(Email email, MimeMessageHelper helper) {
        try {
            helper.setFrom(email.fromAddress(), email.senderName());
            helper.setTo(email.toAddress());
            helper.setSubject(email.subject());
            helper.setText(email.content(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return helper.getMimeMessage();
    }

    @Transactional
    public void saveUnverifiedEmail(String code, String email) {
        userRepository.saveUnverifiedEmail(code, email);
    }

    /**
     * ID/PW 기반 회원가입 시 사용자가 작성한 이메일을 확인 및 인증합니다.
     * @param code 회원 이메일 인증을 위한 식별자
     * @return 이메일 인증이 완료된 경우 true, 완료되지 않은 경우 false 를 반환
     */
    @Transactional
    public boolean verifyEmail(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        userRepository.verifyEmail(params);
        return (int) params.get("deletedRows") == 1;
    }

    @Transactional
    public void savePasswordResetEmail(String code, String email) {
        userRepository.savePasswordResetEmail(code, email);
    }

    @Transactional(readOnly = true)
    public boolean isExistPasswordResetCode(String code) {
        return userRepository.countPasswordResetEmail(code) > 0;
    }

    @Transactional
    public boolean resetPasswordByResetCode(String code, String newPassword) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("newPassword", newPassword);
        userRepository.resetPassword(params);
        return (int) params.get("updatedRows") == 1;
    }

    @Transactional
    public boolean changePassword(String email, String newPassword) {
        return userRepository.changePasswordByEmail(email, newPassword) == 1;
    }

    @Transactional
    public boolean changeNickname(String email, String newNickname) {
        return userRepository.changeNicknameByEmail(email, newNickname) == 1;
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> getRecentContentsAndReplies(long userId) {
        return boardRepository.loadRecentContentsAndReplies(userId);
    }

    @Transactional
    public boolean withdrawal(long userId) {
        return userRepository.deleteUser(userId) == 1;
    }
}
