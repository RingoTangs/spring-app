package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(Application.class, args);
        Environment environment = context.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            log.info("Active profiles: {}", String.join(",", activeProfiles));
        } else {
            log.info("Active profiles: none; default profiles: {}",
                    String.join(",", environment.getDefaultProfiles()));
        }
    }
}
