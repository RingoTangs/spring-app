package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.exception.BusinessException;
import org.example.exception.CommonError;
import org.example.exception.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/examples", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExampleController {

    @Operation(summary = "正常返回 JSON")
    @ApiResponse(responseCode = "200", description = "返回示例消息",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SuccessResponse.class)))
    @GetMapping("/success")
    public SuccessResponse success() {
        return new SuccessResponse("Hello World!");
    }

    @Operation(summary = "业务异常处理示例")
    @ApiResponse(responseCode = "404", description = "业务异常返回错误码和消息",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/business-error")
    public void businessError() {
        throw BusinessException.of(CommonError.NOT_FOUND, "示例资源不存在");
    }

    @Operation(summary = "未知异常兜底处理示例")
    @ApiResponse(responseCode = "500", description = "未知异常返回固定错误信息",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/unexpected-error")
    public void unexpectedError() {
        throw new IllegalStateException("Example unexpected error");
    }

    public record SuccessResponse(String message) {
    }
}
