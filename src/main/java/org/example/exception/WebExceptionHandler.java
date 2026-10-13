package org.example.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(BindException exception) {
        return errorResponse(RequestError.VALIDATION_FAILED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        return errorResponse(RequestError.MISSING_PARAMETER);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException exception) {
        return errorResponse(RequestError.MISSING_HEADER);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterType(
            MethodArgumentTypeMismatchException exception) {
        return errorResponse(RequestError.INVALID_PARAMETER_TYPE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {
        return errorResponse(RequestError.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception) {
        if (exception.isForReturnValue()) {
            log.error("Controller return value validation failed", exception);
            return errorResponse(CommonError.INTERNAL_SERVER_ERROR);
        }
        return errorResponse(RequestError.VALIDATION_FAILED);
    }

    private ResponseEntity<ErrorResponse> errorResponse(ErrorCode error) {
        return ResponseEntity.status(error.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(error.getCode(), error.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(exception.getCode(), exception.getMessage()));
    }
}
