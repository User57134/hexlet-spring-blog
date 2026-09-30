package io.hexlet.controller.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@Component
@ConfigurationProperties(prefix = "app")
@Setter
@Getter
class AppDefaults {
    private String welcomeMessage;
    private int pageSize;
    private String adminEmail;
}


@RestController
public class WelcomeController {
    @Autowired
    private AppDefaults defaultValues;

    @Value("${app.welcome-message}")
    private String welcomeMessage;

    @GetMapping("/welcome")
    public String welcome() {
        return welcomeMessage;
    }

    @GetMapping("/welcome/defaults")
    public Map<String, String> defaults() {
        return Map.of("welcome-message", defaultValues.getWelcomeMessage()
                , "admin-email", defaultValues.getAdminEmail()
                , "page-size", defaultValues.getPageSize() + "");
    }

}
