package org.example.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void rejectsNullCustomMessageArguments() {
        assertThrows(NullPointerException.class, () -> BusinessException.of(null, "message"));
        assertThrows(NullPointerException.class, () -> BusinessException.of(CommonError.NOT_FOUND, null));
    }

    @Test
    void customMessagesDoNotChangeDefaultMessage() {
        BusinessException first = BusinessException.of(CommonError.NOT_FOUND, "用户 alice 不存在");
        BusinessException second = BusinessException.of(CommonError.NOT_FOUND, "用户 bob 不存在");

        assertEquals("用户 alice 不存在", first.getMessage());
        assertEquals("用户 bob 不存在", second.getMessage());
        assertEquals("资源不存在", CommonError.NOT_FOUND.getMessage());
        assertEquals("资源不存在", BusinessException.of(CommonError.NOT_FOUND).getMessage());
    }
}
