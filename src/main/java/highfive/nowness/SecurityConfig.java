package highfive.nowness;

import highfive.nowness.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/user/mypage", "/user/logout", "/user/withdrawal","/request/modify/*", "/request/writer").hasRole("USER"); // 로그인하지 않은 채로 명시된 경로에 접근하면 로그인 페이지로 redirect
                        auth.requestMatchers("/admin", "/admin/**").hasRole("ADMIN");
                        auth.requestMatchers("/**").permitAll();})
                .formLogin(formLoginConfigurer -> formLoginConfigurer
                        .loginPage("/user/login")
                        .loginProcessingUrl("/authenticate")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main"))
                .rememberMe(rememberMeConfigurer -> rememberMeConfigurer
                        .tokenValiditySeconds((int)Duration.ofDays(30).toSeconds()))
                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login"))
                .userDetailsService(userDetailsService)
                .oauth2Login(Customizer.withDefaults())
                .build();
    }

}
