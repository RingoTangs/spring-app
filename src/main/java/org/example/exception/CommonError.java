package org.example.exception;

import org.springframework.http.HttpStatus;

// Common errors are available for projects created from this template.
@SuppressWarnings("unused")
public enum CommonError implements ErrorCode {
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "common.bad_request"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "common.unauthorized"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "common.forbidden"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "common.not_found"),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "common.conflict"),
    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "common.internal_server_error");

    private final String code;
    private final HttpStatus status;
    private final String messageKey;

    CommonError(String code, HttpStatus status, String messageKey) {
        this.code = code;
        this.status = status;
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
