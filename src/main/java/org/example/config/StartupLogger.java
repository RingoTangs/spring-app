package org.example.config;

import jakarta.servlet.ServletContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final ApplicationContext applicationContext;

    public StartupLogger(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartup(ApplicationReadyEvent event) {
        if (event.getApplicationContext() != applicationContext) {
            return;
        }
        Environment environment = applicationContext.getEnvironment();
        logProfiles(environment);
        logSwaggerUrl(environment);
    }

    private void logProfiles(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            log.info("Active profiles: {}", String.join(",", activeProfiles));
        } else {
            log.info("Active profiles: none; default profiles: {}",
                    String.join(",", environment.getDefaultProfiles()));
        }
    }

    private void logSwaggerUrl(Environment environment) {
        if (Boolean.TRUE.equals(environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, false))
                && Boolean.TRUE.equals(environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false))
                && applicationContext instanceof ServletWebServerApplicationContext webContext) {
            String scheme = Boolean.TRUE.equals(environment.getProperty("server.ssl.enabled", Boolean.class, false))
                    ? "https" : "http";
            int port = webContext.getWebServer().getPort();
            @Nullable ServletContext servletContext = webContext.getServletContext();
            if (servletContext == null) {
                return;
            }
            String contextPath = servletContext.getContextPath();
            @Nullable String configuredPath = environment.getProperty("springdoc.swagger-ui.path");
            String swaggerPath = configuredPath != null ? configuredPath : "/swagger-ui.html";
            log.info("Swagger UI: {}://localhost:{}{}{}", scheme, port, contextPath, swaggerPath);
        }
    }
}
