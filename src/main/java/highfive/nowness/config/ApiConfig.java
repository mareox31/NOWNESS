package highfive.nowness.config;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource(value = "classpath:/api.properties", encoding = "utf-8")
@Getter
public class ApiConfig {

    @Value("${api.captcha.client-id}")
    private String apiCaptchaClientId;

    @Value("${api.captcha.client-secret}")
    private String apiCaptchaClientSecret;
}
