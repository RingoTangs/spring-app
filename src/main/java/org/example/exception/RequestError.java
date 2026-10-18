package org.example.exception;

import org.springframework.http.HttpStatus;

public enum RequestError implements ErrorCode {
    VALIDATION_FAILED("VALIDATION_FAILED", "request.validation_failed"),
    MISSING_PARAMETER("MISSING_PARAMETER", "request.missing_parameter"),
    MISSING_HEADER("MISSING_HEADER", "request.missing_header"),
    INVALID_PARAMETER_TYPE("INVALID_PARAMETER_TYPE", "request.invalid_parameter_type"),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", "request.invalid_request_body");

    private final String code;
    private final HttpStatus status = HttpStatus.BAD_REQUEST;
    private final String messageKey;

    RequestError(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
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
    public String getMessageKey() {
        return messageKey;
    }
}
