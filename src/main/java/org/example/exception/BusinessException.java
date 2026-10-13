package org.example.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public static BusinessException of(ErrorCode error) {
        Objects.requireNonNull(error, "error must not be null");
        return new BusinessException(error.getCode(), error.getMessage(), error.getStatus());
    }

    public HttpStatus getStatus() {
        return status;
    }
}
