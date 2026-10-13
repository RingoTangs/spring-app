package org.example.exception;

import org.springframework.http.HttpStatus;

// Common errors are available for projects created from this template.
@SuppressWarnings("unused")
public enum CommonError implements ErrorCode {

    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "请求参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "未认证"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "无权限访问"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "资源不存在"),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "资源冲突"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final String code;
    private final HttpStatus status;
    private final String message;

    CommonError(String code, HttpStatus status, String message) {
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
