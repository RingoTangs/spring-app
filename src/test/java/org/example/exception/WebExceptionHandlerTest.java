package org.example.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebExceptionHandlerTest.TestController.class)
@Import({WebExceptionHandler.class, WebExceptionHandlerTest.TestController.class})
class WebExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "CONFLICT"})
    void returnsBusinessStatusAndErrorBody(HttpStatus httpStatus) throws Exception {
        mockMvc.perform(get("/test/business-error").param("status", httpStatus.name()))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"BUSINESS_ERROR","message":"业务操作失败"}
                        """, org.springframework.test.json.JsonCompareMode.STRICT));
    }

    @Test
    void returnsJsonForPlainTextEndpoint() throws Exception {
        mockMvc.perform(get("/test/plain-text-error").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"USER_NOT_FOUND","message":"用户不存在"}
                        """, org.springframework.test.json.JsonCompareMode.STRICT));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business-error")
        String businessError(@RequestParam HttpStatus status) {
            throw new BusinessException("BUSINESS_ERROR", "业务操作失败", status);
        }

        @GetMapping(value = "/test/plain-text-error", produces = MediaType.TEXT_PLAIN_VALUE)
        String plainTextError() {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.NOT_FOUND);
        }
    }
}
