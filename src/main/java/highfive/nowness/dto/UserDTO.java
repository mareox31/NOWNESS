package highfive.nowness.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private LocalDateTime lastPasswordChangeDateTime;
    private String nickname;
    private LocalDateTime createdDateTime;
    private boolean admin;
    private String lastLoginIp;
    private boolean verifiedEmail;
}