package org.example.exception;

import org.springframework.http.HttpStatus;

public enum RequestError implements ErrorCode {

    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "请求参数错误"),
    MISSING_PARAMETER("MISSING_PARAMETER", HttpStatus.BAD_REQUEST, "请求参数错误"),
    MISSING_HEADER("MISSING_HEADER", HttpStatus.BAD_REQUEST, "请求参数错误"),
    INVALID_PARAMETER_TYPE("INVALID_PARAMETER_TYPE", HttpStatus.BAD_REQUEST, "请求参数错误"),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST, "请求参数错误");

    private final String code;
    private final HttpStatus status;
    private final String message;

    RequestError(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
