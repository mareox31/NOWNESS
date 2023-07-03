package highfive.nowness.domain;

import highfive.nowness.dto.UserDTO;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@RequiredArgsConstructor
@Component
public class User implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final LocalDateTime lastPasswordChangeDateTime;
    private final String nickname;
    private final LocalDateTime createdDateTime;
    private final boolean admin;
    private final String lastLoginIp;

    public User(UserDTO userDto) {
        id = userDto.getId();
        email = userDto.getEmail();
        password = userDto.getPassword();
        lastPasswordChangeDateTime = userDto.getLastPasswordChangeDateTime();
        nickname = userDto.getNickname();
        createdDateTime = userDto.getCreatedDateTime();
        admin = userDto.isAdmin();
        lastLoginIp = userDto.getLastLoginIp();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = admin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
