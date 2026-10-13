package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class WebExceptionFallbackConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionFallbackConfiguration.class);

    private final ObjectMapper objectMapper;

    public WebExceptionFallbackConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add((request, response, handler, exception) -> {
            log.error("Unhandled exception during {} {}", request.getMethod(), request.getRequestURI(), exception);
            if (response.isCommitted()) {
                return null;
            }
            try {
                byte[] body = objectMapper.writeValueAsBytes(
                        new ErrorResponse("INTERNAL_SERVER_ERROR", "服务器内部错误"));
                response.resetBuffer();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setContentLength(body.length);
                response.getOutputStream().write(body);
                return new ModelAndView();
            } catch (IOException | IllegalStateException writeException) {
                log.error("Failed to write fallback error response", writeException);
                return null;
            }
        });
    }
}
