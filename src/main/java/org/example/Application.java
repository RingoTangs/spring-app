package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
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

        if (environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, false)
                && environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false)
                && context instanceof ServletWebServerApplicationContext webContext) {
            String scheme = environment.getProperty("server.ssl.enabled", Boolean.class, false)
                    ? "https" : "http";
            int port = webContext.getWebServer().getPort();
            String contextPath = webContext.getServletContext().getContextPath();
            String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
            log.info("Swagger UI: {}://localhost:{}{}{}", scheme, port, contextPath, swaggerPath);
        }
    }
}
