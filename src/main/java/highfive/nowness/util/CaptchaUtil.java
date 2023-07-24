package highfive.nowness.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class CaptchaUtil {

    public static String encodeBase64Image(String imagePath) {
        Path path = Paths.get(imagePath);
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

}
