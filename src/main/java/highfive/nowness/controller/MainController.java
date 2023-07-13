package highfive.nowness.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/main")
    String main() {
        return "main";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/rankboard")
    public String rankboard() {
        return "rankboard";
    }

    @GetMapping("/reportboard")
    public String reportboard() {
        return "reportboard";
    }

    @GetMapping("/requestboard")
    public String requestboard() {
        return "requestboard";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }



}
