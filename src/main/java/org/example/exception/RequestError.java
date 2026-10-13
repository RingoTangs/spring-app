package org.example.exception;

import org.springframework.http.HttpStatus;

public enum RequestError implements ErrorCode {
    VALIDATION_FAILED("VALIDATION_FAILED", "请求参数校验失败"),
    MISSING_PARAMETER("MISSING_PARAMETER", "缺少必填请求参数"),
    MISSING_HEADER("MISSING_HEADER", "缺少必填请求头"),
    INVALID_PARAMETER_TYPE("INVALID_PARAMETER_TYPE", "请求参数类型错误"),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", "请求体缺失或格式错误");

    private final String code;
    private final HttpStatus status = HttpStatus.BAD_REQUEST;
    private final String message;

    RequestError(String code, String message) {
        this.code = code;
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
