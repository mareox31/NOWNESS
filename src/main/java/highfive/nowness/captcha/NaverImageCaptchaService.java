package highfive.nowness.captcha;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import highfive.nowness.config.ApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
public class NaverImageCaptchaService {

    private final String clientId;
    private final String clientSecret;

    @Autowired
    public NaverImageCaptchaService(ApiConfig apiConfig) {
        this.clientId = apiConfig.getApiCaptchaClientId();
        this.clientSecret = apiConfig.getApiCaptchaClientSecret();
    }

    public String getCaptchaKey() {
        try {
            String apiURL = "https://naveropenapi.apigw.ntruss.com/captcha/v1/nkey?code=0";
            BufferedReader br = getBufferedReader(apiURL);
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            log.info("captcha key: " + response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.toString());
            return jsonNode.get("key").asText();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return "";
    }

    public String getUserInputEvaluationResult(String captchaKey, String userInput) {
        try {
            String apiURL = "https://naveropenapi.apigw.ntruss.com/captcha/v1/nkey?code=1&key="+ captchaKey + "&value="+ userInput;
            BufferedReader br = getBufferedReader(apiURL);
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            log.info("CAPTCHA User Input Evaluation Result: " + response.toString());

            return response.toString();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return "";
    }

    private BufferedReader getBufferedReader(String apiURL) throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
        con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
        int responseCode = con.getResponseCode();
        BufferedReader br;
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 오류 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        return br;
    }

    public byte[] getCaptchaImage(String captchaKey) {
        String apiURL = buildApiURL(captchaKey);
        try {
            HttpURLConnection con = establishHttpConnection(apiURL);
            return handleResponse(con);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return new byte[]{};

    }

    private String buildApiURL(String captchaKey) {
        return "https://naveropenapi.apigw.ntruss.com/captcha-bin/v1/ncaptcha?key="
                + captchaKey + "&X-NCP-APIGW-API-KEY-ID=" + clientId;
    }

    private HttpURLConnection establishHttpConnection(String apiURL) throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }

    private byte[] handleResponse(HttpURLConnection con) throws IOException{
        if(con.getResponseCode() == 200) {
            return con.getInputStream().readAllBytes();
        } else {
            handleError(con);
            return new byte[]{};
        }
    }

    private void handleError(HttpURLConnection con) throws IOException {
        var br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();
        log.info("[Failed Create CAPTCHA Image] Message: " + response.toString());
    }

}
