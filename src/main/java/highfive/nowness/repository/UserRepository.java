package highfive.nowness.repository;

import highfive.nowness.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Mapper
@Repository
public interface UserRepository {
    Optional<UserDTO> findByEmail(String email);
    Optional<UserDTO> findByNickname(String nickname);
    void save(UserDTO userDTO);
    int countByEmail(String email);
    int countByNickname(String nickname);
    void saveUnverifiedEmail(String code, String email);
    void verifyEmail(Map<String, Object> params);
    void savePasswordResetEmail(String code, String email);
    int countPasswordResetEmail(String code);
    void resetPassword(Map<String, Object> params);
}
