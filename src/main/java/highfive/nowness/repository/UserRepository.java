package highfive.nowness.repository;

import highfive.nowness.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface UserRepository {
    Optional<UserDTO> findByEmail(String email);
    void save(UserDTO userDTO);
}
