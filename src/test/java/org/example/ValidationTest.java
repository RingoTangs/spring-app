package org.example;

import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ValidationTest.TestController.class)
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validInputsReturnJson() throws Exception {
        mockMvc.perform(post("/examples/validation/body").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"age\":18}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\":\"Alice\",\"age\":18}", JsonCompareMode.STRICT));
        mockMvc.perform(get("/examples/validation/query").param("age", "18"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"age\":18}", JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":\" \",\"age\":18}",
            "{\"name\":null,\"age\":18}", "{\"name\":\"Alice\"}",
            "{\"name\":\"Alice\",\"age\":null}", "{\"name\":\"Alice\",\"age\":0}"})
    void bodyConstraintFailuresReturnValidationCode(String body) throws Exception {
        assertBadRequest(mockMvc.perform(post("/examples/validation/body")
                .contentType(MediaType.APPLICATION_JSON).content(body)), "VALIDATION_FAILED", "请求参数校验失败");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void queryConstraintFailuresReturnValidationCode(String age) throws Exception {
        assertBadRequest(mockMvc.perform(get("/examples/validation/query").param("age", age)), "VALIDATION_FAILED", "请求参数校验失败");
    }

    @Test
    void missingParameterAndHeaderReturnFixedError() throws Exception {
        assertBadRequest(mockMvc.perform(get("/examples/validation/query")), "MISSING_PARAMETER", "缺少必填请求参数");
        assertBadRequest(mockMvc.perform(get("/test/validation/header")), "MISSING_HEADER", "缺少必填请求头");
    }

    @Test
    void invalidReturnValueIsServerError() throws Exception {
        mockMvc.perform(get("/test/validation/return"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"INTERNAL_SERVER_ERROR","message":"服务器内部错误"}
                        """, JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"name\":\"Alice\",\"age\":\"invalid\"}", ""})
    void unreadableBodyReturnsBodyErrorCode(String body) throws Exception {
        assertBadRequest(mockMvc.perform(post("/examples/validation/body")
                .contentType(MediaType.APPLICATION_JSON).content(body)), "INVALID_REQUEST_BODY", "请求体缺失或格式错误");
    }

    @Test
    void invalidQueryTypeReturnsTypeErrorCode() throws Exception {
        assertBadRequest(mockMvc.perform(get("/examples/validation/query").param("age", "invalid")),
                "INVALID_PARAMETER_TYPE", "请求参数类型错误");
    }

    private void assertBadRequest(ResultActions result, String code, String message) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/validation/header")
        String header(@RequestHeader("X-Example") String value) {
            return value;
        }

        @Min(1)
        @GetMapping("/test/validation/return")
        Integer invalidReturn() {
            return 0;
        }
    }
}
