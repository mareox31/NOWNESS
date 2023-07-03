package highfive.nowness;

import highfive.nowness.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                        auth.requestMatchers("/user/logout").hasRole("USER");
                        auth.requestMatchers("/admin", "/admin/**").hasRole("ADMIN");
                        auth.requestMatchers("/**").permitAll();})
                .formLogin(formLoginConfigurer -> formLoginConfigurer
                        .loginPage("/user/login")
                        .loginProcessingUrl("/authenticate")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main"))
                .rememberMe(rememberMeConfigurer -> rememberMeConfigurer
                        .tokenValiditySeconds(2_592_000)) // 하루 86_400초, 일주일 604_800초, 30일 2_592_000초
                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login"))
                .userDetailsService(userDetailsService)
                .build();
    }

}
