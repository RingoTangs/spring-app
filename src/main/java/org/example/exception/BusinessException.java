package org.example.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.Objects;

public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }

    public String getCode() {
        return code;
    }

    public static BusinessException of(ErrorCode error) {
        Objects.requireNonNull(error, "error must not be null");
        return new BusinessException(error.getCode(), error.getMessage(), error.getStatus());
    }

    public static BusinessException of(ErrorCode error, String message) {
        Objects.requireNonNull(error, "error must not be null");
        return new BusinessException(error.getCode(), message, error.getStatus());
    }

    public HttpStatus getStatus() {
        return status;
    }
}
