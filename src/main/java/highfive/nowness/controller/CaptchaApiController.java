package highfive.nowness.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import highfive.nowness.captcha.NaverImageCaptchaService;
import highfive.nowness.util.CaptchaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/captcha")
public class CaptchaApiController {

    private final NaverImageCaptchaService naverImageCaptchaService;

    @GetMapping("/image/{key}")
    public ResponseEntity<CaptchaEvaluationResult> evaluateCaptcha(@PathVariable String key,
                                                               @RequestParam String input) {
        String response = naverImageCaptchaService.getUserInputEvaluationResult(key, input);
        return ResponseEntity.ok(getCaptchaEvaluationResult(response));
    }

    /**
     * Naver Image Captcha 응답을 저장하는 record 입니다.
     * <a href="https://api.ncloud-docs.com/docs/ai-naver-captcha-image">API 문서 링크</a>
     * @param result 정답이 맞은 경우 true, 정답이 틀린 경우 false
     * @param responseTime 정답을 맞추는데 걸린 시간. 응답 범위 -1 ~ 7200 사이의 값.
     */
    public record CaptchaEvaluationResult(boolean result, int responseTime) {}

    /**
     * Naver Image Captcha 응답으로부터 JSON 을 추출하여 {@link CaptchaEvaluationResult} 에 값 저장 후 반환합니다.
     * @param response Naver Image Captcha API 응답
     * @return CaptchaEvaluationResult 반환
     */
    private CaptchaEvaluationResult getCaptchaEvaluationResult(String response) {
        JsonNode jsonNode = null;
        try {
            jsonNode = new ObjectMapper().readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        boolean result = jsonNode.get("result").asBoolean();
        int responseTime = jsonNode.get("responseTime").asInt();
        return new CaptchaEvaluationResult(result, responseTime);
    }

    @GetMapping("/image")
    public ResponseEntity<CaptchaImage> getNewCaptchaImage() {
        String captchaKey = naverImageCaptchaService.getCaptchaKey();
        String captchaImagePath = naverImageCaptchaService.getCaptchaImagePath(captchaKey);
        var captchaImage = new CaptchaImage(captchaKey, CaptchaUtil.encodeBase64Image(captchaImagePath));
        return ResponseEntity.ok(captchaImage);
    }

    public record CaptchaImage(String captchaKey, String encodedCaptchaImage){}
}
