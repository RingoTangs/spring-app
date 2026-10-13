package org.example.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessExceptionTest {

    @Test
    void rejectsNullConstructorArguments() {
        assertThrows(NullPointerException.class,
                () -> new BusinessException(null, "message", HttpStatus.BAD_REQUEST));
        assertThrows(NullPointerException.class,
                () -> new BusinessException("CODE", null, HttpStatus.BAD_REQUEST));
        assertThrows(NullPointerException.class,
                () -> new BusinessException("CODE", "message", null));
    }

    @Test
    void rejectsNullErrorCode() {
        assertThrows(NullPointerException.class, () -> BusinessException.of(null));
    }
}
